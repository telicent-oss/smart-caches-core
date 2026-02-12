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
package io.telicent.smart.caches.configuration.auth;

import io.telicent.servlet.auth.jwt.JwtHttpConstants;
import io.telicent.servlet.auth.jwt.verifier.aws.AwsConstants;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides constants related to configuring authentication
 */
public class AuthConstants {
    /**
     * Private constructor prevents instantiation
     */
    private AuthConstants() {
    }

    /**
     * Environment variable that specifies the User Attributes service URL used to obtain attributes for an
     * authenticated user
     * @deprecated Replaced by {@link #ENV_USERINFO_URL}
     */
    @Deprecated(forRemoval = true)
    public static final String ENV_USER_ATTRIBUTES_URL = "USER_ATTRIBUTES_URL";

    /**
     * Environment variable that specifies the Attributes Hierarchy service URL to obtain attributes hierarchies from,
     * attribute hierarchies are used in evaluating whether user attributes meet the security labels on data
     * @deprecated No longer used
     */
    @Deprecated(forRemoval = true)
    public static final String ENV_ATTRIBUTE_HIERARCHY_URL = "ATTRIBUTE_HIERARCHY_URL";

    /**
     * Environment variable that specifies the JSON Web Key Set (JWKS) URL to use to obtain the public keys for
     * verifying JSON Web Tokens (JWTs)
     */
    public static final String ENV_JWKS_URL = "JWKS_URL";

    /**
     * Environment variable that specifies the User Info URL from which the application can obtain User Info for
     * authenticated users
     */
    public static final String ENV_USERINFO_URL = "USERINFO_URL";

    /**
     * Special value used for the {@value #ENV_JWKS_URL} environment variable to indicate that authentication is
     * disabled.
     */
    public static final String AUTH_DISABLED = "disabled";

    /**
     * Prefix for special values to the {@value #ENV_JWKS_URL} environment variable to indicate that authentication is
     * using AWS ELB where the rest of the value will indicate the AWS region we are deployed in
     */
    public static final String AUTH_PREFIX_AWS = "aws:";

    /**
     * Temporary feature flag environment variable that allows for disabling the new authorization policy support.
     * <p>
     * This allows us to update applications built against version 0.30.0 of these libraries to have authorization
     * policies defined on their endpoints <strong>but</strong> still be able to deploy these into our existing clusters
     * where the new Telicent Auth server, which is required to enforce these policies, is not yet available.
     * </p>
     * <p>
     * The flag defaults to on so that as we upgrade applications to define their authorization policies we ensure that
     * any existing unit and integration tests are appropriately updated so that they pass the policy checks.  Since in
     * test environment our {@code MockKeyServer} provides the ability to mock the new Telicent Auth server.
     * </p>
     *
     * @since 0.30.0
     * @deprecated Authorization is always on in 1.0.0 onwards unless Authentication is disabled
     */
    @Deprecated(forRemoval = true)
    public static final String FEATURE_FLAG_AUTHORIZATION = "FEATURE_FLAG_AUTHZ";

    /**
     * The default set of Authentication header names from which a JWT may be extracted
     */
    public static final String DEFAULT_AUTH_HEADER_NAMES =
            StringUtils.joinWith(",", JwtHttpConstants.HEADER_AUTHORIZATION, AwsConstants.HEADER_DATA);
    /**
     * The default set of Authentication header prefixes used in conjunction with the header names to control how JWTs
     * are extracted from headers
     *
     * @see #DEFAULT_AUTH_HEADER_NAMES
     */
    public static final String DEFAULT_AUTH_HEADER_PREFIXES =
            StringUtils.joinWith(",", JwtHttpConstants.AUTH_SCHEME_BEARER, "");

    /**
     * The default set of JWT claims that will be inspected to find the username
     */
    public static final String DEFAULT_USERNAME_CLAIMS =
            StringUtils.joinWith(",", "preferred_name", "email", "username");

    /**
     * The default JWT roles claim that will be inspected to find the users roles
     */
    public static final String DEFAULT_ROLES_CLAIM = "roles";

    /**
     * Calculates the Hierarchy Lookup URL based upon the configured User Attributes URL
     * <p>
     * This assumes that the user attributes server is Telicent Access and thus follows the URL patterns that it uses.
     * Given that assumption and knowing the user attributes URL we can calculate what the hierarchy URL should be.
     * </p>
     *
     * @param attributesUrl Attributes URL
     * @return Hierarchy Lookup URL
     * @deprecated No longer used as Telicent Access is deprecated
     */
    @Deprecated(forRemoval = true)
    public static String calculateHierarchyLookupUrl(String attributesUrl) {
        if (attributesUrl == null) {
            return null;
        }
        return attributesUrl.replaceFirst("/users?/", "/hierarchies/").replaceFirst("\\{user}", "{name}");
    }
}
