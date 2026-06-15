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

/**
 * Shared, dependency-free vocabulary for representing Provenance information as RDF.
 * <p>
 * These are plain {@link String} IRI constants (and small IRI-minting helpers) rather than Apache Jena
 * {@code Resource}/{@code Property} objects, deliberately, so that this vocabulary can live in the common
 * {@code event-sources-core} module without forcing an RDF/Jena dependency onto it. RDF-aware modules (the
 * Fuseki-Kafka connector, the catalogue updater, etc.) construct their own framework objects from these IRIs, ensuring
 * every component emits provenance using the same terms.
 * </p>
 * <p>
 * The model follows W3C PROV-O, aligned with DCAT 3 (the vocabulary already used by the Telicent catalogue), which is
 * the recommended pairing because PROV and DCAT 3 are designed to interoperate. In outline: an ingest <em>Activity</em>
 * (identified by the request id) <em>generates</em> the ingested data <em>Entity</em>, which is <em>derived from</em> a
 * {@code dcat:Distribution}; the activity records the ingest time, security label and execution path.
 * </p>
 */
public final class ProvenanceVocabulary {

    private ProvenanceVocabulary() {
    }

    /** W3C PROV-O namespace. */
    public static final String PROV_NS = "http://www.w3.org/ns/prov#";
    /** W3C DCAT namespace. */
    public static final String DCAT_NS = "http://www.w3.org/ns/dcat#";
    /** Dublin Core terms namespace. */
    public static final String DCTERMS_NS = "http://purl.org/dc/terms/";
    /** XML Schema datatypes namespace. */
    public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema#";

    /** Telicent provenance term/instance namespace (for concepts without a standard equivalent). */
    public static final String TELICENT_PROV_NS = "http://telicent.io/provenance#";
    /** Base IRI for the per-distribution named graph that holds captured provenance. */
    public static final String TELICENT_PROV_GRAPH_BASE = "http://telicent.io/provenance/";

    // ----- PROV-O classes -----
    /** {@code prov:Entity}. */
    public static final String PROV_ENTITY = PROV_NS + "Entity";
    /** {@code prov:Activity}. */
    public static final String PROV_ACTIVITY = PROV_NS + "Activity";

    // ----- PROV-O properties -----
    /** {@code prov:wasGeneratedBy} — links the data entity to the ingest activity. */
    public static final String PROV_WAS_GENERATED_BY = PROV_NS + "wasGeneratedBy";
    /** {@code prov:wasDerivedFrom} — links the data entity to the source distribution. */
    public static final String PROV_WAS_DERIVED_FROM = PROV_NS + "wasDerivedFrom";
    /** {@code prov:generatedAtTime} — the ingest timestamp ({@code xsd:dateTime}). */
    public static final String PROV_GENERATED_AT_TIME = PROV_NS + "generatedAtTime";
    /** {@code prov:wasAttributedTo} — the agent responsible (e.g. the asserting user, for write-back). */
    public static final String PROV_WAS_ATTRIBUTED_TO = PROV_NS + "wasAttributedTo";
    /** {@code prov:used} — an input used by the ingest activity (e.g. the upstream message). */
    public static final String PROV_USED = PROV_NS + "used";

    // ----- DCAT terms -----
    /** {@code dcat:Distribution}. */
    public static final String DCAT_DISTRIBUTION = DCAT_NS + "Distribution";
    /** {@code dcat:DataService}. */
    public static final String DCAT_DATA_SERVICE = DCAT_NS + "DataService";
    /** {@code dcat:accessURL}. */
    public static final String DCAT_ACCESS_URL = DCAT_NS + "accessURL";
    /** {@code dcat:accessService} — links a distribution to the data service that serves it ("how" to reach it). */
    public static final String DCAT_ACCESS_SERVICE = DCAT_NS + "accessService";

    // ----- Dublin Core terms -----
    /** {@code dcterms:source}. */
    public static final String DCTERMS_SOURCE = DCTERMS_NS + "source";
    /** {@code dcterms:identifier}. */
    public static final String DCTERMS_IDENTIFIER = DCTERMS_NS + "identifier";

    // ----- Telicent-specific provenance terms (no direct standard equivalent) -----
    /** Property recording the ABAC security label that applied to the data at ingest. */
    public static final String TELICENT_SECURITY_LABEL = TELICENT_PROV_NS + "securityLabel";
    /** Property recording the raw execution-path header. */
    public static final String TELICENT_EXEC_PATH = TELICENT_PROV_NS + "execPath";
    /** Property recording the request id that produced the data. */
    public static final String TELICENT_REQUEST_ID = TELICENT_PROV_NS + "requestId";
    /** Property recording the input (upstream) request id the data was derived from. */
    public static final String TELICENT_INPUT_REQUEST_ID = TELICENT_PROV_NS + "inputRequestId";

    // ----- IRI minting helpers -----

    /**
     * Mints the IRI for the ingest activity identified by a request id.
     * <p>
     * Callers are responsible for ensuring the identifier is IRI-safe (platform request ids are UUIDs, which are). If
     * identifiers from less controlled sources are ever used, percent-encode them before calling this.
     * </p>
     *
     * @param requestId Request id
     * @return Activity IRI
     */
    public static String activityIri(String requestId) {
        return TELICENT_PROV_NS + "activity/" + requestId;
    }

    /**
     * Mints the IRI for the catalogue distribution identified by a distribution id.
     *
     * @param distributionId Distribution id
     * @return Distribution IRI
     */
    public static String distributionIri(String distributionId) {
        return TELICENT_PROV_NS + "distribution/" + distributionId;
    }

    /**
     * Mints the IRI of the named graph used to hold captured provenance for a given distribution.
     *
     * @param distributionId Distribution id
     * @return Provenance named-graph IRI
     */
    public static String provenanceGraphIri(String distributionId) {
        return TELICENT_PROV_GRAPH_BASE + distributionId;
    }
}
