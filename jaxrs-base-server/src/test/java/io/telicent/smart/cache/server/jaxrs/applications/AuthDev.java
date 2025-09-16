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

import io.telicent.servlet.auth.jwt.configuration.ConfigurationParameters;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.configuration.sources.PropertiesSource;
import io.telicent.smart.cache.server.jaxrs.init.JwtAuthInitializer;
import io.telicent.smart.caches.configuration.auth.AuthConstants;

import java.util.Properties;

/**
 * Development helper for running our mock server application configured to authenticate against a locally running
 * Telicent Auth server
 */
public class AuthDev extends AbstractAppEntrypoint {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(AuthConstants.ENV_JWKS_URL, "http://auth.telicent.localhost/oauth2/jwks");
        props.put(ConfigurationParameters.PARAM_ROLES_CLAIM, "roles");
        Configurator.addSource(new PropertiesSource(props));
        AuthDev authDev = new AuthDev();
        authDev.run(true);
    }

    @Override
    protected ServerBuilder buildServer() {
        return ServerBuilder.create()
                            .port(12345)
                            .hostname("localhost")
                            .displayName("Test Server")
                            .application(MockApplicationWithAuth.class)
                            .withListener(JwtAuthInitializer.class)
                            .withAuthExclusion("/healthz");
    }
}
