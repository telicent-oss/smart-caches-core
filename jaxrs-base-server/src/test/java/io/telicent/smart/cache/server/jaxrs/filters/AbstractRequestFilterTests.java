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

import io.jsonwebtoken.Jws;
import io.telicent.servlet.auth.jwt.configuration.ClaimPath;
import io.telicent.servlet.auth.jwt.jaxrs3.JwtSecurityContext;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.server.jaxrs.model.Problem;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.mockito.ArgumentCaptor;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Abstract base class for tests of {@link ContainerRequestFilter} which sets up a bunch of mocking to make it easier to
 * test request filters with unit tests
 */
public abstract class AbstractRequestFilterTests {
    protected final Map<String, Object> attributes = new LinkedHashMap<>();
    protected ContainerRequestContext requestContext;
    protected ResourceInfo resourceInfo;
    protected UriInfo uriInfo;
    protected HttpHeaders httpHeaders;
    protected ServletContext servletContext;
    protected SecurityContext securityContext;

    @BeforeMethod
    public void setup() {
        Configurator.reset();

        this.requestContext = mock(ContainerRequestContext.class);
        this.resourceInfo = mock(ResourceInfo.class);
        this.uriInfo = mock(UriInfo.class);
        this.httpHeaders = mock(HttpHeaders.class);
        this.servletContext = mock(ServletContext.class);
        this.securityContext = mock(SecurityContext.class);
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

    protected SecurityContext mockUser(String username) {
         return new JwtSecurityContext(mock(Jws.class), username, true, ClaimPath.topLevel("roles"));
    }

    protected abstract ContainerRequestFilter createFilter();

    protected void applyFilter() throws IOException {
        ContainerRequestFilter filter = createFilter();
        filter.filter(this.requestContext);
    }

    /**
     * Verifies that the filter aborted the request with a problem response and an expected status
     *
     * @param status Expected status
     * @return Problem response for further inspection
     */
    protected Problem verifyProblemResponse(Response.Status status) {
        ArgumentCaptor<Response> capture = ArgumentCaptor.forClass(Response.class);
        verify(this.requestContext, times(1)).abortWith(capture.capture());
        Response response = capture.getValue();
        Assert.assertEquals(response.getStatus(), status.getStatusCode());
        Assert.assertTrue(response.getEntity() instanceof Problem);
        Problem problem = (Problem) response.getEntity();
        Assert.assertEquals(problem.getStatus(), status.getStatusCode());
        return problem;
    }
}
