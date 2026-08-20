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

import io.telicent.smart.cache.server.jaxrs.model.Problem;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.mockito.ArgumentCaptor;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestRequireContextFilter {

    private ContainerRequestContext requestContext;
    private ResourceInfo resourceInfo;
    private HttpHeaders httpHeaders;
    private ServletContext servletContext;
    private final Map<String, Object> attributes = new LinkedHashMap<>();

    @BeforeMethod
    public void setup() {
        this.requestContext = mock(ContainerRequestContext.class);
        this.resourceInfo = mock(ResourceInfo.class);
        this.httpHeaders = mock(HttpHeaders.class);
        this.servletContext = mock(ServletContext.class);
        this.attributes.clear();

        doAnswer(invocation -> {
            String attribute = invocation.getArgument(0, String.class);
            Object value = invocation.getArgument(1, Object.class);
            this.attributes.put(attribute, value);
            return null;
        }).when(servletContext).setAttribute(any(), any());
        doAnswer(invocation -> {
            String attribute = invocation.getArgument(0, String.class);
            return this.attributes.get(attribute);
        }).when(servletContext).getAttribute(any());
    }

    @AfterClass
    public void teardown() {
        this.attributes.clear();
    }

    private RequireContextFilter createFilter() {
        return new RequireContextFilter(this.resourceInfo, this.httpHeaders, this.servletContext);
    }

    private void applyFilter() throws IOException {
        RequireContextFilter filter = createFilter();
        filter.filter(this.requestContext);
    }

    private void verifyServiceUnavailableResponse() {
        ArgumentCaptor<Response> capture = ArgumentCaptor.forClass(Response.class);
        verify(this.requestContext).abortWith(capture.capture());
        Response response = capture.getValue();
        Assert.assertEquals(response.getStatus(), Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
        Assert.assertTrue(response.getEntity() instanceof Problem);
        Problem problem = (Problem) response.getEntity();
        Assert.assertEquals(problem.getStatus(), Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void givenNoResourceInfo_whenFiltering_thenNoOp() throws IOException {
        // Given
        this.resourceInfo = null;

        // When
        applyFilter();

        // Then
        verifyNoInteractions(this.requestContext);
    }

    @Test
    public void givenResourceInfo_whenFiltering_thenFilterRejects() throws NoSuchMethodException, IOException {
        // Given
        when(this.resourceInfo.getResourceMethod()).thenReturn(Example.class.getMethod("basic"));

        // When
        applyFilter();

        // Then
        verifyServiceUnavailableResponse();
    }

    @Test
    public void givenResourceInfoWithNullMethod_whenFiltering_thenClassLevelConstraintsStillApplied() throws IOException {
        // Given
        when(this.resourceInfo.getResourceClass()).thenAnswer(invocation -> Example.class);

        // When
        applyFilter();

        // Then
        verifyServiceUnavailableResponse();
    }

    @Test
    public void givenResourceInfoWithNullMethodAndClass_whenFiltering_thenNoOp() throws IOException {
        // Given and When
        applyFilter();

        // Then
        verifyNoInteractions(this.requestContext);
    }

    @DataProvider(name = "valid")
    private Object[][] validConfigurations() {
        return new Object[][] {
                { None.class, "none", Collections.emptyMap() },
                { Example.class, "basic", Map.of("a", new Object()) },
                { Example.class, "additional", Map.of("a", new Object(), "map", new HashMap<>()) },
                { Extended.class, "extended", Map.of("a", "test", "extended", 12345L)},
                { Extended.class, "extended", Map.of("a", "test", "extended", BigDecimal.valueOf(1.23e4))}
        };
    }

    @DataProvider(name = "invalid")
    private Object[][] invalidConfigurations() {
        return new Object[][] {
                { Example.class, "basic", Collections.emptyMap() },
                { Example.class, "additional", Map.of("a", new Object(), "map", new ArrayList<>()) },
                { Example.class, "additional", Map.of("a", new Object(), "map", new LinkedHashMap<>()) },
                { Extended.class, "extended", Map.of("a", "test", "extended", 1.23e4)}
        };
    }

    @Test(dataProvider = "valid")
    public void givenResourceInfoAndValidAttributes_whenFiltering_thenOk(Class<?> clazz, String methodName,
                                                                         Map<String, Object> attributes) throws
            NoSuchMethodException, IOException {
        // Given
        when(this.resourceInfo.getResourceMethod()).thenReturn(clazz.getMethod(methodName));
        when(this.resourceInfo.getResourceClass()).thenAnswer(invocation -> clazz);
        this.attributes.putAll(attributes);

        // When
        applyFilter();

        // Then
        verify(this.requestContext, never()).abortWith(any());
    }

    @Test(dataProvider = "invalid")
    public void givenResourceInfoAndInvalidAttributes_whenFiltering_thenRequestAborted(Class<?> clazz, String methodName,
                                                                         Map<String, Object> attributes) throws
            NoSuchMethodException, IOException {
        // Given
        when(this.resourceInfo.getResourceMethod()).thenReturn(clazz.getMethod(methodName));
        when(this.resourceInfo.getResourceClass()).thenAnswer(invocation -> clazz);
        this.attributes.putAll(attributes);

        // When
        applyFilter();

        // Then
        verifyServiceUnavailableResponse();
    }
}
