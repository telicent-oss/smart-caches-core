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
import io.telicent.jena.abac.attributes.Attribute;
import io.telicent.jena.abac.attributes.ValueTerm;
import io.telicent.jena.abac.core.AttributesStoreAuthServer;
import io.telicent.servlet.auth.jwt.JwtServletConstants;
import io.telicent.smart.caches.configuration.auth.UserInfo;
import io.telicent.smart.caches.configuration.auth.UserInfoLookup;
import io.telicent.smart.caches.configuration.auth.UserInfoLookupException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.SecurityContext;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
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

    private static AttributeValueSet findAttributes(String username) {
        AttributesStoreAuthServer attrStore = new AttributesStoreAuthServer(null);
        return attrStore.attributes(username);
    }

    private static void applyFilter(UserInfoLookup lookup, String username) throws IOException,
            ServletException {
        // Given
        UserInfoFilter filter = new UserInfoFilter();
        ContainerRequestContext requestContext = mockRequest(username, lookup, filter);

        // When
        filter.filter(requestContext);

        // Then
        AttributesStoreAuthServer attrStore = new AttributesStoreAuthServer(null);
        if (username != null) {
            Assert.assertNotNull(attrStore.attributes(username));
        }
    }

    @Test
    public void givenMinimalUserInfo_whenConvertingToUserAttributes_thenNoAttributes() throws UserInfoLookupException,
            ServletException, IOException {
        // Given
        UserInfo info = UserInfo.builder().preferredName("Mr T. Test").build();
        UserInfoLookup lookup = mockLookup(info);

        // When
        applyFilter(lookup, "test");

        // Then
        AttributeValueSet attrs = findAttributes("test");
        Assert.assertNotNull(attrs);
        Assert.assertTrue(attrs.isEmpty());
    }

    private void verifyMissingAttributes(AttributeValueSet attrs, String... missingAttributes) {
        for (String missing : missingAttributes) {
            Attribute a = Attribute.create(missing);
            Assert.assertFalse(attrs.hasAttribute(a));
        }
    }

    private void verifyAttributes(AttributeValueSet attrs, Map<String, Object> attributes) {
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            Attribute a = Attribute.create(entry.getKey());
            Assert.assertTrue(attrs.hasAttribute(a));
            Collection<ValueTerm> values = attrs.get(a);

            if (entry.getValue() instanceof Collection<?> expected) {
                Assert.assertEquals(expected.size(), values.size());
                List<String> expectedValues = expected.stream().map(Object::toString).sorted().toList();
                List<String> actualValues = values.stream().map(ValueTerm::getString).sorted().toList();
                Assert.assertEquals(expectedValues, actualValues);
            } else {
                Assert.assertNotEquals(values.size(), 0);
                verifyValue(entry.getValue(), values.iterator().next());
            }
        }
    }

    private static void verifyValue(Object expected, ValueTerm actual) {
        if (expected instanceof Boolean b) {
            Assert.assertEquals(b, actual.getBoolean());
        } else {
            Assert.assertEquals(expected.toString(), actual.getString());
        }
    }

    @Test
    public void givenBasicUserInfo_whenConvertingToUserAttributes_thenAttributes() throws UserInfoLookupException,
            ServletException, IOException {
        // Given
        Map<String, Object> attributes =
                Map.of("email", "test@example.org", "age", 42, "nationality", "GBR", "active", true);
        UserInfo info = UserInfo.builder().preferredName("Mr T. Test").attributes(attributes).build();
        UserInfoLookup lookup = mockLookup(info);

        // When
        applyFilter(lookup, "tester");

        // Then
        AttributeValueSet attrs = findAttributes("tester");
        Assert.assertNotNull(attrs);
        Assert.assertFalse(attrs.isEmpty());
        verifyAttributes(attrs, attributes);
    }

    @Test
    public void givenUserInfoWithListAttribute_whenConvertingToUserAttributes_thenAllAttributesMaterialised() throws
            UserInfoLookupException, ServletException, IOException {
        // Given
        Map<String, Object> attributes =
                Map.of("email", List.of("test@example.org", "test@personal.org"), "age", 42, "nationality",
                       List.of("GBR", "US"), "active", true);
        UserInfo info = UserInfo.builder().preferredName("Mr T. Test").attributes(attributes).build();
        UserInfoLookup lookup = mockLookup(info);

        // When
        applyFilter(lookup, "tester");

        // Then
        AttributeValueSet attrs = findAttributes("tester");
        Assert.assertNotNull(attrs);
        Assert.assertFalse(attrs.isEmpty());
        verifyAttributes(attrs, attributes);
    }

    @Test
    public void givenUserInfoWithNonConvertibleAttributes_whenConvertingToUserAttributes_thenAttributesIgnored() throws
            UserInfoLookupException, ServletException, IOException {
        // Given
        Map<String, Object> attributes = Map.of("ignored", new Object());
        UserInfo info = UserInfo.builder().preferredName("Mr T. Test").attributes(attributes).build();
        UserInfoLookup lookup = mockLookup(info);

        // When
        applyFilter(lookup, "tester");

        // Then
        AttributeValueSet attrs = findAttributes("tester");
        Assert.assertNotNull(attrs);
        Assert.assertTrue(attrs.isEmpty());
        verifyMissingAttributes(attrs, "ignored");
    }

    @Test
    public void givenUserInfoWithMapAttribute_whenConvertingToUserAttributes_thenAttributesFlattened() throws
            UserInfoLookupException, ServletException, IOException {
        // Given
        Map<String, Object> attributes = Map.of("email", "test@example.org", "employment",
                                                Map.of("company", "Telicent Ltd", "title", "QA Engineer", "department",
                                                       "R&D", "manages", List.of("Adam", "Bob", "Eve")));
        UserInfo info = UserInfo.builder().preferredName("Mr T. Test").attributes(attributes).build();
        UserInfoLookup lookup = mockLookup(info);

        // When
        applyFilter(lookup, "tester");

        // Then
        AttributeValueSet attrs = findAttributes("tester");
        Assert.assertNotNull(attrs);
        Assert.assertFalse(attrs.isEmpty());
        Map<String, Object> expected =
                Map.of("email", "test@example.org", "employment.company", "Telicent Ltd", "employment.title",
                       "QA Engineer", "employment.department", "R&D", "employment.manages",
                       List.of("Adam", "Bob", "Eve"));
        verifyAttributes(attrs, expected);
    }

    @Test
    public void givenNonRetrievableUserInfo_whenFiltering_thenNoAttributes() throws UserInfoLookupException,
            ServletException, IOException {
        // Given
        UserInfoLookup lookup = Mockito.mock(UserInfoLookup.class);
        when(lookup.lookup(any())).thenThrow(new UserInfoLookupException("failed"));

        // When
        applyFilter(lookup, "test");

        // Then
        AttributeValueSet attrs = findAttributes("test");
        Assert.assertTrue(attrs.isEmpty());
    }

    @Test
    public void givenNoAuthentication_whenFiltering_thenNoAttributes() throws ServletException, IOException {
        // Given
        UserInfoLookup lookup = Mockito.mock(UserInfoLookup.class);

        // When and Then
        applyFilter(lookup, null);
    }
}
