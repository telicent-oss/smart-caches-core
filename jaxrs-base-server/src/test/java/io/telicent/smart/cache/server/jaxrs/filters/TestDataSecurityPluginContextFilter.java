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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.telicent.servlet.auth.jwt.JwtServletConstants;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.SecurityContext;
import org.testng.annotations.Test;

import java.io.IOException;
import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TestDataSecurityPluginContextFilter {

    @Test
    public void givenAuthenticatedRequestWithoutVerifiedJwt_whenFiltering_thenNoPluginContextIsCreated() throws IOException {
        DataSecurityPluginContextFilter filter = new DataSecurityPluginContextFilter();
        ContainerRequestContext requestContext = mockRequest("test");

        filter.filter(requestContext);

        verify(requestContext, never()).setProperty(eq(DataSecurityPluginContextFilter.ATTRIBUTE), any());
    }

    @Test
    public void givenRequestWithoutPluginContextAttribute_whenResponseFiltering_thenNothingIsRemoved() throws IOException {
        DataSecurityPluginContextFilter filter = new DataSecurityPluginContextFilter();
        ContainerRequestContext requestContext = mockRequest("test");
        ContainerResponseContext responseContext = mock(ContainerResponseContext.class);
        when(requestContext.getProperty(eq(DataSecurityPluginContextFilter.ATTRIBUTE))).thenReturn(null);

        filter.filter(requestContext, responseContext);

        verify(requestContext, never()).removeProperty(eq(DataSecurityPluginContextFilter.ATTRIBUTE));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenAuthenticatedRequestWithVerifiedJwt_whenFiltering_thenPluginContextIsCreated() throws IOException {
        DataSecurityPluginContextFilter filter = new DataSecurityPluginContextFilter();
        ContainerRequestContext requestContext = mockRequest("test");
        Jws<Claims> jwt = mock(Jws.class);
        when(requestContext.getProperty(eq(JwtServletConstants.REQUEST_ATTRIBUTE_VERIFIED_JWT))).thenReturn(jwt);

        filter.filter(requestContext);

        verify(requestContext).setProperty(eq(DataSecurityPluginContextFilter.ATTRIBUTE), any());
    }

    private static ContainerRequestContext mockRequest(String username) {
        ContainerRequestContext context = mock(ContainerRequestContext.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(username);
        when(securityContext.getUserPrincipal()).thenReturn(principal);
        when(context.getSecurityContext()).thenReturn(securityContext);
        return context;
    }
}
