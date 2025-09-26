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
package io.telicent.smart.cache.server.jaxrs.init;

import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.caches.configuration.auth.AuthConstants;
import io.telicent.smart.caches.configuration.auth.RemoteUserInfoLookup;
import io.telicent.smart.caches.configuration.auth.UserInfoLookup;
import jakarta.servlet.ServletContextEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class UserInfoLookupInit implements ServerConfigInit {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserInfoLookupInit.class);

    @Override
    public String getName() {
        return "User Info Lookup";
    }

    @Override
    public int priority() {
        return 98;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String userInfoEndpoint = Configurator.get(AuthConstants.ENV_USERINFO_URL);
        if (StringUtils.isNotBlank(userInfoEndpoint)) {
            try {
                UserInfoLookup lookup = new RemoteUserInfoLookup(userInfoEndpoint);
                sce.getServletContext().setAttribute(UserInfoLookup.class.getCanonicalName(), lookup);
                LOGGER.info("Successfully configured UserInfoLookup: {}", lookup);
            } catch (IllegalArgumentException e) {
                // This can occur if the provided URL is invalid
                LOGGER.error(
                        "Configured {} had invalid value '{}' - {} - No User Info will be available for users which may result in authorization failures",
                        AuthConstants.ENV_USERINFO_URL, userInfoEndpoint, e.getMessage());
            }
        } else {
            LOGGER.warn(
                    "No User Info lookup configured.  No User Info will be available for users which may result in authorization failures");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        UserInfoLookup lookup =
                (UserInfoLookup) sce.getServletContext().getAttribute(UserInfoLookup.class.getCanonicalName());
        if (lookup != null) {
            try {
                lookup.close();
            } catch (IOException e) {
                LOGGER.warn("Failed to close UserInfoLookup: {}", e.getMessage());
            }
        }
    }
}
