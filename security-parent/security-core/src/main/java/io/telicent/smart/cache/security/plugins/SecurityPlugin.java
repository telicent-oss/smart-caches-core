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
package io.telicent.smart.cache.security.plugins;

import io.telicent.smart.cache.security.Authorizer;
import io.telicent.smart.cache.security.attributes.UserAttributes;
import io.telicent.smart.cache.security.attributes.AttributesParser;
import io.telicent.smart.cache.security.attributes.AttributesProvider;
import io.telicent.smart.cache.security.labels.*;
import io.telicent.smart.cache.security.requests.RequestContext;
import org.apache.jena.graph.Graph;

/**
 * Interface for security plugins, primarily this provides access to the various interfaces since a plugin may wish to
 * compose itself from multiple components and/or reuse some standard components
 */
public interface SecurityPlugin {

    /**
     * Gets the attributes parser
     *
     * @return Attributes parser
     */
    AttributesParser attributesParser();

    /**
     * Gets the user attributes provider
     *
     * @return User attributes provider
     */
    AttributesProvider attributesProvider();

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
     * Prepares an authorizer based on the given user attributes
     * <p>
     * The returned instance is scoped to the lifetime of a single user request so implementors should take that into
     * account when implementing their authorizer, see {@link Authorizer#canRead(SecurityLabels)} Javadoc for more
     * details.
     * </p>
     * <p>
     * Note that the {@link io.telicent.smart.cache.security.requests.RequestContext} is not provided here as
     * applications should already have supplied that when using the
     * {@link AttributesProvider#attributesForUser(RequestContext)} method.
     * </p>
     *
     * @param userAttributes User attributes
     * @return Authorizer
     */
    Authorizer prepareAuthorizer(UserAttributes<?> userAttributes);

    /**
     * Closes the plugin releasing any resources it may be holding
     * <p>
     * Only needs to be overridden if the plugin may be holding any resources that need freeing e.g. HTTP connection
     * pool.
     * </p>
     */
    default void close() {
    }
}
