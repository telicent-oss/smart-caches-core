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
package io.telicent.smart.cache.cli.probes.resources;

import io.telicent.smart.cache.server.jaxrs.model.HealthStatus;
import io.telicent.smart.cache.server.jaxrs.resources.AbstractHealthResource;
import jakarta.servlet.ServletContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

public class ReadinessResource extends AbstractHealthResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReadinessResource.class);

    @Override
    @SuppressWarnings("unchecked")
    protected HealthStatus determineStatus(ServletContext context) {
        try {
            Supplier<HealthStatus> supplier =
                    (Supplier<HealthStatus>) context.getAttribute(ReadinessResource.class.getCanonicalName());
            if (supplier == null) {
                LOGGER.warn("Application considered unhealthy as no readiness supplier configured");
                return HealthStatus.builder()
                                   .healthy(false)
                                   .reasons(List.of("No HealthStatus supplier configured for the server"))
                                   .build();
            } else {
                HealthStatus status = supplier.get();
                if (status != null) {
                    if (!status.isHealthy()) {
                        LOGGER.warn("Application indicates it is unhealthy due to {} reasons: {}",
                                    status.reasons().size(),
                                    StringUtils.join(status.reasons(), ", "));
                    }
                    return status;
                } else {
                    LOGGER.warn("Application considered unhealthy as readiness supplier produced a null status");
                    return HealthStatus.builder()
                                       .healthy(false)
                                       .reasons(List.of("HealthStatus supplier failed to produce a non-null status"))
                                       .build();
                }
            }
        } catch (Throwable e) {
            LOGGER.warn("Application considered unhealthy as readiness supplier produced an error", e);
            return HealthStatus.builder().healthy(false).reasons(List.of(e.getMessage())).build();
        }
    }
}
