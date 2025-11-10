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
package io.telicent.smart.cache.server.jaxrs.filters;

import io.telicent.jena.abac.AttributeValueSet;
import io.telicent.jena.abac.attributes.AttributeValue;
import io.telicent.jena.abac.attributes.ValueTerm;
import io.telicent.jena.abac.core.AttributesStoreAuthServer;
import io.telicent.servlet.auth.jwt.JwtServletConstants;
import io.telicent.smart.caches.configuration.auth.UserInfo;
import io.telicent.smart.caches.configuration.auth.UserInfoLookup;
import io.telicent.smart.caches.configuration.auth.UserInfoLookupException;
import jakarta.annotation.Priority;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A JAX-RS filter that obtains and injects {@link io.telicent.smart.caches.configuration.auth.UserInfo} for the
 * currently authenticated user into the request attributes for subsequent filters e.g.
 * {@link TelicentAuthorizationFilter} to use in making authorization decisions
 */
@Provider
@Priority(Priorities.AUTHORIZATION - 10)
public class UserInfoFilter implements ContainerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserInfoFilter.class);

    private static volatile boolean notConfiguredWarning = false;

    @Context
    private ServletContext servletContext;

    /**
     * Sets the {@link ServletContext}, intended only for unit test usage
     * @param servletContext Servlet Context
     */
    void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Detect whether the request is authorized by finding the Raw JWT attribute since we need that to exchange for
        // User Info anyway
        String jwt = (String) requestContext.getProperty(JwtServletConstants.REQUEST_ATTRIBUTE_RAW_JWT);
        if (StringUtils.isNotBlank(jwt)) {
            // Also deal with the RDF-ABAC integration here, need to set the user so that the RDF-ABAC layer has the
            // username, and user info, if available
            // This is kinda clumsy but for now we're stuck with the API that RDF-ABAC gives us so have to jump through
            // some hoops to make things work properly without creating circular dependencies between Smart Caches Core
            // and RDF-ABAC
            UserInfoLookup lookup =
                    (UserInfoLookup) this.servletContext.getAttribute(UserInfoLookup.class.getCanonicalName());
            String username = requestContext.getSecurityContext()
                                            .getUserPrincipal()
                                            .getName();
            AttributesStoreAuthServer.addIdAndUserName(jwt, username);
            if (lookup != null) {
                try {
                    UserInfo userInfo = lookup.lookup(jwt);
                    requestContext.setProperty(UserInfo.class.getCanonicalName(), userInfo);
                    AttributesStoreAuthServer.addUserNameAndAttributes(username,
                                                                       toAttributeValueSet(userInfo.getAttributes()));
                } catch (UserInfoLookupException e) {
                    LOGGER.warn("Failed to obtain user info: {}", e.getMessage());

                    // In the error path forcibly reset the cached attributes to the empty set as otherwise users could get
                    // attributes they no longer have
                    AttributesStoreAuthServer.addUserNameAndAttributes(
                            username,
                            AttributeValueSet.EMPTY);
                }
            } else if (!notConfiguredWarning) {
                // Issue this warning once, and only once, for the lifetime of the application
                LOGGER.warn(
                        "No UserInfoLookup configured so unable to obtain User Info which may result in failed authorization");
                notConfiguredWarning = true;
            }
        }
    }

    /**
     * Converts users raw attributes into RDF-ABAC compatible attributes
     *
     * @param attributes Raw attributes
     * @return RDF-ABAC Attribute Value set
     */
    private AttributeValueSet toAttributeValueSet(Map<String, Object> attributes) {
        List<AttributeValue> attrs = new ArrayList<>();
        convertMapToAttributes(attrs, "", attributes);
        return AttributeValueSet.of(attrs);
    }

    private static void convertMapToAttributes(List<AttributeValue> attrs, String prefix, Map<String, Object> map) {
        if (map == null) return;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            convertValue(attrs, !prefix.isEmpty() ? prefix + "." + entry.getKey() : entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private static void convertValue(List<AttributeValue> attrs, String key, Object value) {
        // TODO Once we upgrade to JDK 21+ can simplify this into a switch statement
        if (value instanceof String strValue) {
            attrs.add(AttributeValue.of(key, ValueTerm.value(strValue)));
        } else if (value instanceof Number numberValue) {
            attrs.add(AttributeValue.of(key, ValueTerm.value(numberValue.toString())));
        } else if (value instanceof Boolean boolValue) {
            attrs.add(AttributeValue.of(key, ValueTerm.value(boolValue)));
        } else if (value instanceof Map<?, ?> map) {
            Map<String, Object> values = (Map<String, Object>) map;
            convertMapToAttributes(attrs, key, values);
        } else if (value instanceof Collection<?> collection) {
            Collection<Object> values = (Collection<Object>) collection;
            for (Object v : values) {
                convertValue(attrs, key, v);
            }
        } else {
            LOGGER.warn("Unsupported value type for attribute {} ignored: {}", key, value.getClass());
        }
    }
}
