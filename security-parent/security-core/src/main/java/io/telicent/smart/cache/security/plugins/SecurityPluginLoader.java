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

import io.telicent.smart.cache.security.plugins.failsafe.FailSafePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * Static class that provides access to the configured {@link SecurityPlugin}
 */
public final class SecurityPluginLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityPluginLoader.class);

    private static final Object LOCK = new Object();
    private static SecurityPlugin PLUGIN;

    /**
     * Private constructor to prevent instantiation as only static methods like {@link #load()} should be accessed by
     * callers
     */
    private SecurityPluginLoader() {
    }

    /**
     * Load/retrieve the registered Telicent Security Plugin
     * <p>
     * The security plugin to use is loaded via Java {@link ServiceLoader}, it is expected that 1, and only 1, plugin
     * instance is registered via the {@code META-INF/io.telicent.smart.caches.security.SecurityPluginLoader} file on
     * the classpath.
     * </p>
     * <p>
     * Note that if misconfiguration is detected, i.e. 0 plugins or more than 1 plugin, then this is configured to
     * {@link FailSafePlugin} as a safe fallback.  That initial call will log and throw an {@link Error}.  Subsequent
     * calls will then return the cached instance of {@link FailSafePlugin}.
     * </p>
     * <p>
     * Note that after the first call the configured plugin instance is returned on all subsequent invocations so it is
     * safe to call this method many times wherever the system needs to obtain the security plugin.
     * </p>
     *
     * @return Telicent Security Plugin
     * @throws Error Thrown
     */
    public static SecurityPlugin load() {
        synchronized (LOCK) {
            if (PLUGIN != null) {
                if (PLUGIN instanceof FailSafePlugin) {
                    throw new Error(
                            "Previous attempts to load a Telicent Security Plugin failed, operating in fail-safe mode.");
                }
                return PLUGIN;
            }

            LOGGER.info("Attempting to load Telicent Security Plugin...");

            ServiceLoader<SecurityPlugin> loader = ServiceLoader.load(SecurityPlugin.class);
            List<SecurityPlugin> loaded = new ArrayList<>();
            try {
                for (SecurityPlugin securityPlugin : loader) {
                    loaded.add(securityPlugin);
                }

                if (loaded.size() > 1) {
                    // Fail nastily if multiple plugins defined
                    LOGGER.error("Classpath contains multiple Telicent Security Plugins, found {}", loaded.stream()
                                                                                                          .map(p -> p.getClass()
                                                                                                                     .getCanonicalName())
                                                                                                          .collect(
                                                                                                                  Collectors.joining(
                                                                                                                          ", ")));
                    useFailSafe();
                    throw new Error(
                            "Multiple Telicent Security Plugins found so unable to determine which should be used.  Please correct the Classpath so only one plugin is registered.");
                } else if (loaded.isEmpty()) {
                    // Fail nastily if no plugins defined
                    LOGGER.error("Classpath contains no Telicent Security Plugins");
                    useFailSafe();
                    throw new Error(
                            "No Telicent Security Plugins found.  Please correct the Classpath so exactly one plugin is registered.");
                } else {
                    PLUGIN = loaded.get(0);
                    LOGGER.info("Loaded Telicent Security Plugin {}", PLUGIN.getClass().getCanonicalName());
                }
            } catch (Throwable e) {
                LOGGER.error("Failed to load Telicent Security Plugins", e);
                useFailSafe();
                throw new Error("Failed to load Telicent Security Plugins", e);
            }
            return PLUGIN;
        }
    }

    /**
     * Gets whether the plugin has been loaded yet
     *
     * @return True if loaded, false otherwise
     */
    public static boolean isLoaded() {
        return PLUGIN != null;
    }

    /**
     * Gets whether the {@link FailSafePlugin} is in-use, see {@link #load()} discussion for details
     *
     * @return True if operating in fail-safe mode, false if no plugin yet loaded or a valid plugin was loaded
     */
    public static boolean isFailSafeMode() {
        return PLUGIN instanceof FailSafePlugin;
    }

    /**
     * Configures the plugin to be the {@link FailSafePlugin} which is a fail-safe fallback used when the system detects
     * plugins are not properly configured
     */
    private static void useFailSafe() {
        PLUGIN = FailSafePlugin.INSTANCE;
    }
}
