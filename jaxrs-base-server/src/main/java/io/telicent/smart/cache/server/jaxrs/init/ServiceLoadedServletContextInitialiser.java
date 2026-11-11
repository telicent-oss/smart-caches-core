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

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * A Servlet Context listener that automatically discovers other listeners that conform to the {@link ServerConfigInit}
 * interface and have a suitable {@code META-INF/services/io.telicent.smart.cache.server.jaxrs.init.ServerConfigInit}
 * file present
 */
public class ServiceLoadedServletContextInitialiser implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLoadedServletContextInitialiser.class);

    /**
     * Applies the discovered initialisers
     *
     * @param sce      Servlet Context Event
     * @param inits    Discovered initialisers
     * @param action   Action being performed, used for logging statements
     * @param consumer Consumer that applies the relevant action to the initialisers
     */
    private void applyInits(ServletContextEvent sce, List<ServerConfigInit> inits, String action,
                            BiConsumer<ServletContextListener, ServletContextEvent> consumer) {
        LOGGER.info("Discovered {} ServerConfigInit instances via ServiceLoader", inits.size());
        int success = 0;
        for (ServerConfigInit init : inits) {
            LOGGER.info("{}ing {} (Priority {})...", action, init.getName(), init.priority());
            try {
                consumer.accept(init, sce);
                success++;
            } catch (Exception e) {
                LOGGER.warn("{}ing {} failed: {}", action, init.getName(), e.getMessage());
            }
        }
        LOGGER.info("{}/{} ServerConfigInit instances were successfully {}ed", success, inits.size(), action);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        List<ServerConfigInit> inits = ServerConfigInit.getConfigInitialisers();
        // NB - Action intentionally misspelt because the applyInit method will prepend -ing and -ed suffixes to it in
        //      its log statements
        applyInits(sce, inits, "Initialis", ServletContextListener::contextInitialized);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        List<ServerConfigInit> inits = ServerConfigInit.getConfigInitialisers();
        // Reverse the order so that we destroy them in the reverse order to which we initialised them
        Collections.reverse(inits);
        applyInits(sce, inits, "Destroy", ServletContextListener::contextDestroyed);
    }
}
