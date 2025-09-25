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

import io.telicent.servlet.auth.jwt.jaxrs3.JwtAuthFilter;
import io.telicent.smart.cache.server.jaxrs.errors.FallbackExceptionMapper;
import io.telicent.smart.cache.server.jaxrs.resources.AbstractHealthResource;
import io.telicent.smart.cache.server.jaxrs.resources.AwsElbResource;
import io.telicent.smart.cache.server.jaxrs.resources.JwksResource;
import io.telicent.smart.cache.server.jaxrs.resources.UserInfoResource;

import java.util.Set;

public class MockKeyServerApplication extends AbstractApplication {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = super.getClasses();
        classes.add(JwksResource.class);
        classes.add(AwsElbResource.class);
        classes.add(UserInfoResource.class);
        classes.add(JwtAuthFilter.class);
        return classes;
    }

    @Override
    protected Class<? extends AbstractHealthResource> getHealthResourceClass() {
        return null;
    }
}
