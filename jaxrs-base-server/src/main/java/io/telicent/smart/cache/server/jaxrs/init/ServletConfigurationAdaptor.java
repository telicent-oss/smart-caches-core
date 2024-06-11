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

import io.telicent.smart.caches.configuration.auth.TelicentConfigurationAdaptor;
import jakarta.servlet.ServletContext;

import java.util.Objects;


/**
 * An adaptor from our environment variable driven configuration approach to the JWT Servlet Auth libraries runtime
 * independent configuration mechanism
 */
public class ServletConfigurationAdaptor extends TelicentConfigurationAdaptor {
    private final ServletContext servletContext;

    /**
     * Creates a new adaptor
     *
     * @param servletContext Servlet Context into which configured objects will be stored as attributes
     */
    public ServletConfigurationAdaptor(ServletContext servletContext) {
        super();
        this.servletContext = Objects.requireNonNull(servletContext, "Servlet Context cannot be null");
    }

    @Override
    public void setAttribute(String attribute, Object value) {
        this.servletContext.setAttribute(attribute, value);
    }

    @Override
    public Object getAttribute(String attribute) {
        return this.servletContext.getAttribute(attribute);
    }
}
