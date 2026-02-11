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
package io.telicent.smart.cache.security.data.plugins.rdf.abac;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.telicent.jena.abac.AE;
import io.telicent.jena.abac.attributes.AttributeExpr;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.security.data.labels.MalformedLabelsException;
import io.telicent.smart.cache.security.data.labels.SecurityLabels;
import io.telicent.smart.cache.security.data.labels.SecurityLabelsParser;
import io.telicent.smart.cache.security.data.labels.SecurityLabelsValidator;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public class RdfAbacParser implements SecurityLabelsParser, SecurityLabelsValidator {

    private final Cache<String, List<AttributeExpr>> labelParserCache;
    private final int cacheSize;
    private final Duration cacheDuration;

    /**
     * Creates a new parser
     */
    public RdfAbacParser() {
        this.cacheSize = Configurator.get(new String[] { RdfAbac.ENV_PARSER_CACHE_SIZE }, Integer::parseInt,
                                          RdfAbac.DEFAULT_PARSER_CACHE_SIZE);
        this.cacheDuration = Configurator.get(new String[] { RdfAbac.ENV_PARSER_CACHE_DURATION }, Duration::parse,
                                              Duration.ofMinutes(5));
        int initialCacheSize =
                Math.min(RdfAbac.DEFAULT_PARSER_CACHE_MIN_SIZE, Math.max(this.cacheSize, this.cacheSize / 10));
        this.labelParserCache = Caffeine.newBuilder()
                                        .expireAfterAccess(this.cacheDuration)
                                        .initialCapacity(initialCacheSize)
                                        .maximumSize(this.cacheSize)
                                        .build();
    }

    @Override
    public SecurityLabels<List<AttributeExpr>> parseSecurityLabels(byte[] rawLabels) {
        try {
            // Convert byte sequence into a string ignoring schema prefix if present
            String labelStr = getLabelsString(rawLabels);
            return new RdfAbacLabels(rawLabels, this.labelParserCache.get(labelStr, AE::parseExprList));
        } catch (Throwable e) {
            throw new MalformedLabelsException("Failed to parse security labels", e);
        }
    }

    private static String getLabelsString(byte[] rawLabels) {
        return new String(rawLabels, StandardCharsets.UTF_8);
    }

    @Override
    public boolean validate(byte[] rawLabels) {
        try {
            AE.parseExprList(getLabelsString(rawLabels));
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "RdfAbacParser{cacheSize=" + this.cacheSize + ", cacheDuration=" + this.cacheDuration + "}";
    }
}
