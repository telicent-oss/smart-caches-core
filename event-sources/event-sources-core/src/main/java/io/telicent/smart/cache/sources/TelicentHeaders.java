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

import org.apache.commons.lang3.Strings;

import java.util.function.Predicate;

/**
 * Provides constants related to Telicent's standard event headers
 */
public class TelicentHeaders {


    private TelicentHeaders() {
    }

    /**
     * Event header used to specify the default security labels for data that is not more specifically labelled
     */
    public static final String SECURITY_LABEL = "Security-Label";

    /**
     * Event header used to track the execution path for an event
     */
    public static final String EXEC_PATH = "Exec-Path";

    /**
     * Event header conveying a summary for the reason an event was sent to a dead letter queue (DLQ)
     */
    public static final String DEAD_LETTER_REASON = "Dead-Letter-Reason";

    /**
     * Event header used to identify the ID of the input event that was used to generate an output event
     */
    public static final String INPUT_REQUEST_ID = "Input-Request-ID";

    /**
     * Event header used to uniquely identify a request flowing through the system
     */
    public static final String REQUEST_ID = "Request-ID";

    /**
     * Event header used to identify the content type, similar to HTTP, of the message body.  This is mainly used on our
     * {@code knowledge} topic where we exchange RDF messages and allow for them to be in any RDF serialization that our
     * RDF toolchain - Apache Jena - supports.
     */
    public static final String CONTENT_TYPE = "Content-Type";

    /**
     * Event header used to identify the name of the data source/pipeline that an event originated from
     *
     * @deprecated Replaced by {@link #DISTRIBUTION_ID}
     */
    @Deprecated
    public static final String DATA_SOURCE_NAME = "Data-Source-Name";

    /**
     * Event header used to identify the type of the data source/pipeline that an event originated from
     *
     * @deprecated Replaced by {@link #DISTRIBUTION_ID}
     */
    @Deprecated
    public static final String DATA_SOURCE_TYPE = "Data-Source-Type";

    /**
     * Event header used to identify the source reference for a specific event Replaces deprecated DATA_SOURCE_NAME and
     * DATA_SOURCE_TYPE
     */
    public static final String DATA_SOURCE_REFERENCE = "Data-Source-Reference";

    /**
     * Distribution ID header for the distribution of data being ingested.
     */
    public static final String DISTRIBUTION_ID = "Distribution-Id";

    /**
     * EDH/IDH policy information header for the data being ingested.
     */
    public static final String POLICY_INFORMATION_HEADER = "Policy-Information";

    /**
     * The {@code Split-ID} header added to indicate that an event is a chunk event
     * <p>
     * This provides the correlation ID for recombining the chunked events on the consumer via a
     * {@link io.telicent.smart.cache.sources.combiner.CombiningEventSource} or equivalent
     * </p>
     */
    public static final String SPLIT_ID = "Split-ID";

    /**
     * The {@code Chunk-ID} header added to chunk events to indicate the 1 based index of the chunk relative to the
     * original event
     */
    public static final String CHUNK_ID = "Chunk-ID";
    /**
     * The {@code Chunk-Total} header added to chunk events to indicate the total number of chunks for the original
     * event
     */
    public static final String CHUNK_TOTAL = "Chunk-Total";
    /**
     * The {@code Chunk-Checksum} header added to chunk events to provide integrity checksum for the current chunk,
     * value will be prefixed with algorithm identifier e.g. {@code crc32:}
     */
    public static final String CHUNK_CHECKSUM = "Chunk-Checksum";
    /**
     * The {@code Chunk-Hash} header added to chunk events to provide integrity hash for the current chunk, value will
     * be prefixed with algorithm identifier e.g. {@code sha256:}
     */
    public static final String CHUNK_HASH = "Chunk-Hash";
    /**
     * The {@code Original-Checksum} header added to chunk events to provide the checksum for the original event for
     * verifying integrity after recombination, value will be prefixed with algorithm identifier e.g. {@code crc32:}
     */
    public static final String ORIGINAL_CHECKSUM = "Original-Checksum";
    /**
     * The {@code Original-Hash} header added to chunk events to provide the hash for the original event for verifying
     * integrity after recombination, value will be prefixed with algorithm identifier e.g. {@code sha256:}
     */
    public static final String ORIGINAL_HASH = "Original-Hash";

    /**
     * Predicate for determining whether an event header is a chunk header
     */
    //@formatter:off
    public static final Predicate<? super EventHeader> IS_CHUNK_HEADER =
            h -> Strings.CI.equalsAny(h.key(),
                                      SPLIT_ID,
                                      CHUNK_ID,
                                      CHUNK_TOTAL,
                                      CHUNK_CHECKSUM,
                                      ORIGINAL_CHECKSUM,
                                      CHUNK_HASH,
                                      ORIGINAL_HASH);
    //@formatter:on
}
