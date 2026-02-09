package io.telicent.smart.cache.security.plugins.rdf.abac;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RdfAbac {
    /**
     * Environment variable controlling the label parser cache size for the label parsing cache maintained by the
     * singleton instance of the {@link RdfAbacParser}.
     */
    public static final String ENV_PARSER_CACHE_SIZE = "RDF_ABAC_LABEL_PARSER_CACHE_SIZE";
    /**
     * Environment variable controlling the label parser cache duration for the label parsing cache maintained by the
     * singleton instance of the {@link RdfAbacParser}.
     */
    public static final String ENV_PARSER_CACHE_DURATION = "RDF_ABAC_LABEL_PARSER_CACHE_DURATION";
    /**
     * Environment variable controlling the label evaluation cache size for the label evaluation caches that are
     * maintained by each unique instance of the {@link RdfAbacAuthorizer}
     */
    public static final String ENV_LABEL_EVALUATION_CACHE_SIZE = "RDF_ABAC_LABEL_EVALUATION_CACHE_SIZE";
    /**
     * Default size for the label evaluation cache if not configured via {@link #ENV_LABEL_EVALUATION_CACHE_SIZE}
     */
    public static final int DEFAULT_EVALUATION_CACHE_SIZE = 1_000;
    /**
     * Default size for the label parser cache if not configured via {@link #ENV_PARSER_CACHE_SIZE}
     */
    public static final int DEFAULT_PARSER_CACHE_SIZE = 10_000;
    /**
     * Default initial size for the label parser cache, in practise the initial size is the lesser of this or 1/10th of
     * the configured {@link #DEFAULT_PARSER_CACHE_SIZE}
     */
    public static final int DEFAULT_PARSER_CACHE_MIN_SIZE = 1_000;

    /**
     * Telicent's original RDF-ABAC labels schema is grandfathered in as Schema ID 0
     */
    public static final short SCHEMA = 0;

    /**
     * Shared singleton instance of the Jackson JSON parser used by the plugin
     */
    static final ObjectMapper JSON = new ObjectMapper();
}
