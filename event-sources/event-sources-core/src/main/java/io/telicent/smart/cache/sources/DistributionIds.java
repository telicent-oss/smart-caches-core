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
package io.telicent.smart.cache.sources;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Helpers for reading and writing the Distribution ID carried by an event.
 * <p>
 * Historically the Distribution ID was carried solely in the {@value TelicentHeaders#DISTRIBUTION_ID} header.  The
 * Core Data Management design additionally requires it to be used as the Kafka message key so that a distribution's
 * events are partitioned together, while retaining the header for backwards compatibility with existing pipelines and
 * services.
 * </p>
 * <p>
 * On the read side <strong>the key is authoritative where it can be trusted to convey a Distribution ID</strong>.
 * A message key cannot be self-describing - a key that is not a Distribution ID, e.g. a document ID, is
 * indistinguishable from one that is - so the key and the {@value TelicentHeaders#DISTRIBUTION_ID} header are
 * reconciled rather than the key being taken on faith.  See {@link #resolve(Event)} and
 * {@link #reconcile(String, String)}.
 * </p>
 * <p>
 * This class deliberately understands keys expressed as {@code byte[]} or {@link CharSequence} only.  Kafka's
 * {@code Bytes} type lives in a module that {@code event-sources-core} does not depend upon, so code working with
 * {@code Event<Bytes, ?>} should call {@code KafkaDistributionKeys} from the {@code event-source-kafka} module
 * instead, which handles that type and delegates here for everything else.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DistributionIds {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributionIds.class);

    /**
     * The separator used between the Distribution ID and the uniqueness suffix in a composite message key, as
     * produced by {@link DistributionKeyStrategy#DISTRIBUTION_ID_AND_UUID}
     */
    public static final String KEY_SEPARATOR = "/";

    /**
     * Length of the canonical string form of a UUID, i.e. {@code 00000000-0000-0000-0000-000000000000}
     */
    private static final int UUID_STRING_LENGTH = 36;

    /**
     * Decodes a raw message key into the Distribution ID it conveys, if any.
     * <p>
     * The key is decoded as strict UTF-8.  A key that is not valid UTF-8 is <strong>not</strong> a Distribution ID
     * key, e.g. it may be a legacy key from before this convention was adopted, and {@code null} is returned so that
     * callers fall back to the {@value TelicentHeaders#DISTRIBUTION_ID} header.
     * </p>
     *
     * @param rawKey Raw message key bytes, may be {@code null}
     * @return Distribution ID, or {@code null} if the key does not convey one
     */
    public static String fromKeyBytes(byte[] rawKey) {
        if (rawKey == null || rawKey.length == 0) {
            return null;
        }
        // NB - Deliberately strict.  A lenient decode would silently turn arbitrary legacy binary keys into
        //      replacement characters and we would then treat that garbage as a Distribution ID.
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                                                       .onMalformedInput(CodingErrorAction.REPORT)
                                                       .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            CharBuffer decoded = decoder.decode(ByteBuffer.wrap(rawKey));
            return fromKeyString(decoded.toString());
        } catch (CharacterCodingException e) {
            LOGGER.debug(
                    "Message key is not valid UTF-8 so does not convey a Distribution ID, falling back to the {} header",
                    TelicentHeaders.DISTRIBUTION_ID);
            return null;
        }
    }

    /**
     * Decodes a message key that is already in string form into the Distribution ID it conveys, if any.
     * <p>
     * Both key forms produced by {@link DistributionKeyStrategy} are understood.  Where the key ends in
     * {@code /<uuid>}, i.e. the composite form, that suffix is stripped, otherwise the whole key is the Distribution
     * ID.  Note that Distribution IDs are frequently URIs and therefore routinely contain {@value #KEY_SEPARATOR}
     * themselves, so only a final segment that actually parses as a UUID is treated as a uniqueness suffix.
     * </p>
     *
     * @param key Message key in string form, may be {@code null}
     * @return Distribution ID, or {@code null} if the key does not convey one
     */
    public static String fromKeyString(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        int separator = key.lastIndexOf(KEY_SEPARATOR);
        if (separator > 0 && key.length() - separator - 1 == UUID_STRING_LENGTH && isUuid(
                key.substring(separator + 1))) {
            return key.substring(0, separator);
        }
        return key;
    }

    /**
     * Decodes a message key into the Distribution ID it conveys, if any.
     * <p>
     * Keys expressed as {@code byte[]} or {@link CharSequence} are understood, any other type, including {@link UUID}
     * keys as used by the Distribution Lifecycle and Action Tracker topics, yields {@code null} so that callers fall
     * back to the {@value TelicentHeaders#DISTRIBUTION_ID} header.
     * </p>
     *
     * @param key Message key, may be {@code null}
     * @return Distribution ID, or {@code null} if the key does not convey one
     */
    public static String fromKey(Object key) {
        if (key == null) {
            return null;
        } else if (key instanceof byte[] rawKey) {
            return fromKeyBytes(rawKey);
        } else if (key instanceof CharSequence sequence) {
            return fromKeyString(sequence.toString());
        }
        return null;
    }

    /**
     * Resolves the Distribution ID for an event by reconciling its message key with its
     * {@value TelicentHeaders#DISTRIBUTION_ID} header, see {@link #reconcile(String, String)} for the rule applied.
     * <p>
     * See the class documentation for the caveat about {@code Bytes} keys.
     * </p>
     *
     * @param event Event, may be {@code null}
     * @return Distribution ID, or {@code null} if the event does not carry one
     */
    public static String resolve(Event<?, ?> event) {
        if (event == null) {
            return null;
        }
        return reconcile(fromKey(event.key()), fromHeader(event));
    }

    /**
     * Reconciles a Distribution ID decoded from a message key with one read from the
     * {@value TelicentHeaders#DISTRIBUTION_ID} header.
     * <p>
     * The key is authoritative <strong>where the two agree, or where there is no header to check it against</strong>:
     * </p>
     * <ul>
     *     <li>Only one of the two present - that one is used.</li>
     *     <li>Both present and equal - the shared value is used.  This is the steady state for a pipeline that has
     *     adopted message keys, since producers write the key and the header together.</li>
     *     <li>Both present and different - <strong>the header is used</strong>.</li>
     * </ul>
     * <p>
     * That last rule is the important one.  A message key cannot say whether it is a Distribution ID: a key carrying
     * a document ID, a subject URI or any other identifier decodes to a perfectly well formed string and is
     * indistinguishable from a Distribution ID key.  Taking such a key on faith silently attributes the event to a
     * distribution that does not exist, and because lifecycle decisions are made from this value the event is then
     * misjudged rather than failing loudly.  A producer that has adopted message keys always writes the header to
     * match, so a disagreement means the key is not a Distribution ID key - not that the header is stale.
     * </p>
     *
     * @param fromKey    Distribution ID decoded from the message key, may be {@code null}
     * @param fromHeader Distribution ID read from the header, may be {@code null}
     * @return Distribution ID, or {@code null} if neither conveys one
     */
    public static String reconcile(String fromKey, String fromHeader) {
        if (fromKey == null) {
            return fromHeader;
        }
        if (fromHeader == null) {
            return fromKey;
        }
        if (fromKey.equals(fromHeader)) {
            return fromKey;
        }
        // NB - Deliberately DEBUG and not WARN: this is on the per event hot path, and a topic that predates message
        //      keys produces one of these for every single event it carries.
        LOGGER.debug(
                "Message key conveys '{}' but the {} header says '{}', so the key is not a Distribution ID key; using the header",
                fromKey, TelicentHeaders.DISTRIBUTION_ID, fromHeader);
        return fromHeader;
    }

    /**
     * Reads the Distribution ID from an event's {@value TelicentHeaders#DISTRIBUTION_ID} header, ignoring its message
     * key entirely
     *
     * @param event Event, may be {@code null}
     * @return Distribution ID, or {@code null} if the event has no such header
     */
    public static String fromHeader(Event<?, ?> event) {
        if (event == null) {
            return null;
        }
        return StringUtils.trimToNull(event.lastHeader(TelicentHeaders.DISTRIBUTION_ID));
    }

    /**
     * Generates the message key for a Distribution ID using the given strategy
     *
     * @param distributionId Distribution ID
     * @param strategy       Key strategy, if {@code null} then {@link DistributionKeyStrategy#DEFAULT} is used
     * @return Message key, or {@code null} if no Distribution ID was supplied
     */
    public static String toKey(String distributionId, DistributionKeyStrategy strategy) {
        return (strategy != null ? strategy : DistributionKeyStrategy.DEFAULT).key(distributionId);
    }

    /**
     * Generates the message key bytes for a Distribution ID using the given strategy
     *
     * @param distributionId Distribution ID
     * @param strategy       Key strategy, if {@code null} then {@link DistributionKeyStrategy#DEFAULT} is used
     * @return Message key bytes, or {@code null} if no Distribution ID was supplied
     */
    public static byte[] toKeyBytes(String distributionId, DistributionKeyStrategy strategy) {
        String key = toKey(distributionId, strategy);
        return key != null ? key.getBytes(StandardCharsets.UTF_8) : null;
    }

    private static boolean isUuid(String candidate) {
        try {
            // NB - UUID.fromString() is lenient about segment lengths, e.g. it accepts "1-1-1-1-1", so callers also
            //      check the candidate is exactly the canonical 36 characters long.
            UUID.fromString(candidate);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
