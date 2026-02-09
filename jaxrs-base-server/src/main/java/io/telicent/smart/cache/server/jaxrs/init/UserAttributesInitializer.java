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

import io.telicent.jena.abac.core.AttributesStore;
import io.telicent.jena.abac.core.AttributesStoreAuthServer;
import io.telicent.jena.abac.core.AttributesStoreLocal;
import io.telicent.jena.abac.core.AttributesStoreRemote;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.caches.configuration.auth.AuthConstants;
import jakarta.servlet.ServletContextEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static io.telicent.smart.caches.configuration.auth.AuthConstants.calculateHierarchyLookupUrl;

/**
 * A servlet context listener that initialises the user attributes store
 *
 * @deprecated As the new experimental Security Plugin API comes into usage it becomes the responsibility of plugins,
 * not applications, to create appropriate {@link io.telicent.smart.cache.security.attributes.AttributesProvider} based
 * on configuration.  Once that API gains wider adoption this will be removed from the Base Server API.
 */
@Deprecated(forRemoval = false)
public class UserAttributesInitializer implements ServerConfigInit {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAttributesInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Attempting to initialize user attributes store...");
        AttributesStore store;

        // If using new Authorization Server then ignore deprecated configuration and just use that instead
        if (Configurator.get(AuthConstants.FEATURE_FLAG_AUTHORIZATION, Boolean::parseBoolean, true)) {
            store = new AttributesStoreAuthServer(null);
        } else {
            // Use old style configuration i.e. Telicent Access service
            String attributesUrl = Configurator.get(AuthConstants.ENV_USER_ATTRIBUTES_URL);
            String hierarchyUrl = Configurator.get(AuthConstants.ENV_ATTRIBUTE_HIERARCHY_URL);

            if (Objects.equals(attributesUrl, AuthConstants.AUTH_DISABLED)) {
                // NB: We don't need a special AttributesStore implementation here because when authentication is disabled
                // we'll be using the SecurityOptions.DISABLED singleton meaning the code will skip the portions that
                // enforce security labels so never need to care about user attributes
                LOGGER.warn(
                        "Running with authentication disabled, server will act as if the anonymous user has all attributes available");
                store = new AttributesStoreLocal();
            } else if (StringUtils.isNotBlank(attributesUrl)) {
                if (StringUtils.isNotBlank(hierarchyUrl)) {
                    LOGGER.info("Using remote user attributes store at {} with hierarchy service at {}", attributesUrl,
                                hierarchyUrl);
                } else {
                    hierarchyUrl = calculateHierarchyLookupUrl(attributesUrl);
                    LOGGER.info("Using remote user attributes store at {} with assumed hierarchy service at {}",
                                attributesUrl, hierarchyUrl);
                    LOGGER.warn(
                            "Please explicitly configure the hierarchy service via the {} environment variable if necessary",
                            AuthConstants.ENV_ATTRIBUTE_HIERARCHY_URL);
                }
                store = new AttributesStoreRemote(attributesUrl, hierarchyUrl);
            } else {
                LOGGER.warn(
                        "No user attributes store configured, using an empty store.  Users will only be able to view data that has no labels present.");
                store = new AttributesStoreLocal();
            }
        }

        LOGGER.info("Using User Attributes Store {}", store.getClass().getCanonicalName());
        sce.getServletContext().setAttribute(AttributesStore.class.getCanonicalName(), store);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().removeAttribute(AttributesStore.class.getCanonicalName());
    }

    @Override
    public String getName() {
        return "ABAC Authorization";
    }

    @Override
    public int priority() {
        return 99;
    }
}
