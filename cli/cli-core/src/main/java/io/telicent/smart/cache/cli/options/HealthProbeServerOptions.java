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
package io.telicent.smart.cache.cli.options;

import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Port;
import com.github.rvesse.airline.annotations.restrictions.PortType;
import io.telicent.smart.cache.cli.probes.HealthProbeServer;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.server.jaxrs.model.HealthStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Options that provide a Health Probe Server that can be used to provide HTTP based liveness and readiness probes for
 * CLIs that aren't otherwise HTTP applications
 */
public class HealthProbeServerOptions {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthProbeServerOptions.class);
    /**
     * The default health probe server port
     */
    public static final int DEFAULT_PORT = 10101;

    @Option(name = { "--health-probe-port" }, description = "Provides a port that the health probe server will run on to offer a minimal HTTP server that supports liveness and readiness probes.  Defaults to 10101.")
    @Port(acceptablePorts = { PortType.USER, PortType.DYNAMIC })
    int healthProbePort =
            Configurator.get(CliEnvironmentVariables.HEALTH_PROBES_PORT, Integer::parseInt, DEFAULT_PORT);

    @Option(name = {
            "--health-probes", "--no-health-probes"
    }, description = "Sets whether the Health Probe Server is enabled/disabled.")
    boolean enableHealthProbeServer =
            Configurator.get(CliEnvironmentVariables.ENABLE_HEALTH_PROBES, Boolean::parseBoolean, true);

    private HealthProbeServer healthProbes;

    /**
     * Sets up the health probe server
     *
     * @param displayName       Display Name for the server
     * @param readinessSupplier Readiness supplier
     * @param libraries         Libraries whose version information will be incorporated into the liveness probe return
     */
    public void setupHealthProbeServer(String displayName, Supplier<HealthStatus> readinessSupplier,
                                       String... libraries) {
        if (!this.enableHealthProbeServer) {
            LOGGER.warn("Health Probe Server explicitly disabled by user");
            return;
        }
        this.healthProbes = new HealthProbeServer(displayName, this.healthProbePort, readinessSupplier, libraries);
        this.healthProbes.run();
    }

    /**
     * Tears down the health probe server
     */
    public void teardownHealthProbeServer() {
        if (this.healthProbes != null) {
            this.healthProbes.shutdown();
        }
    }
}
