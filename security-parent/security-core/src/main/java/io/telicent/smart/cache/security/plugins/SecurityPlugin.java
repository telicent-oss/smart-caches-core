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

import io.telicent.smart.cache.security.identity.DefaultIdentityProvider;
import io.telicent.smart.cache.security.Authorizer;
import io.telicent.smart.cache.security.entitlements.Entitlements;
import io.telicent.smart.cache.security.entitlements.EntitlementsParser;
import io.telicent.smart.cache.security.entitlements.EntitlementsProvider;
import io.telicent.smart.cache.security.identity.IdentityProvider;
import io.telicent.smart.cache.security.labels.*;
import io.telicent.smart.cache.security.requests.RequestContext;
import org.apache.commons.lang3.Conversion;
import org.apache.jena.graph.Graph;

/**
 * Interface for security plugins, primarily this provides access to the various interfaces since a plugin may wish to
 * compose itself from multiple components and/or reuse some standard components e.g. {@link DefaultIdentityProvider}
 */
public interface SecurityPlugin {

    /**
     * Record Separator byte used in the schema prefix encoding, see {@link #decodeSchemaPrefix(byte[])} for more
     * details
     */
    byte RECORD_SEPARATOR = 0x1e;

    /**
     * Decodes the schema prefix (if any)
     * <p>
     * The schema prefix is a 4 byte sequence at the start of the data where the first and fourth bytes are
     * {@value #RECORD_SEPARATOR} and the second and third bytes represent an encoded short indicating the schema ID.
     * This is encoded using {@link Conversion#shortToByteArray(short, int, byte[], int, int)} so follows those encoding
     * rules.
     * </p>
     * <p>
     * See {@link #encodeSchemaPrefix(short)} for encoding a schema prefix.
     * </p>
     *
     * @param data Raw label/entitlements data
     * @return Decoded schema ID, or {@code null} if no schema prefix present
     */
    static Short decodeSchemaPrefix(byte[] data) {
        if (data.length >= 4) {
            if (data[0] == SecurityPlugin.RECORD_SEPARATOR && data[3] == SecurityPlugin.RECORD_SEPARATOR) {
                return Conversion.byteArrayToShort(data, 1, (short) 0, 0, 2);
            }
        }
        return null;
    }

    /**
     * Encodes a schema prefix
     * <p>
     * See {@link #decodeSchemaPrefix(byte[])} for details of how prefixes and encoded into bytes
     * </p>
     *
     * @param schema Schema
     * @return Encoded schema prefix
     */
    static byte[] encodeSchemaPrefix(short schema) {
        byte[] prefix = new byte[4];
        prefix[0] = RECORD_SEPARATOR;
        Conversion.shortToByteArray(schema, 0, prefix, 1, 2);
        prefix[3] = RECORD_SEPARATOR;
        return prefix;
    }

    /**
     * Gets the Default Schema Identifier for this plugin
     *
     * @return Schema Identifier
     */
    short defaultSchema();

    /**
     * Indicates whether the plugin supports the given Schema Identifier
     * <p>
     * More advanced plugins <strong>MAY</strong> choose to support multiple label/entitlements schemes in order to
     * allow multiple different security labelling schemas to co-exist within the system.
     * </p>
     *
     * @param schema Schema Identifier
     * @return Whether the plugin supports the given schema
     */
    boolean supportsSchema(short schema);

    /**
     * Gets the identity provider
     *
     * @return Identity Provider
     */
    IdentityProvider identityProvider();

    /**
     * Gets the entitlements parser
     *
     * @return Entitlements parser
     */
    EntitlementsParser entitlementsParser();

    /**
     * Gets the entitlements provider
     *
     * @return Entitlements provider
     */
    EntitlementsProvider entitlementsProvider();

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
    SecurityLabelsApplicator prepareLabelsApplicator(byte[] defaultLabel, Graph labelsGraph) throws
            MalformedLabelsException;

    /**
     * Prepares an authorizer based on the given entitlements
     * <p>
     * The returned instance is scoped to the lifetime of a single user request so implementors should take that into
     * account when implementing their authorizer, see {@link Authorizer#canRead(SecurityLabels)} Javadoc for more
     * details.
     * </p>
     * <p>
     * Note that the {@link io.telicent.smart.cache.security.requests.RequestContext} is not provided here as
     * applications should already have supplied that when using the
     * {@link EntitlementsProvider#entitlementsForUser(RequestContext)} method.  If applications need to make further
     * fine-grained API/business logic authorization decisions during the processing of a request they can use the
     * {@link Authorizer#canUse(SecurityLabels, RequestContext)} method supplying the original/new request context as
     * appropriate.
     * </p>
     *
     * @param entitlements User entitlements
     * @return Authorizer
     */
    Authorizer prepareAuthorizer(Entitlements<?> entitlements);

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
