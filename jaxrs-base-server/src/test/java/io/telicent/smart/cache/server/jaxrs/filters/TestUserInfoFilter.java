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

import io.telicent.servlet.auth.jwt.JwtServletConstants;
import io.telicent.smart.caches.configuration.auth.UserInfo;
import io.telicent.smart.caches.configuration.auth.UserInfoLookup;
import io.telicent.smart.caches.configuration.auth.UserInfoLookupException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.SecurityContext;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TestUserInfoFilter {

    private UserInfoLookup mockLookup(UserInfo info) throws UserInfoLookupException {
        UserInfoLookup lookup = Mockito.mock(UserInfoLookup.class);
        when(lookup.lookup(any())).thenReturn(info);
        return lookup;
    }

    private static ContainerRequestContext mockRequest(String username, UserInfoLookup lookup, UserInfoFilter filter) {
        ContainerRequestContext context = mock(ContainerRequestContext.class);
        if (username != null) {
            when(context.getProperty(eq(JwtServletConstants.REQUEST_ATTRIBUTE_RAW_JWT))).thenReturn("token");
            SecurityContext securityContext = mock(SecurityContext.class);
            Principal principal = mock(Principal.class);
            when(principal.getName()).thenReturn(username);
            when(securityContext.getUserPrincipal()).thenReturn(principal);
            when(context.getSecurityContext()).thenReturn(securityContext);
        }
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getAttribute(eq(UserInfoLookup.class.getCanonicalName()))).thenReturn(lookup);
        filter.setServletContext(servletContext);
        return context;
    }
    
    private static UserInfo applyFilter(UserInfoLookup lookup, String username, boolean expectUserInfo) throws IOException {
        // Given
        UserInfoFilter filter = new UserInfoFilter();
        ContainerRequestContext requestContext = mockRequest(username, lookup, filter);

        // When
        filter.filter(requestContext);

        // Then
        if (expectUserInfo) {
            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            verify(requestContext, times(1)).setProperty(eq(UserInfo.class.getCanonicalName()), captor.capture());
            return (UserInfo) captor.getValue();
        } else {
            verify(requestContext, never()).setProperty(any(), any());
            return null;
        }
    }

    @Test
    public void givenMinimalUserInfo_whenFiltering_thenAvailable() throws UserInfoLookupException,
            IOException {
        // Given
        UserInfo info = UserInfo.builder().preferredName("Mr T. Test").build();
        UserInfoLookup lookup = mockLookup(info);

        // When
        UserInfo filterInfo = applyFilter(lookup, "test", true);

        // Then
        Assert.assertEquals(filterInfo, info);
    }
    
    @Test
    public void givenBasicUserInfo_whenFiltering_thenAvailable() throws UserInfoLookupException,
            ServletException, IOException {
        // Given
        Map<String, Object> attributes =
                Map.of("email", "test@example.org", "age", 42, "nationality", "GBR", "active", true);
        UserInfo info = UserInfo.builder().preferredName("Mr T. Test").attributes(attributes).build();
        UserInfoLookup lookup = mockLookup(info);

        // When
        UserInfo filterInfo = applyFilter(lookup, "tester", true);

        // Then
        Assert.assertEquals(filterInfo, info);
    }

    @Test
    public void givenNonRetrievableUserInfo_whenFiltering_thenNoAttributes() throws UserInfoLookupException,
            ServletException, IOException {
        // Given
        UserInfoLookup lookup = Mockito.mock(UserInfoLookup.class);
        when(lookup.lookup(any())).thenThrow(new UserInfoLookupException("failed"));

        // When
        UserInfo info = applyFilter(lookup, "test", false);

        // Then
        Assert.assertNull(info);
    }

    @Test
    public void givenNoAuthentication_whenFiltering_thenNoAttributes() throws ServletException, IOException {
        // Given
        UserInfoLookup lookup = Mockito.mock(UserInfoLookup.class);

        // When and Then
        applyFilter(lookup, null, false);
    }
}
