/**
 * Copyright (C) Telicent Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.telicent.smart.cache.sources.kafka;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableLongGauge;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import io.telicent.smart.cache.observability.TelicentMetrics;
import io.telicent.smart.cache.projectors.utils.PeriodicAction;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSourceException;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicy;
import io.telicent.smart.cache.sources.offsets.OffsetStore;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.consumer.InvalidOffsetException;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.apache.commons.lang3.Strings.CI;

/**
 * An event source backed by Kafka
 *
 * @param <TKey>   Event key type
 * @param <TValue> Event value type
 */
public class KafkaEventSource<TKey, TValue>
        extends AbstractBufferedEventSource<ConsumerRecord<TKey, TValue>, TKey, TValue> {

    /**
     * Creates a new builder for building a Kafka Event Source
     *
     * @param <TKey>   Event key type
     * @param <TValue> Event value type
     * @return Builder
     */
    public static <TKey, TValue> Builder<TKey, TValue> create() {
        return new Builder<>();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaEventSource.class);

    private final KafkaReadPolicy<TKey, TValue> readPolicy;
    private final Consumer<TKey, TValue> consumer;
    private final String server, consumerGroup;
    private final Set<String> topics;
    private final int maxPollRecords;
    private boolean firstRun = true;
    private final TopicExistenceChecker topicExistenceChecker;
    private final boolean autoCommit;
    private final Map<TopicPartition, OffsetAndMetadata> autoCommitOffsets = new HashMap<>();
    private final Queue<Map<TopicPartition, OffsetAndMetadata>> delayedOffsetCommits = new ConcurrentLinkedDeque<>();
    private final Map<TopicPartition, Long> delayedOffsetResets = new ConcurrentHashMap<>();
    private final OffsetStore externalOffsetStore;
    private Thread pollThread = null;
    private final PeriodicAction positionLogger, lagWarning;
    private final Attributes metricAttributes;
    private final DoubleHistogram pollTimingMetric;
    private final LongHistogram fetchCountsMetric;
    @SuppressWarnings("unused")
    private final ObservableLongGauge lagMetric;
    private volatile boolean resetInProgress = false;

    private Long lastObservedLag = null;

    /**
     * Creates a new event source backed by a Kafka topic
     *
     * @param bootstrapServers       Kafka Bootstrap servers
     * @param topics                 Kafka topic(s) to subscribe to
     * @param groupId                Kafka Consumer Group ID
     * @param keyDeserializerClass   Key deserializer class
     * @param valueDeserializerClass Value deserializer class
     * @param maxPollRecords         Maximum events to retrieve and buffer in one Kafka
     *                               {@link KafkaConsumer#poll(Duration)} request.
     * @param policy                 Kafka Read Policy to control what events to read from the configured topic
     * @param autoCommit             Whether the event source will automatically commit Kafka positions
     * @param offsetStore            An external offset store to commit offsets to in addition to committing them to
     *                               Kafka
     * @param lagReportInterval      Lag reporting interval
     * @param properties             Kafka Consumer Properties, these may be overwritten by explicit configuration
     *                               passed as other parameters
     */
    KafkaEventSource(final String bootstrapServers, final Set<String> topics, final String groupId,
                     final String keyDeserializerClass, final String valueDeserializerClass, final int maxPollRecords,
                     final KafkaReadPolicy<TKey, TValue> policy, final boolean autoCommit,
                     final OffsetStore offsetStore, final Duration lagReportInterval, final Properties properties) {
        if (StringUtils.isBlank(bootstrapServers)) {
            throw new IllegalArgumentException("Kafka bootstrapServers cannot be null");
        }
        if (CollectionUtils.isEmpty(topics)) {
            throw new IllegalArgumentException("Kafka topic(s) to read cannot be null");
        }
        if (StringUtils.isBlank(groupId)) {
            throw new IllegalArgumentException("Kafka Consumer groupID cannot be null");
        }
        if (StringUtils.isBlank(keyDeserializerClass)) {
            throw new IllegalArgumentException("Kafka keyDeserializerClass cannot be null");
        }
        if (StringUtils.isBlank(valueDeserializerClass)) {
            throw new IllegalArgumentException("Kafka valueDeserializerClass cannot be null");
        }
        if (maxPollRecords < 1) {
            throw new IllegalArgumentException("Kafka maxPollRecords must be >= 1");
        }
        Objects.requireNonNull(policy, "Kafka readPolicy cannot be null");
        // NB - Not validating lagReportInterval because that gets validated by the PeriodicAction constructor

        // Configure our Kafka consumer appropriately
        Properties props = new Properties();
        if (properties != null) {
            props.putAll(properties);
        }
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, Integer.toString(maxPollRecords));
        props.setProperty(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, Integer.toString(10 * 1024 * 1024));
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializerClass);
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializerClass);
        props.setProperty(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, "false");

        // Allow the read policy to further configure the consumer configuration as needed
        policy.prepareConsumerConfiguration(props);

        this.consumer = createConsumer(props);
        this.server = bootstrapServers;
        this.consumerGroup = groupId;
        this.topics = new LinkedHashSet<>(topics);
        this.readPolicy = policy;
        this.readPolicy.setConsumer(this.consumer);
        this.maxPollRecords = maxPollRecords;
        this.autoCommit = autoCommit;
        this.externalOffsetStore = offsetStore;
        this.topicExistenceChecker =
                new TopicExistenceChecker(createAdminClient(props), this.server, this.topics, LOGGER);

        // Prepare metrics, for Messaging systems there are a bunch of predefined attributes we reuse
        this.metricAttributes = Attributes.of(SemanticAttributes.MESSAGING_KAFKA_CONSUMER_GROUP, groupId,
                                              SemanticAttributes.MESSAGING_OPERATION, "process",
                                              SemanticAttributes.MESSAGING_DESTINATION_NAME,
                                              StringUtils.join(topics, ","), SemanticAttributes.MESSAGING_SYSTEM,
                                              "kafka");
        Meter meter = TelicentMetrics.getMeter(Library.NAME);
        this.pollTimingMetric = meter.histogramBuilder(KafkaMetricNames.POLL_TIMING)
                                     .setDescription(KafkaMetricNames.POLL_TIMING_DESCRIPTION)
                                     .setUnit("seconds")
                                     .build();
        this.fetchCountsMetric = meter.histogramBuilder(KafkaMetricNames.FETCH_EVENTS_COUNT)
                                      .setDescription(KafkaMetricNames.FETCH_EVENTS_COUNT_DESCRIPTION)
                                      .ofLongs()
                                      .build();
        this.lagMetric = meter.gaugeBuilder(KafkaMetricNames.KAFKA_LAG)
                              .setDescription(KafkaMetricNames.LAG_DESCRIPTION)
                              .ofLongs()
                              .buildWithCallback(measure -> {
                                  if (this.lastObservedLag != null) {
                                      measure.record(this.lastObservedLag, this.metricAttributes);
                                  }
                              });

        // Prepare our periodic actions
        // We use one to log our current read positions, and thus lag, intermittently
        // And another to issue a warning when lag is very low i.e. when we are caught up, or close thereof
        this.positionLogger = new PeriodicAction(() -> {
            this.topics.forEach(this.readPolicy::logReadPositions);
            this.lastObservedLag = this.remaining();
        }, lagReportInterval);
        this.lagWarning = new PeriodicAction(() -> topics.stream().anyMatch(topic -> {
            Long lag = readPolicy.currentLag(topic);
            if (lag != null && lag < maxPollRecords && lag > 0) {
                LOGGER.warn(
                        "Only able to buffer {} new events when configured to buffer a max of {} events.  Application performance is being reduced by a slower upstream producer writing to {}",
                        events.size(), maxPollRecords, topic);
                return true;
            }
            return false;
        }), lagReportInterval);
    }

    /**
     * Creates the actual Kafka Admin Client used for the topic existence check
     * <p>
     * An implementation may choose to return {@code null} in which case the topic existence check is disabled for this
     * event source.
     * </p>
     *
     * @param props Client configuration
     * @return Admin client, or {@code null} to disable functionality that depends on the admin client
     */
    protected AdminClient createAdminClient(Properties props) {
        return KafkaAdminClient.create(props);
    }

    /**
     * Creates the actual Kafka consumer
     * <p>
     * This primarily exists for test purposes where it is useful to be able to introduce a
     * {@link org.apache.kafka.clients.consumer.MockConsumer}
     * </p>
     *
     * @param props Consumer configuration
     * @return Kafka consumer
     */
    protected Consumer<TKey, TValue> createConsumer(Properties props) {
        return new KafkaConsumer<>(props);
    }

    @Override
    public void close() {
        if (!this.closed) {
            // NB - There's a lot of stuff protected by try-catch blocks here because when asked to close we want to
            //      make sure we clean up and don't leak resources.  However, due to the fact that a KafkaConsumer is
            //      effectively pinned to a thread once it starts consuming events if this is called from a different
            //      thread than the polling thread some of these close actions won't succeed, and we need to still clean
            //      up regardless!

            try {
                if (this.autoCommit) {
                    // Make sure that we have committed our offsets.  When using Kafka's offset management functionality
                    // this will let us resume processing from the correct offset the next time we are run.
                    if (this.events.isEmpty()) {
                        // If there's no buffered events we've consumed everything from our last poll() so can use
                        // Kafka's no argument commitSync() method to just commit offsets based on our last poll()
                        // results
                        this.tryAutoCommit();
                    } else {
                        // Since we have some events buffered we cannot do a simple commitSync() since that would commit
                        // as if we had processed all the buffered events, which we have not!
                        // Instead, we need to do an explicit commit of the next offset(s) we were yet to process, we
                        // automatically track these as the caller polls events from us, so we already know the offsets
                        // to be committed.
                        // Only gotcha here is have to remove any partitions that are no longer assigned to us as
                        // otherwise the commit will fail.
                        performOffsetCommits(this.autoCommitOffsets);
                    }
                }

                // If there were any unprocessed delayed commits (because something called processed() from a background
                // thread) then commit those now
                processDelayedCommits();
            } catch (Throwable e) {
                LOGGER.warn("Error committing offsets during close(): {}", e.getMessage());
            }

            // If using an external offset store update and close it now
            closeExternalOffsetStore();

            try {
                // Stop events ONLY once we've done our commits (if any), otherwise attempting to do our commit
                // operations might actually result in us not committing anything as once events have been stopped the
                // consumer doesn't consider itself subscribed to anything and so may not commit any offsets!
                this.topics.forEach(this.readPolicy::stopEvents);
            } catch (Throwable e) {
                LOGGER.warn("Error stopping topic event consumption: {}", e.getMessage());
            }

            try {
                // Close our topic existence checker as if we've been configured with non-existent topics we could have
                // in-flight checks that need terminating
                this.topicExistenceChecker.close();
            } catch (Throwable e) {
                LOGGER.warn("Error closing topic existence checker: {}", e.getMessage());
            }

            // Close the underlying Kafka classes to release their network resources
            this.consumer.close();
        }
        super.close();
    }

    /**
     * Closes the external offset store, committing any outstanding offsets first.
     */
    private void closeExternalOffsetStore() {
        if (this.externalOffsetStore != null) {
            try {
                this.performExternalOffsetStoreCommits(this.autoCommitOffsets);
                this.externalOffsetStore.close();
            } catch (Throwable e) {
                LOGGER.warn("Failed to close external offset store {}: {}",
                            this.externalOffsetStore.getClass().getCanonicalName(), e.getMessage());
            }
        }
    }

    /**
     * Processes any delayed offset commits i.e. offset commits that happened on a different thread to the one that is
     * calling {@link #poll(Duration)} and thus effectively owns the underlying {@link KafkaConsumer}.  This is
     * necessary because a {@link KafkaConsumer} isn't thread-safe as demonstrated by <a
     * href="https://github.com/Telicent-io/smart-caches-core/issues/135">Issue 135</a>
     */
    private void processDelayedCommits() {
        if (!this.delayedOffsetCommits.isEmpty()) {
            Map<TopicPartition, OffsetAndMetadata> delayedCommits = this.delayedOffsetCommits.poll();
            while (delayedCommits != null) {
                performOffsetCommits(delayedCommits);
                delayedCommits = this.delayedOffsetCommits.poll();
            }
        }
    }

    /**
     * Called when the event source is attempting to commit offsets but determines it can't commit currently as the
     * relevant partitions are not currently assigned to us
     */
    protected void noOffsetsToCommit() {
        LOGGER.warn("Unable to commit offsets as not currently assigned any relevant partitions");
    }

    @Override
    protected Event<TKey, TValue> decodeEvent(ConsumerRecord<TKey, TValue> internalEvent) {
        // If we're currently resetting offsets then we could be trying to decode a previously buffered event so want to
        // discard this and force the consumer to poll() again which should then retrieve from the reset offsets
        if (this.resetInProgress) {
            return null;
        }

        processDelayedCommits();

        if (internalEvent == null) {
            return null;
        }

        if (this.autoCommit) {
            // If we're auto-committing track the next event we would read for each partition so that we can
            // periodically commit our offsets
            // Remember Kafka wants us to commit the next offset to be read so have to add 1 to the offset of the record
            // we're currently reading
            this.autoCommitOffsets.put(new TopicPartition(internalEvent.topic(), internalEvent.partition()),
                                       new OffsetAndMetadata(internalEvent.offset() + 1));
        }

        return new KafkaEvent<>(internalEvent, this);
    }

    @Override
    public Long remaining() {
        List<Long> onTopicRemaining = this.topics.stream().map(this.readPolicy::currentLag).toList();
        if (onTopicRemaining.stream().allMatch(Objects::isNull)) {
            // No topics reported their remaining total so can't report right now
            return null;
        }
        Long actualRemaining = onTopicRemaining.stream().filter(Objects::nonNull).reduce(0L, Long::sum);
        return actualRemaining + events.size();
    }

    /**
     * Tells the Kafka Event Source that the given events are now considered processed.
     * <p>
     * Calling this method causes the committed offsets for the topic partitions this event source is consuming events
     * from to be updated based upon the offsets in the processed events provided.
     * </p>
     * <p>
     * If this event source has been created with auto-commit disabled then this is the <strong>ONLY</strong> time that
     * the offsets will be committed.  Generally you should either have auto-commit enabled and <strong>NEVER</strong>
     * call this method, or have auto-commit disabled in which case you <strong>MUST</strong> call this method as
     * appropriate.
     * </p>
     * <p>
     * If offsets are not committed, either automatically or via invoking this method, then upon application restart the
     * source will produce the same events the application has previously read.
     * </p>
     *
     * @param processedEvents A collection of events that have been processed.
     * @see io.telicent.smart.cache.sources.EventSource#processed(Collection)
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void processed(Collection<Event> processedEvents) {
        // Compute the maximum processed offset for each topic partitions
        Map<TopicPartition, OffsetAndMetadata> commitOffsets = determineCommitOffsetsFromEvents(processedEvents);

        // If we're on a background thread committing offsets will fail so delay committing them instead
        if (this.pollThread != Thread.currentThread()) {
            this.delayedOffsetCommits.add(commitOffsets);
        } else {
            performOffsetCommits(commitOffsets);
        }
    }

    private void performOffsetCommits(Map<TopicPartition, OffsetAndMetadata> commitOffsets) {
        // If we've been configured with an external offset store commit there first
        performExternalOffsetStoreCommits(commitOffsets);

        // If we are no longer assigned a given partition we aren't permitted to commit an offset for it
        commitOffsets.entrySet().removeIf(e -> !this.consumer.assignment().contains(e.getKey()));
        if (!commitOffsets.isEmpty()) {
            try {
                this.consumer.commitSync(commitOffsets);
            } catch (KafkaException e) {
                // If we've been removed from the consumer group then it's an acceptable failure, otherwise throw
                if (isAcceptableCommitFailure(e)) {
                    logAcceptableCommitFailure();
                } else {
                    throw e;
                }
            }
        } else {
            noOffsetsToCommit();
        }
    }

    private void performExternalOffsetStoreCommits(Map<TopicPartition, OffsetAndMetadata> commitOffsets) {
        if (this.externalOffsetStore == null) {
            return;
        }

        try {
            for (Map.Entry<TopicPartition, OffsetAndMetadata> offset : commitOffsets.entrySet()) {
                // Because many consumers can read from the same topic and partition need to make a unique key for
                // the offset store based on the topic, partition and consumer group
                String offsetKey = externalOffsetStoreKey(offset.getKey().topic(), offset.getKey().partition(),
                                                          this.consumerGroup);
                this.externalOffsetStore.saveOffset(offsetKey, offset.getValue().offset());
            }
            this.externalOffsetStore.flush();
        } catch (Throwable e) {
            // Intentionally just ignoring and logging any errors from the external offset store
            LOGGER.warn("Configured external offset store {} failed to store offsets: {}",
                        this.externalOffsetStore.getClass().getCanonicalName(), e.getMessage());
        }
    }

    /**
     * Computes the key for use in storing an offset to an {@link OffsetStore} instance
     *
     * @param topic         Kafka topic name
     * @param partition     Topic partition
     * @param consumerGroup Consumer group ID
     * @return Offset Store key
     */
    public static String externalOffsetStoreKey(String topic, int partition, String consumerGroup) {
        return String.format("%s-%d-%s", topic, partition, consumerGroup);
    }

    /**
     * Given a collection of events, find the offsets that should be committed for each topic
     *
     * @param events Events
     * @return Offsets to commit, may be empty if no Kafka events provided
     */
    @SuppressWarnings("rawtypes")
    public static Map<TopicPartition, OffsetAndMetadata> determineCommitOffsetsFromEvents(Collection<Event> events) {
        if (CollectionUtils.isEmpty(events)) {
            return Collections.emptyMap();
        }
        return determineCommitOffsetsFromRecords(events.stream()
                                                       .filter(e -> e instanceof KafkaEvent)
                                                       .map(e -> ((KafkaEvent) e).getConsumerRecord())
                                                       .toList());
    }

    @SuppressWarnings("rawtypes")
    private static Map<TopicPartition, OffsetAndMetadata> determineCommitOffsetsFromRecords(
            Collection<ConsumerRecord> records) {
        Map<TopicPartition, Long> offsets = new HashMap<>();
        for (ConsumerRecord record : records) {
            TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
            if (offsets.containsKey(topicPartition)) {
                offsets.computeIfPresent(topicPartition, (k, v) -> Math.max(v, record.offset()));
            } else {
                offsets.put(topicPartition, record.offset());
            }
        }

        // Convert into actual offsets to commit to Kafka
        // Note that Kafka expects us to commit the offset of the next record we want to read, so we have to add 1 to
        // the maximum offset processed
        Map<TopicPartition, OffsetAndMetadata> commitOffsets = new HashMap<>();
        for (Map.Entry<TopicPartition, Long> offset : offsets.entrySet()) {
            commitOffsets.put(offset.getKey(), new OffsetAndMetadata(offset.getValue() + 1));
        }
        return commitOffsets;
    }

    @Override
    protected void bufferExhausted() {
        if (!firstRun) {
            // Once the buffer of events has been exhausted tell Kafka we've processed them
            // Don't do this on the first run since we won't have called KafkaConsumer.poll() yet so there's nothing to
            // commit
            if (this.autoCommit) {
                tryAutoCommit();
            }
        } else {
            // This is the point where the consumer is actually connected to Kafka.  It is intentionally delayed to the
            // first time the user calls poll() (and thus calls into this method)
            this.topics.forEach(this.readPolicy::startEvents);

            // Capture the current thread as only this thread will be able to commit offsets, see processed() and
            // processDelayedCommits() for more information
            this.pollThread = Thread.currentThread();

            // If we were asked to reset offsets prior to attempting to read any events we would have delayed applying
            // them due to now being on the polling thread so go ahead and reset them now
            if (this.resetInProgress) {
                this.performOffsetReset(this.delayedOffsetResets);
            }

            // Also add a shutdown hook that will explicitly interrupt the consumer, otherwise if we're currently
            // blocked on a poll() call to the underlying KafkaConsumer we'll block application shutdown up to the
            // callers provided timeout
            Runtime.getRuntime().addShutdownHook(new Thread(new Interrupter(this.consumer)));
        }
        this.firstRun = false;
    }

    /**
     * Tries to automatically commit offsets
     */
    protected void tryAutoCommit() {
        try {
            this.consumer.commitSync();
        } catch (WakeupException e) {
            // Ignore, this is recoverable, likely caused by a previous KafkaConsumer.wakeUp() call
            // We can try again immediately
            try {
                this.consumer.commitSync();
            } catch (KafkaException e1) {
                if (isAcceptableCommitFailure(e1)) {
                    // Acceptable, just issue a warning
                    logAcceptableCommitFailure();
                } else {
                    throw e;
                }
            }
        } catch (KafkaException e) {
            if (isAcceptableCommitFailure(e)) {
                // Acceptable, just issue a warning
                logAcceptableCommitFailure();
            } else {
                throw e;
            }
        }
    }

    /**
     * Logs that a commit failure was considered acceptable
     */
    protected static void logAcceptableCommitFailure() {
        LOGGER.warn(
                "Failed to commit offsets, not currently part of an active group due to partition reassignment.  Some events may be reprocessed as a result.");
    }

    /**
     * Checks whether the commit failure is acceptable
     *
     * @param e Commit failure
     * @return True if acceptable, false otherwise
     */
    protected static boolean isAcceptableCommitFailure(KafkaException e) {
        if (e instanceof CommitFailedException cfEx) {
            // If the consumer got removed from the group, for whatever reason, then this could fail
            // We detect this case by looking at the error message, and if so just issue a warning and continue,
            // since we're likely to be reassigned partitions at a future date
            return CI.contains(cfEx.getMessage(), "not part of an active group");
        } else if (e instanceof RebalanceInProgressException) {
            // If a rebalance is in progress we're not permitted to commit offsets either, again this is recoverable
            // once rebalance completes in a future poll() call
            return true;
        }
        // Any other error is not considered acceptable
        return false;
    }

    private static Duration updateTimeout(long start, Duration timeout) {
        long elapsed = System.currentTimeMillis() - start;
        long remainingTime = timeout.toMillis() - elapsed;
        if (remainingTime <= 0) {
            return null;
        } else {
            return Duration.ofMillis(remainingTime);
        }
    }

    @Override
    protected void tryFillBuffer(Duration timeout) {
        // Buffer up some more events
        ConsumerRecords<TKey, TValue> records;
        try {
            // Don't do any work if none of the topic(s) exist on the Kafka cluster
            long start = System.currentTimeMillis();
            if (!this.topicExistenceChecker.anyTopicExists(timeout)) return;

            // Reduce the timeout by the amount of time we spent waiting for the topic to exist as otherwise we
            // could wait twice our timeout and violate our API contract
            timeout = updateTimeout(start, timeout);
            if (timeout == null) {
                return;
            }

            // If an offset reset is in progress this means we have delayed offset resets submitted by a thread other
            // than the polling thread, apply those now
            if (this.resetInProgress) {
                this.performOffsetReset(this.delayedOffsetResets);
            }

            // Perform the actual Kafka poll() and store the returned ConsumerRecord instances (if any) in our local
            // buffer
            Duration finalTimeout = timeout;
            records = TelicentMetrics.time(this.pollTimingMetric, this.metricAttributes,
                                           () -> this.consumer.poll(finalTimeout));
            if (this.resetInProgress) {
                // If a reset started while we were in a poll() then we should in principal have been interrupted by the
                // KafkaConsumer.wakeup() call and fall into the catch block
                // However, there is a small chance that it could occur between the poll() completing and the result
                // being returned to us.
                // In this scenario the events we just returned are likely not for the correct offsets so don't
                // add these to the buffer, this forces the caller to call EventSource.poll() again at which point we
                // should resolve the offset reset on our next poll() call and return the expected events.
                return;
            }
            for (ConsumerRecord<TKey, TValue> record : records) {
                events.add(record);
            }
            this.fetchCountsMetric.record(events.size(), this.metricAttributes);

            if (events.isEmpty()) {
                LOGGER.debug("Currently no new events available for Kafka topic(s) {}",
                             StringUtils.join(this.topics, ", "));
            } else {
                LOGGER.debug("Buffered {} new events from Kafka topic(s) {}", events.size(),
                             StringUtils.join(this.topics, ", "));

                if (events.size() < this.maxPollRecords) {
                    this.lagWarning.run();
                }
            }

            this.positionLogger.run();

        /*
        These errors are considered recoverable i.e. a subsequent call to this function could successfully fill the
        buffer
        */
        } catch (WakeupException | InterruptException e) {
            LOGGER.debug("Interrupted/woken while polling Kafka for events");
        /*
        The following errors are considered unrecoverable and result in an EventSourceException being thrown

        However, they do represent things that a user can potentially address e.g. Kafka auth settings, topic
        name etc.  Therefore we provide specific logging and error messaging for these.
        */
        } catch (InvalidOffsetException e) {
            LOGGER.error("Kafka Offset Invalid: {}", e.getMessage());
            throw new EventSourceException("Invalid Kafka Offset", e);
        } catch (AuthenticationException | AuthorizationException e) {
            LOGGER.error("Kafka Security Error: {}", e.getMessage());
            throw new EventSourceException("Kafka Security rejected the request", e);
        } catch (RecordDeserializationException e) {
            LOGGER.error("Kafka reported error deserializing record at offset {} in topic {}", e.offset(),
                         e.topicPartition());
            LOGGER.error("Kafka Deserialization Error: ", e);
            LOGGER.error(
                    "Please inspect the Kafka topic {} to determine whether the record is genuinely malformed or if the deserializers are misconfigured",
                    e.topicPartition());
            throw new EventSourceException(
                    String.format("Unable to deserialize Kafka record at offset %,d in topic %s.", e.offset(),
                                  e.topicPartition()), e);
        } catch (IllegalStateException e) {
            LOGGER.error("Not subscribed/assigned any Kafka topics: {}", e.getMessage());
            throw new EventSourceException("Not subscribed/assigned to any Kafka topics", e);
        } catch (InvalidTopicException e) {
            LOGGER.error("Kafka Topic is invalid: {}", e.getMessage());
            throw new EventSourceException("Invalid Kafka topic", e);
        } catch (Throwable e) {
            // Some other error encountered.
            // While there are other errors that Kafka explicitly says poll() might produce none of them represent
            // things that the user can do anything about
            LOGGER.error("Kafka Error: ", e);
            throw new EventSourceException(e);
        }
    }

    /**
     * Resets the Kafka offsets to the given offsets
     * <p>
     * This is used to roll backwards to a previous state to replay some events again, or to roll forwards to a future
     * state to skip over some events.
     * </p>
     *
     * @param offsets Offsets
     */
    public void resetOffsets(Map<TopicPartition, Long> offsets) {
        if (MapUtils.isEmpty(offsets)) {
            LOGGER.warn("Reset offsets called with no offsets data, nothing was reset!");
            return;
        }

        // Check that at least one topic to reset matches the topics we are sourcing events from
        boolean anyTopicsMatch = offsets.keySet().stream().anyMatch(t -> this.topics.contains(t.topic()));
        if (!anyTopicsMatch) {
            LOGGER.warn(
                    "Reset offsets called without any topics that match this sources configured topics, nothing was reset!");
            return;
        }

        this.resetInProgress = true;
        LOGGER.info("Resetting Kafka Offsets in progress...");

        // As KafkaConsumer is not multithreaded if we are called from a thread other than the polling thread, which is
        // quite likely in real application scenarios, then trying to reset from the non-poll thread will just throw an
        // error.
        // So similar to processed() we copy the requested resets into a temporary map and we'll apply them the next
        // time the polling thread is in a position to do so
        if (this.pollThread != Thread.currentThread()) {
            // Delay resets to later
            this.delayedOffsetResets.putAll(offsets);
            LOGGER.info("Only the polling thread may reset offsets, delaying resets until next poll call");

            // Clear out internally buffered events, this forces the next poll() call to call tryFillBuffer() which is
            // where we process our delayed offset resets
            this.events.clear();

            // As the polling thread might currently be blocked in a poll() call want to call wakeup() so we force the
            // ongoing poll() to be interrupted, then the next poll() call will apply the resets.
            // If there isn't a pollThread set yet then no-one has called our poll() method yet and so no need to
            // wakeup() the consumer as it isn't started yet!
            if (this.pollThread != null) {
                this.consumer.wakeup();
            }
        } else {
            // We're on the polling thread so can apply resets immediately
            // No need to wakeup() the consumer here as the fact we're processing this on the polling thread means there
            // can't be a ongoing poll() call
            performOffsetReset(offsets);
        }
    }

    /**
     * Performs the actual offset resets
     *
     * @param offsets Offsets to reset to
     */
    private synchronized void performOffsetReset(Map<TopicPartition, Long> offsets) {
        try {
            // Clear any currently buffered events and any delayed commits/resets, then ask the read policy to reset the
            // offsets
            this.events.clear();
            this.delayedOffsetCommits.clear();
            this.autoCommitOffsets.clear();
            this.readPolicy.resetOffsets(offsets);

            // If the reset was delayed clear those now
            this.delayedOffsetResets.clear();

            LOGGER.info("Successfully reset Kafka Offsets");
        } catch (Throwable e) {
            LOGGER.error("Failed to reset Kafka Offsets: {}", e.getMessage());
            throw new EventSourceException("Failed to reset offsets", e);
        } finally {
            this.resetInProgress = false;
        }
    }

    @Override
    public void interrupt() {
        // Tell the consumer to wakeup in case we're waiting on a poll()
        this.consumer.wakeup();
    }

    @Override
    public String toString() {
        return String.format("%s/%s", this.server, StringUtils.join(this.topics, ","));
    }

    /**
     * A builder for Kafka event sources
     *
     * @param <TKey>   Key type
     * @param <TValue> Value type
     */
    public static class Builder<TKey, TValue> extends
            AbstractKafkaEventSourceBuilder<TKey, TValue, KafkaEventSource<TKey, TValue>, Builder<TKey, TValue>> {

        /**
         * Builders the event source
         *
         * @return Kafka Event Source
         */
        @Override
        public KafkaEventSource<TKey, TValue> build() {
            return new KafkaEventSource<>(this.bootstrapServers, this.topics, this.groupId, this.keyDeserializerClass,
                                          this.valueDeserializerClass, this.maxPollRecords, this.readPolicy,
                                          this.autoCommit, this.externalOffsetStore, this.lagReportInterval,
                                          this.properties);
        }
    }

    /**
     * A runnable that wakes up the consumer
     * <p>
     * This holds only a weak reference to the relevant instances such that we don't prevent their garbage collections
     * once they are no longer needed.  However, we still retain the ability to interrupt them when application shutdown
     * is requested thus allowing our application to shut down in a timely fashion.
     * </p>
     */
    @SuppressWarnings("rawtypes")
    private static final class Interrupter implements Runnable {

        private static final Logger LOGGER = LoggerFactory.getLogger(Interrupter.class);

        private final WeakReference<Consumer> consumerReference;

        /**
         * Creates a new interrupter
         *
         * @param consumer Consumer to hold a weak reference to
         */
        public Interrupter(Consumer consumer) {
            this.consumerReference = new WeakReference<>(consumer);
        }

        @Override
        public void run() {
            // Wake up the consumer if still valid
            Consumer consumer = this.consumerReference.get();
            if (consumer != null) {
                LOGGER.warn("Interrupting the KafkaConsumer due to application shutdown");
                consumer.wakeup();
            }
        }
    }
}
