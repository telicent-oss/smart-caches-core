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
package io.telicent.smart.cache.server.jaxrs.applications;

import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.configuration.sources.PropertiesSource;
import io.telicent.smart.caches.configuration.auth.AuthConstants;
import jakarta.ws.rs.core.Response;
import org.testng.Assert;

import java.util.Properties;

/**
 * Tests that explicitly check that when the {@link AuthConstants#FEATURE_FLAG_AUTHORIZATION} is set to {@code false}
 * the authorization policy enforcement feature is not added into the JAX-RS application and thus authorization policy
 * is not applied.
 */
public class TestServerAuthorizationDisabled extends TestServerRoleAuthorization {

    @Override
    protected void configureAuthentication() {
        Properties properties = new Properties();
        properties.put(AuthConstants.ENV_JWKS_URL, this.keyServer.getJwksUrl());
        properties.put(AuthConstants.ENV_USERINFO_URL, this.keyServer.getUserInfoUrl());
        // Explicitly disable authorization feature via feature flag
        properties.put(AuthConstants.FEATURE_FLAG_AUTHORIZATION, "false");
        // Note that we don't need to explicit configure PARAM_ROLES_CLAIM as we already set a suitable default that
        // matches what the Telicent Authentication server returns
        Configurator.setSingleSource(new PropertiesSource(properties));
    }

    @Override
    protected void verifyUnauthorized(Response response, String expectedErrorDetail) {
        // When AuthZ explicitly disabled then no requests that are authenticated will be rejected with a 401
        // Unauthorized
        Assert.assertNotEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());
    }
}
