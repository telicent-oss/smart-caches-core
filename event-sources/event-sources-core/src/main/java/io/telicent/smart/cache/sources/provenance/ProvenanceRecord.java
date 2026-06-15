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
package io.telicent.smart.cache.sources.provenance;

import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.TelicentHeaders;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * An immutable, dependency-free snapshot of the provenance/audit information carried by an {@link Event} as it is
 * ingested into the platform.
 * <p>
 * The platform's Kafka messages carry a number of provenance and audit headers (see {@link TelicentHeaders}) describing how
 * a message has flowed through the system. These headers are normally consumed and then discarded during ingest, which
 * means the provenance cannot be queried after the fact. This record captures those values at the point of ingest so
 * that downstream, RDF-aware components (for example the Fuseki-Kafka connector or the catalogue updater) can persist
 * them as PROV-O / DCAT triples alongside the data they describe.
 * </p>
 * <p>
 * This type intentionally has <strong>no RDF dependency</strong>. It simply holds the captured header values plus the
 * ingest timestamp. Conversion into RDF is the responsibility of the RDF-aware modules, which can use
 * {@link ProvenanceVocabulary} for a consistent vocabulary. Any field may be {@code null} if the corresponding header
 * was absent — provenance headers are optional in the platform.
 * </p>
 *
 * @param requestId      Value of the {@link TelicentHeaders#REQUEST_ID} header — uniquely identifies the request that
 *                       produced this message. May be {@code null}.
 * @param inputRequestId Value of the {@link TelicentHeaders#INPUT_REQUEST_ID} header — the id of the upstream message
 *                       this one was derived from. May be {@code null}.
 * @param execPath       Value of the {@link TelicentHeaders#EXEC_PATH} header — the list of services the message has
 *                       traversed, as the raw header string. May be {@code null}. See {@link #execPathSteps()}.
 * @param distributionId Value of the {@link TelicentHeaders#DISTRIBUTION_ID} header — links the data back to its
 *                       catalogue (DCAT) distribution. May be {@code null}.
 * @param securityLabel  Value of the {@link TelicentHeaders#SECURITY_LABEL} header — the default ABAC label applied to
 *                       the data. May be {@code null}.
 * @param ingestedAt     The instant at which this provenance was captured (i.e. when the data was ingested). Never
 *                       {@code null}.
 */
public record ProvenanceRecord(String requestId, String inputRequestId, String execPath, String distributionId,
                               String securityLabel, Instant ingestedAt) {

    /**
     * Canonical constructor.
     *
     * @throws NullPointerException if {@code ingestedAt} is {@code null}
     */
    public ProvenanceRecord {
        Objects.requireNonNull(ingestedAt, "ingestedAt cannot be null");
    }

    /**
     * Captures the provenance headers from the given event, timestamped with the current instant.
     *
     * @param event Event to capture provenance from
     * @return Provenance record
     * @throws NullPointerException if {@code event} is {@code null}
     */
    public static ProvenanceRecord fromEvent(Event<?, ?> event) {
        return fromEvent(event, Instant.now());
    }

    /**
     * Captures the provenance headers from the given event using a caller-supplied ingest timestamp.
     * <p>
     * The explicit-timestamp overload exists primarily so that callers operating on a batch of events can apply a single
     * consistent ingest time, and so that tests are deterministic.
     * </p>
     *
     * @param event      Event to capture provenance from
     * @param ingestedAt The ingest timestamp to record
     * @return Provenance record
     * @throws NullPointerException if {@code event} or {@code ingestedAt} is {@code null}
     */
    public static ProvenanceRecord fromEvent(Event<?, ?> event, Instant ingestedAt) {
        Objects.requireNonNull(event, "event cannot be null");
        return new ProvenanceRecord(event.lastHeader(TelicentHeaders.REQUEST_ID),
                                    event.lastHeader(TelicentHeaders.INPUT_REQUEST_ID),
                                    event.lastHeader(TelicentHeaders.EXEC_PATH),
                                    event.lastHeader(TelicentHeaders.DISTRIBUTION_ID),
                                    event.lastHeader(TelicentHeaders.SECURITY_LABEL), ingestedAt);
    }

    /**
     * Splits the {@link #execPath()} header into its individual steps.
     * <p>
     * NOTE: a comma is assumed to be the delimiter between steps. This matches the common rendering but
     * <strong>should be confirmed against the canonical {@code telicent-lib} provenance format</strong> before this is
     * relied upon in production; if the delimiter differs this is the single place to change it.
     * </p>
     *
     * @return Ordered list of execution-path steps, trimmed and with empty entries removed; empty if no exec path is
     * present
     */
    public List<String> execPathSteps() {
        if (this.execPath == null || this.execPath.isBlank()) {
            return List.of();
        }
        return Arrays.stream(this.execPath.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    /**
     * Indicates whether any provenance information was actually present on the event.
     *
     * @return {@code true} if at least one provenance header was captured, {@code false} if none were present
     */
    public boolean hasProvenance() {
        return this.requestId != null || this.inputRequestId != null || this.execPath != null
               || this.distributionId != null || this.securityLabel != null;
    }
}