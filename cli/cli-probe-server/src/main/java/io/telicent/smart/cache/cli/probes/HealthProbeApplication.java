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
import io.telicent.smart.cache.server.jaxrs.applications.AbstractApplication;

import java.util.Set;

public final class HealthProbeApplication extends AbstractApplication {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = super.getClasses();
        classes.add(ReadinessResource.class);
        return classes;
    }

    @Override
    protected boolean isAuthEnabled() {
        // Authentication is ALWAYS disabled for the health probe application
        return false;
    }
}
