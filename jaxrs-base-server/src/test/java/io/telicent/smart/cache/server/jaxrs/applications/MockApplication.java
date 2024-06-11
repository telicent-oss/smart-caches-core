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

import io.telicent.smart.cache.server.jaxrs.parameters.ModeParametersProvider;
import io.telicent.smart.cache.server.jaxrs.resources.DataResource;
import io.telicent.smart.cache.server.jaxrs.resources.HealthResource;
import io.telicent.smart.cache.server.jaxrs.resources.ParamsResource;
import io.telicent.smart.cache.server.jaxrs.resources.ProblemsResource;

import java.util.Set;

public class MockApplication extends AbstractApplication {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = super.getClasses();
        classes.add(ModeParametersProvider.class);
        classes.add(HealthResource.class);
        classes.add(DataResource.class);
        classes.add(ParamsResource.class);
        classes.add(ProblemsResource.class);
        return classes;
    }
}
