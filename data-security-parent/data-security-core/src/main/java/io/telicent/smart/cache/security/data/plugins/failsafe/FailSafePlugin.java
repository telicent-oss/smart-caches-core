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
package io.telicent.smart.cache.security.data.plugins.failsafe;

import io.telicent.smart.cache.security.data.Authorizer;
import io.telicent.smart.cache.security.data.labels.*;
import io.telicent.smart.cache.security.data.plugins.DataSecurityPlugin;
import io.telicent.smart.cache.security.data.requests.RequestContext;
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
public class FailSafePlugin implements DataSecurityPlugin {

    /**
     * Singleton instance of the fail-safe plugin
     */
    public static final FailSafePlugin INSTANCE = new FailSafePlugin();

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
    public Authorizer prepareAuthorizer(RequestContext context) {
        return FailSafeAuthorizer.INSTANCE;
    }

    @Override
    public void close() {
        // No-op
    }

}
