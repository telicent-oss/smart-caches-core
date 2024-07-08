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
package io.telicent.smart.cache.cli.probes;

import io.telicent.smart.cache.cli.probes.resources.ReadinessResource;
import io.telicent.smart.cache.server.jaxrs.applications.AbstractAppEntrypoint;
import io.telicent.smart.cache.server.jaxrs.applications.ServerBuilder;
import io.telicent.smart.cache.server.jaxrs.model.HealthStatus;

import java.util.function.Supplier;

/**
 * A Health Probe Server
 */
public class HealthProbeServer extends AbstractAppEntrypoint {

    private final String displayName;
    private final int port;
    private final Supplier<HealthStatus> readinessSupplier;
    private final String[] libraries;

    /**
     * Creates a new Health Probe Server
     *
     * @param displayName       Display Name
     * @param port              Port to run on
     * @param readinessSupplier Supplier that can determine whether the application this server is hosted in is
     *                          currently healthy
     * @param libraries         Library versions to report in the liveness check
     */
    public HealthProbeServer(String displayName, int port, Supplier<HealthStatus> readinessSupplier,
                             String... libraries) {
        this.displayName = displayName;
        this.port = port;
        this.readinessSupplier = readinessSupplier;
        this.libraries = libraries;
    }

    @Override
    protected ServerBuilder buildServer() {
        return ServerBuilder.create()
                            .application(HealthProbeApplication.class)
                            .withCors(c -> c.withDefaults())
                            .displayName(this.displayName)
                            .port(this.port)
                            .localhost()
                            .withAutoConfigInitialisation()
                            .withContextAttribute(ReadinessResource.class.getCanonicalName(), this.readinessSupplier)
                            .withVersionInfo("cli-probe-server")
                            .withVersionInfo(libraries);
    }

    /**
     * Runs the server and registers a shutdown hook for cleaning up the server
     */
    public void run() {
        this.run(false);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (this.server != null) {
                this.server.shutdownNow();
            }
        }));
    }

    /**
     * Shuts down the server
     */
    public void shutdown() {
        if (this.server != null) {
            this.server.shutdownNow();
        }
    }

    /**
     * Gets the base URL
     *
     * @return Base URL
     */
    public String getBaseUrl() {
        return String.format("http://localhost:%d", this.port);
    }

    /**
     * Gets the liveness probe URL
     *
     * @return Liveness probe URL
     */
    public String getLivenessUrl() {
        return String.format("%s/%s", this.getBaseUrl(), "version-info");
    }

    /**
     * Gets the readiness probe URL
     *
     * @return Readiness probe URL
     */
    public String getReadinessUrl() {
        return String.format("%s/%s", this.getBaseUrl(), "healthz");
    }
}
