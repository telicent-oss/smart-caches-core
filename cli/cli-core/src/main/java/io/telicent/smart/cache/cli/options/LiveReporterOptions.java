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
package io.telicent.smart.cache.cli.options;

import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.NotBlank;
import com.github.rvesse.airline.annotations.restrictions.ranges.IntegerRange;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.live.LiveErrorReporter;
import io.telicent.smart.cache.live.LiveReporter;
import io.telicent.smart.cache.live.LiveReporterBuilder;
import io.telicent.smart.cache.live.TelicentLive;
import io.telicent.smart.cache.live.model.IODescriptor;
import io.telicent.smart.cache.live.model.LiveError;
import io.telicent.smart.cache.live.model.LiveStatus;
import io.telicent.smart.cache.live.serializers.LiveErrorSerializer;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.sinks.NullSink;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.kafka.config.KafkaConfiguration;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.utils.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Options for Telicent Live heartbeat reporting
 */
public class LiveReporterOptions extends KafkaConfigurationOptions {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiveReporterOptions.class);

    private LiveReporter reporter;

    @Option(name = {
            "--live-reporter", "--no-live-reporter"
    }, arity = 0, description = "Sets whether Telicent Live heartbeat reporting is enabled/disabled.")
    boolean enableLiveReporter =
            Configurator.get(CliEnvironmentVariables.ENABLE_LIVE_REPORTER, Boolean::parseBoolean, true);

    @Option(name = "--live-reporter-topic", title = "LiveTopic", arity = 1, description = "Sets the Kafka topic to which Telicent Live heartbeat reports are sent.  Only used if Kafka connection has been suitably configured.")
    @NotBlank
    String liveReportTopic = Configurator.get(new String[] { CliEnvironmentVariables.LIVE_REPORTER_TOPIC },
                                                      LiveReporter.DEFAULT_LIVE_TOPIC);

    @Option(name = "--live-error-topic", title = "LiveErrorTopic", arity = 1, description = "Sets the Kafka topic to which Telicent Live errors are sent.  Only used if Kafka connection has been suitably configured.")
    String liveErrorTopic = Configurator.get(new String[] { CliEnvironmentVariables.LIVE_ERROR_TOPIC },
                                                     LiveErrorReporter.DEFAULT_LIVE_TOPIC);

    @Option(name = {
            "--live-report-interval", "--live-reporter-interval"
    }, title = "LiveReportInterval", arity = 1, description = "Sets the Telicent Live heartbeat reporting interval in seconds i.e. how frequently the application will send a Heartbeat.  Defaults to 15 seconds.")
    @IntegerRange(min = 1, max = 300)
    int liveReportPeriod = Configurator.get(CliEnvironmentVariables.LIVE_REPORTER_INTERVAL, Integer::parseInt,
                                                    LiveReporter.DEFAULT_REPORTING_PERIOD_SECONDS);

    @Option(name = {
            "--live-bootstrap-server", "--live-bootstrap-servers"
    }, title = "LiveBootstrapServers", description = "Provides a comma separated list of bootstrap servers to use for creating the initial connection to Kafka.  For commands that connect to Kafka anyway this option is unnecessary provided the Kafka source is configured via the --bootstrap-servers option, however for commands that don't require a Kafka connection normally this option is required for the Live Heartbeats to be reported correctly.")
    String liveBootstrapServers =
            Configurator.get(new String[] { CliEnvironmentVariables.LIVE_BOOTSTRAP_SERVERS, KafkaConfiguration.BOOTSTRAP_SERVERS });

    /**
     * Sets up and starts the Telicent {@link LiveReporter} if appropriately configured
     *
     * @param bootstrapServers Kafka Bootstrap Servers, if {@code null}/blank then Live Reporting will not be sent to
     *                         Kafka unless the {@code --live-reporter-bootstrap-servers} option was provided
     * @param name             Application name
     * @param id               Application ID
     * @param componentType    Component Type
     * @param input            Input descriptor
     * @param output           Output descriptor
     */
    public void setupLiveReporter(String bootstrapServers, String name, String id, String componentType,
                                  IODescriptor input, IODescriptor output) {
        if (!this.enableLiveReporter) {
            warnLiveReportingDisabled();
            return;
        }

        //@formatter:off
        LiveReporterBuilder builder = LiveReporter.create()
                                                  .name(name)
                                                  .id(id)
                                                  .componentType(componentType)
                                                  .reportingPeriod(Duration.ofSeconds(this.liveReportPeriod))
                                                  .input(input)
                                                  .output(output);
        //@formatter:on
        if (StringUtils.isNotBlank(this.liveBootstrapServers)) {
            logLiveReportingLocation(this.liveBootstrapServers);
            builder = builder.toKafka(k -> k.bootstrapServers(this.liveBootstrapServers)
                                            .topic(this.liveReportTopic)
                                            .producerConfig(this.getAdditionalProperties()));
        } else if (StringUtils.isNotBlank(bootstrapServers)) {
            logLiveReportingLocation(bootstrapServers);
            builder = builder.toKafka(k -> k.bootstrapServers(bootstrapServers)
                                            .topic(this.liveReportTopic)
                                            .producerConfig(this.getAdditionalProperties()));
        }

        this.reporter = builder.build();
        reporter.start();
    }

    private void logLiveReportingLocation(String bootstrapServers) {
        LOGGER.info("Telicent Live Heartbeat Reporting going to Kafka topic {} @ {}", this.liveReportTopic,
                    bootstrapServers);
    }

    /**
     * Tears down the Telicent {@link LiveReporter} that was previously created (via
     * {@link #setupLiveReporter(String, String, String, String, IODescriptor, IODescriptor)}) stopping it with the
     * given stop status
     * <p>
     * Generally it may be better to call {@link #teardown(LiveStatus)} as that will handle any unexpected errors that
     * occur during teardown.
     * </p>
     *
     * @param stopStatus Stop Status
     * @deprecated Avoid direct usage, call {@link #teardown(LiveStatus)} instead
     */
    @Deprecated
    public void teardownLiveReporter(LiveStatus stopStatus) {
        if (this.reporter != null) {
            this.reporter.stop(stopStatus);
        }
    }

    /**
     * Sets up the Telicent {@link LiveErrorReporter} if appropriately configured
     *
     * @param bootstrapServers Kafka bootstrap servers, if {@code null}/blank then Live Reporting will not be sent to
     *                         Kafka unless the {@code --live-reporter-bootstrap-servers} option was provided
     * @param id               Application ID
     */
    public void setupErrorReporter(String bootstrapServers, String id) {
        if (!this.enableLiveReporter) {
            warnLiveReportingDisabled();
            return;
        }

        Sink<Event<Bytes, LiveError>> sink;
        if (StringUtils.isAllBlank(this.liveBootstrapServers, bootstrapServers)) {
            sink = NullSink.of();
        } else {
            String kafkaServers =
                    StringUtils.isNotBlank(this.liveBootstrapServers) ? this.liveBootstrapServers : bootstrapServers;
            LOGGER.info("Telicent Live Error Reporting going to Kafka topic {} @ {}", this.liveErrorTopic,
                        kafkaServers);
            //@formatter:off
            sink = KafkaSink.<Bytes, LiveError>create()
                            .bootstrapServers(kafkaServers)
                            .topic(this.liveErrorTopic)
                            .keySerializer(BytesSerializer.class)
                            .valueSerializer(LiveErrorSerializer.class)
                            .producerConfig(this.getAdditionalProperties())
                            .build();
            //@formatter:on
        }

        LiveErrorReporter errorReporter = LiveErrorReporter.create().id(id).destination(sink).build();
        TelicentLive.setErrorReporter(errorReporter);
    }

    private static void warnLiveReportingDisabled() {
        LOGGER.warn("Telicent Live Reporting explicitly disabled by user");
    }

    /**
     * Tears down the {@link LiveErrorReporter} that was previously created via a call to
     * {@link #setupErrorReporter(String, String)}
     * <p>
     * Generally it may be better to call {@link #teardown(LiveStatus)} as that will handle any unexpected errors that
     * occur during teardown.
     * </p>
     *
     * @deprecated Avoid direct usage, call {@link #teardown(LiveStatus)} instead
     */
    @Deprecated
    public void teardownErrorReporter() {
        if (TelicentLive.getErrorReporter() != null) {
            TelicentLive.getErrorReporter().close();
        }
    }

    /**
     * Tears down the configured reporters (if any)
     * <p>
     * This method intentionally suppresses any errors that might occur while trying to tear things down as those errors
     * could have the effect of hiding the real cause of the shutdown and preven correct troubleshooting of the issue.
     * </p>
     *
     * @param stopStatus Stop status for the final Live Heartbeat
     */
    public void teardown(LiveStatus stopStatus) {
        try {
            this.teardownLiveReporter(stopStatus);
        } catch (Throwable e) {
            // Log and ignore
            LOGGER.warn("Unexpected error tearing down Telicent Live Heartbeat reporting: {}", e.getMessage());
        }
        try {
            this.teardownErrorReporter();
        } catch (Throwable e) {
            // Log and ignore
            LOGGER.warn("Unexpected error tearing down Telicent Live Error reporting: {}", e.getMessage());
        }
    }
}
