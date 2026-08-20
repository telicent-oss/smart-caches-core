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

import io.telicent.smart.cache.server.jaxrs.filters.Example;
import io.telicent.smart.cache.server.jaxrs.filters.Extended;
import io.telicent.smart.cache.server.jaxrs.filters.None;
import io.telicent.smart.cache.server.jaxrs.resources.AbstractHealthResource;

import java.util.Set;

public class ContextRequiredApplication extends AbstractApplication{

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = super.getClasses();
        classes.add(Example.class);
        classes.add(Extended.class);
        classes.add(None.class);
        return classes;
    }

    @Override
    protected Class<? extends AbstractHealthResource> getHealthResourceClass() {
        return null;
    }
}
