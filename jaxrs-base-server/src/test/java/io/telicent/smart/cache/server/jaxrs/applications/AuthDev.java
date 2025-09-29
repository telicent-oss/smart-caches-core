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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.configuration.sources.PropertiesSource;
import io.telicent.smart.cache.server.jaxrs.init.JwtAuthInitializer;
import io.telicent.smart.cache.server.jaxrs.init.UserInfoLookupInit;
import io.telicent.smart.caches.configuration.auth.AuthConstants;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Development helper for running our mock server application configured to authenticate against a locally running
 * Telicent Auth server
 */
public class AuthDev extends AbstractAppEntrypoint {

    public static void main(String[] args) {
        // Enable logging (we're in src/test where the default logback-test.xml disables logging)
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.INFO);

        // Configure to talk to local development instance of Telicent Auth server
        Properties props = new Properties();
        props.put(AuthConstants.ENV_JWKS_URL, "http://auth.telicent.localhost/oauth2/jwks");
        props.put(AuthConstants.ENV_USERINFO_URL, "http://auth.telicent.localhost/userinfo");
        Configurator.addSource(new PropertiesSource(props));

        // Run a test application
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
                            // Enable JWT authentication and UserInfo lookup
                            .withListener(JwtAuthInitializer.class)
                            .withListener(UserInfoLookupInit.class)
                            .withAuthExclusion("/healthz");
    }
}
