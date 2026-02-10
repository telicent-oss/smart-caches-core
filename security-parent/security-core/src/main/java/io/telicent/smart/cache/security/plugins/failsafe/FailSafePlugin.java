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
package io.telicent.smart.cache.security.plugins.failsafe;

import io.telicent.smart.cache.security.Authorizer;
import io.telicent.smart.cache.security.attributes.UserAttributes;
import io.telicent.smart.cache.security.attributes.AttributesParser;
import io.telicent.smart.cache.security.attributes.AttributesProvider;
import io.telicent.smart.cache.security.attributes.MalformedAttributesException;
import io.telicent.smart.cache.security.identity.DefaultIdentityProvider;
import io.telicent.smart.cache.security.identity.IdentityProvider;
import io.telicent.smart.cache.security.labels.*;
import io.telicent.smart.cache.security.plugins.SecurityPlugin;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;

/**
 * A fallback plugin that is used if the system detects multiple plugins and doesn't know which should be used, when
 * this plugin is used then all labels are considered invalid and all access requests are rejected.
 * <p>
 * In normal operation this <strong>SHOULD</strong> never get used, only if the system is misconfigured will it be used
 * to put the system into a fail-safe mode.
 * </p>
 */
public class FailSafePlugin implements SecurityPlugin {

    /**
     * Singleton instance of the fail-safe plugin
     */
    public static final FailSafePlugin INSTANCE = new FailSafePlugin();

    /**
     * Error message produced when trying to interact with any part of the API involving user attributes
     */
    public static final String MALFORMED_ATTRIBUTES_FAILSAFE_MESSAGE =
            "Operating in fail-safe mode, all user attributes are considered malformed as we could not load a Security Plugin";
    /**
     * Error message produced when trying to interact with any part of the API involving security labels
     */
    public static final String MALFORMED_LABELS_FAILSAFE_MESSAGE =
            "Operating in fail-safe mode, all labels are considered malformed as we could not load a Security Plugin";

    /**
     * Private constructor to prevent direct instantiation, use {@link #INSTANCE} to access the singleton instance of
     * this plugin
     */
    private FailSafePlugin() {
    }

    @Override
    public IdentityProvider identityProvider() {
        return DefaultIdentityProvider.INSTANCE;
    }

    @Override
    public AttributesParser attributesParser() {
        return raw -> {
            throw malformedAttributes();
        };
    }

    private static MalformedAttributesException malformedAttributes() {
        return new MalformedAttributesException(MALFORMED_ATTRIBUTES_FAILSAFE_MESSAGE);
    }

    @Override
    public AttributesProvider attributesProvider() {
        return context -> {
            throw malformedAttributes();
        };
    }

    @Override
    public SecurityLabelsParser labelsParser() {
        return rawLabels -> {
            throw new MalformedLabelsException(MALFORMED_LABELS_FAILSAFE_MESSAGE);
        };
    }

    @Override
    public SecurityLabelsValidator labelsValidator() {
        return l -> false;
    }

    @Override
    public SecurityLabelsApplicator prepareLabelsApplicator(byte[] defaultLabel, Graph labelsGraph) {
        return new SecurityLabelsApplicator() {
            @Override
            public SecurityLabels<?> labelForTriple(Triple triple) {
                return new RawPrimitive(defaultLabel);
            }

            @Override
            public void close() {
                // No-op
            }
        };
    }

    @Override
    public Authorizer prepareAuthorizer(UserAttributes<?> userAttributes) {
        return FailSafeAuthorizer.INSTANCE;
    }

}
