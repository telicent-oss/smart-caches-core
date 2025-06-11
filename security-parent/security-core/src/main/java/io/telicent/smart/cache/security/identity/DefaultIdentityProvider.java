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
package io.telicent.smart.cache.security.identity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.RequiredTypeException;
import io.telicent.servlet.auth.jwt.configuration.ConfigurationParameters;
import io.telicent.servlet.auth.jwt.configuration.Utils;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.caches.configuration.auth.AuthConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The default implementation of an identity provider
 * <p>
 * This returns the value of the first non-empty claim from the verified JWT based on a configurable list of claims,
 * defaulting to {@link AuthConstants#DEFAULT_USERNAME_CLAIMS} if no explicit configuration.  If none of the configured
 * claims contains a non-empty value then falls back to the {@code sub} (subject) claim from the JWT specifications.
 * </p>
 */
public class DefaultIdentityProvider implements IdentityProvider {

    /**
     * Singleton instance fo the default identity provider
     */
    public static final IdentityProvider INSTANCE = new DefaultIdentityProvider();

    private final List<String> usernameClaims;

    /**
     * Private constructor to prevent direct instantiation, use {@link #INSTANCE} to get the singleton instance
     */
    private DefaultIdentityProvider() {
        String claimsConfig = Configurator.get(new String[] { ConfigurationParameters.PARAM_USERNAME_CLAIMS },
                                               AuthConstants.DEFAULT_USERNAME_CLAIMS);
        this.usernameClaims =
                Utils.parseParameter(claimsConfig, DefaultIdentityProvider::parseList, Collections.emptyList());
    }

    /**
     * Parses the raw string configuration into a list
     *
     * @param value Raw string value
     * @return List of values
     */
    static List<String> parseList(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return Arrays.stream(value.split(",")).toList();
    }

    @Override
    public String identityForUser(Jws<Claims> jws) {
        for (String claim : usernameClaims) {
            if (!jws.getPayload().containsKey(claim)) {
                continue;
            }

            try {
                String username = jws.getPayload().get(claim, String.class);
                if (StringUtils.isNotBlank(username)) {
                    return username;
                }
            } catch (RequiredTypeException e) {
                // Ignore, try the next claim
            }
        }
        return jws.getPayload().getSubject();
    }
}
