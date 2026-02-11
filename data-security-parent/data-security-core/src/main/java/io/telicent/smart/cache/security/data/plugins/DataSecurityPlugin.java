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
package io.telicent.smart.cache.security.data.plugins;

import io.telicent.smart.cache.security.data.Authorizer;
import io.telicent.smart.cache.security.data.labels.*;
import io.telicent.smart.cache.security.data.plugins.failsafe.FailSafeAuthorizer;
import io.telicent.smart.cache.security.data.requests.RequestContext;
import org.apache.jena.graph.Graph;

/**
 * Interface for data security plugins, primarily this provides access to the various interfaces since a plugin may wish
 * to compose itself from multiple components and/or reuse some standard components
 */
public interface DataSecurityPlugin {

    /**
     * Gets the labels parser
     *
     * @return Labels parser
     */
    SecurityLabelsParser labelsParser();

    /**
     * Gets the labels validator
     *
     * @return Labels validator
     */
    SecurityLabelsValidator labelsValidator();

    /**
     * Prepares a labels applicator
     *
     * @param defaultLabel The default label to apply if no more specific label applies
     * @param labelsGraph  The labels graph defining fine grained labels
     * @return Labels applicator
     * @throws MalformedLabelsException Thrown if the provided default label is invalid or not supported by this plugin
     */
    SecurityLabelsApplicator prepareLabelsApplicator(byte[] defaultLabel, Graph labelsGraph);

    /**
     * Prepares an authorizer based on the given request context
     * <p>
     * The returned instance is scoped to the lifetime of a single user request so implementors should take that into
     * account when implementing their authorizer, see {@link Authorizer#canRead(SecurityLabels)} Javadoc for more
     * details.
     * </p>
     * <p>
     * In the event that a {@code null} context is provided, or there's insufficient user information to make
     * authorization decisions then a plugin <strong>MUST</strong> return an authorizer that rejects all authorization
     * requests e.g. {@link FailSafeAuthorizer#INSTANCE}.
     * </p>
     *
     * @param context Request context
     * @return Authorizer
     */
    Authorizer prepareAuthorizer(RequestContext context);

    /**
     * Closes the plugin releasing any resources it may be holding
     */
    void close();
}
