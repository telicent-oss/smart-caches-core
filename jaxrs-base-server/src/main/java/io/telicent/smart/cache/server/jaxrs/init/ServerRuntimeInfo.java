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

import io.telicent.smart.cache.observability.RuntimeInfo;
import jakarta.servlet.ServletContextEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Servlet context initialiser that will print information about the servers runtime environment at startup
 */
public class ServerRuntimeInfo implements ServerConfigInit {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerRuntimeInfo.class);

    @Override
    public String getName() {
        return "Server Runtime Environment Information";
    }

    @Override
    public int priority() {
        // Highest priorities get called first and always want to dump runtime environment information first
        return Integer.MAX_VALUE;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // General runtime environment information - Memory, Java Version and OS
        RuntimeInfo.printRuntimeInfo(LOGGER);

        // Also dump LibraryVersion information
        RuntimeInfo.printLibraryVersions(LOGGER);
    }
}
