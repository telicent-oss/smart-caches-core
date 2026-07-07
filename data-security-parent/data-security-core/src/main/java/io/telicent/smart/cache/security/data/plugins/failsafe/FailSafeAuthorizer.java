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

import io.telicent.smart.cache.security.data.plugins.DataSecurityPlugin;
import io.telicent.smart.cache.security.data.DataAccessAuthorizer;
import io.telicent.smart.cache.security.data.labels.SecurityLabels;
import io.telicent.smart.cache.security.data.requests.RequestContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.sparql.core.DatasetGraph;

import java.util.Optional;

/**
 * An authorizer that denies all access
 * <p>
 * This is intended primarily for use by the {@link FailSafePlugin} but may also be used be other plugins if they are
 * asked to prepare an {@link DataAccessAuthorizer} via
 * {@link DataSecurityPlugin#prepareAuthorizer(RequestContext)} and they are able
 * to pre-determine that a user should have no access up front.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FailSafeAuthorizer implements DataAccessAuthorizer {

    /**
     * Public singleton instance of the fail-safe authorizer that denies all access
     */
    public static final DataAccessAuthorizer INSTANCE = new FailSafeAuthorizer();

    @Override
    public boolean canRead(SecurityLabels<?> labels) {
        return false;
    }

    /**
     * Always returns true so that security is applied by default.
     */
    @Override
    public boolean isSecureDataset(DatasetGraph action) { return true; }

    @Override
    public Optional<DatasetGraph> decideDataset(HttpAction action, DatasetGraph datasetGraph) {
        return Optional.empty();
    }

    @Override
    public void close() {
        // No-op
    }
}
