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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.configuration.sources.PropertiesSource;
import io.telicent.smart.cache.server.jaxrs.annotations.RateLimit;
import io.telicent.smart.cache.server.jaxrs.init.RateLimitInit;
import io.telicent.smart.cache.server.jaxrs.model.Problem;
import io.telicent.smart.cache.server.jaxrs.resources.DataResource;
import io.telicent.smart.cache.server.jaxrs.resources.HealthResource;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.Strings;
import org.awaitility.Awaitility;
import org.jspecify.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.*;

public class TestRateLimitFilter extends AbstractRequestFilterTests {

    protected static final String TOO_MANY_REQUESTS = "Too Many Requests";

    @Override
    protected ContainerRequestFilter createFilter() {
        return new RateLimitFilter(this.resourceInfo, this.uriInfo, this.httpHeaders, this.servletContext,
                                   this.securityContext);
    }

    private void ensureRateLimitConfiguration() {
        this.attributes.put(RateLimitInit.ATTRIBUTE_RATE_LIMITS_CACHE,
                            Caffeine.<String, List<RateLimit>>newBuilder().maximumSize(100).build());
        this.attributes.put(RateLimitInit.ATTRIBUTE_RATE_LIMITS_REGISTRY, RateLimiterRegistry.ofDefaults());
        this.attributes.put(RateLimitInit.ATTRIBUTE_RATE_LIMITS_CONFIGURATIONS,
                            Caffeine.<String, RateLimiterConfig>newBuilder().maximumSize(10).build());
    }

    private void applyFilter(int times) throws IOException {
        ContainerRequestFilter filter = createFilter();
        for (int i = 1; i <= times; i++) {
            filter.filter(this.requestContext);
        }
    }

    private Problem applyFilterUntilRejection() {
        ContainerRequestFilter filter = createFilter();
        AtomicReference<Problem> failure = new AtomicReference<>();
        Awaitility.await("Filter should eventually start to reject requests")
                  .pollInterval(Duration.ZERO)
                  .atMost(Duration.ofSeconds(3))
                  .ignoreExceptionsInstanceOf(AssertionError.class)
                  .until(() -> {
                      filter.filter(this.requestContext);
                      failure.set(verifyProblemResponse(Response.Status.TOO_MANY_REQUESTS));
                      return true;
                  });
        return failure.get();
    }

    @Test
    public void givenNoResourceInfo_whenApplyingFilter_thenNoOp() throws IOException {
        // Given
        this.resourceInfo = null;

        // When
        applyFilter();

        // Then
        verifyNoInteractions(this.requestContext);
    }

    @Test
    public void givenInsufficientlyConfigured_whenApplyingFilter_thenInternalServerError() throws IOException {
        // Given
        when(this.resourceInfo.getResourceClass()).thenAnswer(invocation -> DataResource.class);

        // When
        applyFilter();

        // Then
        Problem problem = verifyProblemResponse(Response.Status.INTERNAL_SERVER_ERROR);
        Assert.assertTrue(Strings.CI.contains(problem.getDetail(), "invalid rate limits"));
    }

    @Test
    public void givenSufficientConfiguration_whenApplyingFilterOnce_thenRequestPermitted() throws IOException {
        // Given
        ensureRateLimitConfiguration();
        when(this.resourceInfo.getResourceClass()).thenAnswer(invocation -> DataResource.class);

        // When
        applyFilter();

        // Then
        verifyNoInteractions(this.requestContext);
    }

    @Test
    public void givenSufficientConfiguration_whenApplyingFilterManyTimes_thenRequestEventuallyRejected_andAcceptedOnceWindowElapsed() throws
            InterruptedException, IOException {
        // Given
        ensureRateLimitConfiguration();
        when(this.resourceInfo.getResourceClass()).thenAnswer(invocation -> DataResource.class);

        // When
        Problem problem = applyFilterUntilRejection();

        // Then
        Assert.assertTrue(Strings.CI.contains(problem.getDetail(), "maximum permitted"));

        // And
        Thread.sleep(1000); // Wait long enough for rate limit window to refresh
        applyFilter();
        verify(this.requestContext, times(1)).abortWith(any());
    }

    @Test
    public void givenResourceHasMultipleLimits_whenApplyingFilter_thenLowestLimitRejectsRequestFirst() throws
            IOException, NoSuchMethodException {
        // Given
        ensureRateLimitConfiguration();
        when(this.resourceInfo.getResourceClass()).thenAnswer(invocation -> DataResource.class);
        when(this.resourceInfo.getResourceMethod()).thenReturn(
                DataResource.class.getMethod("setData", String.class, String.class));

        // When
        applyFilter(6);

        // Then
        Problem problem = verifyProblemResponse(Response.Status.TOO_MANY_REQUESTS);
        Assert.assertTrue(Strings.CI.contains(problem.getDetail(), "5 data writes/second"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenMethodInfoOnly_whenApplyingFilter_thenMethodLimitApplied_andDiscoversLimitFromMethodClassAnyway() throws
            NoSuchMethodException, IOException {
        // Given
        ensureRateLimitConfiguration();
        when(this.resourceInfo.getResourceMethod()).thenReturn(
                DataResource.class.getMethod("setData", String.class, String.class));

        // When
        applyFilter(6);

        // Then
        Problem problem = verifyProblemResponse(Response.Status.TOO_MANY_REQUESTS);
        Assert.assertTrue(Strings.CI.contains(problem.getDetail(), "5 data writes/second"));

        // And
        List<RateLimit> discovered = getDiscoveredLimits();
        Assert.assertNotNull(discovered);
        Assert.assertEquals(discovered.size(), 2,
                            "Should discover the class level limit based on the methods class even without explicit class info provided");
    }

    @Test
    public void givenMethodFromDifferentClass_whenApplyingFilter_thenMethodLimitApplied_andDiscoversOnlyMethodLimit() throws
            NoSuchMethodException, IOException {
        // Given
        ensureRateLimitConfiguration();
        when(this.resourceInfo.getResourceClass()).thenAnswer(invocation -> Object.class);
        when(this.resourceInfo.getResourceMethod()).thenReturn(
                DataResource.class.getMethod("setData", String.class, String.class));

        // When
        applyFilter(6);

        // Then
        Problem problem = verifyProblemResponse(Response.Status.TOO_MANY_REQUESTS);
        Assert.assertTrue(Strings.CI.contains(problem.getDetail(), "5 data writes/second"));

        // And
        List<RateLimit> discovered = getDiscoveredLimits();
        Assert.assertNotNull(discovered);
        Assert.assertEquals(discovered.size(), 1, "Provided class has no additional rate limits to discover");
    }

    private @Nullable List<RateLimit> getDiscoveredLimits() {
        return ((Cache<String, List<RateLimit>>) this.attributes.get(
                RateLimitInit.ATTRIBUTE_RATE_LIMITS_CACHE)).getIfPresent(
                RateLimitFilter.resourceKey(this.resourceInfo));
    }

    @Test
    public void givenSufficientConfiguration_whenApplyingPerUserRateLimit_thenRequestEventuallyRejectedForOneUser_andImmediatelyAcceptedForDifferentUser() throws
            IOException, NoSuchMethodException {
        // Given
        ensureRateLimitConfiguration();
        when(this.resourceInfo.getResourceClass()).thenAnswer(invocation -> DataResource.class);
        when(this.resourceInfo.getResourceMethod()).thenReturn(
                DataResource.class.getMethod("setData", String.class, String.class));

        // When
        this.securityContext = mockUser("a@example.org");
        Problem problem = applyFilterUntilRejection();

        // Then
        Assert.assertTrue(Strings.CI.contains(problem.getDetail(), "5 data writes/second"));

        // And
        this.securityContext = mockUser("b@example.org");
        applyFilter();
        verify(this.requestContext, times(1)).abortWith(any());
    }

    @Test
    public void givenRateLimitOverrideConfiguration_whenApplyingFilter_thenOverriddenLimitsApply_andRequestThatWouldBeRejectedBlocksUntilSucceeds() throws
            IOException, NoSuchMethodException {
        // Given
        ensureRateLimitConfiguration();
        when(this.resourceInfo.getResourceClass()).thenAnswer(invocation -> DataResource.class);
        Method method = DataResource.class.getMethod("setData", String.class, String.class);
        when(this.resourceInfo.getResourceMethod()).thenReturn(method);
        Properties properties = new Properties();
        RateLimit limitToOverride = method.getAnnotation(RateLimit.class);
        properties.put(RateLimitInit.overrideKey(limitToOverride, RateLimitInit.RATE_LIMIT_WINDOW_SUFFIX), 2500);
        properties.put(RateLimitInit.overrideKey(limitToOverride, RateLimitInit.RATE_LIMIT_REQUESTS_SUFFIX), 10);
        properties.put(RateLimitInit.overrideKey(limitToOverride, RateLimitInit.RATE_LIMIT_TIMEOUT_SUFFIX), 3000);
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        applyFilter(10);

        // Then
        verifyNoInteractions(this.requestContext);

        // And
        // Normally this request would fail immediately BUT we've overridden the timeout to be 3 seconds which is longer
        // than the request window.  Therefore, permits will refresh before the timeout elapses and then the request
        // will stop blocking and proceed
        long start = System.currentTimeMillis();
        applyFilter();
        long elapsed = System.currentTimeMillis() - start;
        verifyNoInteractions(this.requestContext);
        Assert.assertTrue(elapsed > 1500,
                          "Request should have blocked for some time until next request window was open");
    }

    @Test
    public void givenInvalidRateLimitOverrideConfiguration_whenApplyingFilter_thenInternalServerError() throws
            IOException, NoSuchMethodException {
        // Given
        ensureRateLimitConfiguration();
        when(this.resourceInfo.getResourceClass()).thenAnswer(invocation -> DataResource.class);
        Method method = DataResource.class.getMethod("setData", String.class, String.class);
        when(this.resourceInfo.getResourceMethod()).thenReturn(method);
        Properties properties = new Properties();
        RateLimit limitToOverride = method.getAnnotation(RateLimit.class);
        // NB - Requests per window MUST be > 0
        properties.put(RateLimitInit.overrideKey(limitToOverride, RateLimitInit.RATE_LIMIT_WINDOW_SUFFIX), 0);
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        applyFilter();

        // Then
        verifyProblemResponse(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void givenRateLimits_whenManyParallelUsers_thenAllEventuallyExceedRateLimit() {
        // Given
        ensureRateLimitConfiguration();
        when(this.resourceInfo.getResourceClass()).thenAnswer(invocation -> DataResource.class);
        ExecutorService executor = null;
        try {
            executor = Executors.newFixedThreadPool(10);

            // When
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i <= 10; i++) {
                ContainerRequestFilter filter =
                        new RateLimitFilter(this.resourceInfo, this.uriInfo, this.httpHeaders, this.servletContext,
                                            mockUser("u" + i));
                futures.add(executor.submit(makeRequestsUntilRateLimited(filter)));
            }

            // Then
            Awaitility.await("All request threads to be rate limited")
                      .pollDelay(Duration.ofMillis(100))
                      .pollInterval(Duration.ofMillis(100))
                      .atMost(Duration.ofSeconds(3))
                      .until(() -> futures.stream().allMatch(f -> f.isDone() && successful(f)));

        } finally {
            if (executor != null) {
                executor.shutdownNow();
            }
        }
    }

    /**
     * A runnable that makes requests to the filter until it gets rate limited
     *
     * @param filter Filter to apply
     * @return Runnable
     */
    private Runnable makeRequestsUntilRateLimited(ContainerRequestFilter filter) {
        return () -> {
            ContainerRequestContext context = mock(ContainerRequestContext.class);

            while (true) {
                try {
                    filter.filter(context);
                    verify(context, times(1)).abortWith(any());
                    break;
                } catch (AssertionError e) {
                    // Ignore, may take a few requests before we start to get rate limited
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private boolean successful(Future<?> f) {
        try {
            f.get(10, TimeUnit.MILLISECONDS);
            return true;
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return false;
        }
    }

    @Test
    public void givenResourceWithNoRateLimits_whenApplyingFilter_thenNoOp() throws IOException {
        // Given
        ensureRateLimitConfiguration();
        when(this.resourceInfo.getResourceClass()).thenAnswer(invocation -> HealthResource.class);

        // When
        applyFilter(100);

        // Then
        verifyNoInteractions(this.requestContext);
    }

    @RateLimit(name = "test", requestsPerWindow = 100, window = 1000, errorTitle = TOO_MANY_REQUESTS, errorDetail = TOO_MANY_REQUESTS)
    private static final class NonUniqueLimits {

        @RateLimit(name = "test", requestsPerWindow = 1000, window = 5000, errorTitle = TOO_MANY_REQUESTS, errorDetail = "A duplicate rate limit name is invalid configuration")
        public void duplicateLimit() {

        }

        public void noAdditionalLimit() {

        }

        @RateLimit(name = "other", requestsPerWindow = 1, window = 10_000, errorTitle = "Slow API", errorDetail = "This API can only process 1 request/10 seconds")
        public void uniqueLimit() {

        }

        @RateLimit(name = "", requestsPerWindow = 1000, window = 1000, errorTitle = TOO_MANY_REQUESTS, errorDetail = "Empty names are invalid")
        public void emptyLimitName() {

        }

        @RateLimit(name = "    ", requestsPerWindow = 1000, window = 1000, errorTitle = TOO_MANY_REQUESTS, errorDetail = "Empty names are invalid")
        public void blankLimitName() {

        }

        @RateLimit(name = "sub", requestsPerWindow = 10, window = 1000, errorTitle = TOO_MANY_REQUESTS, errorDetail = "This portion of the API permits 10 requests/second")
        private static final class SubResource {
            public void multipleClassLimits() {

            }
        }
    }

    @DataProvider(name = "potentiallyInvalidLimits")
    private Object[][] potentiallyInvalidLimits() {
        return new Object[][] {
                { NonUniqueLimits.class, "duplicateLimit", 1, false },
                { NonUniqueLimits.class, "emptyLimitName", 1, false },
                { NonUniqueLimits.class, "blankLimitName", 1, false },
                { NonUniqueLimits.class, "noAdditionalLimit", 100, true },
                { NonUniqueLimits.class, "uniqueLimit", 1, true },
                { NonUniqueLimits.SubResource.class, "multipleClassLimits", 10, true }
        };
    }

    @Test(dataProvider = "potentiallyInvalidLimits")
    public void givenResourceWithPotentiallyInvalidLimits_whenApplyingFilter_thenRejectsOrInternalServerErrorAsAppropriate(
            Class<?> resourceClass, String methodName, int permitted, boolean valid) throws NoSuchMethodException,
            IOException {
        // Given
        ensureRateLimitConfiguration();
        when(this.resourceInfo.getResourceClass()).thenAnswer(invocation -> resourceClass);
        when(this.resourceInfo.getResourceMethod()).thenReturn(resourceClass.getMethod(methodName));

        // When
        applyFilter(valid ? permitted : 1);

        // Then
        if (valid) {
            // For valid configurations the requests so far should all have been permitted
            verifyNoInteractions(this.requestContext);
            // However a subsequent request should exceed the rate limit and trigger the 429 Too Many Requests response
            applyFilter();
            verifyProblemResponse(Response.Status.TOO_MANY_REQUESTS);
        } else {
            // For invalid configurations the first request should immediately trigger a 500 Internal Server
            Problem problem = verifyProblemResponse(Response.Status.INTERNAL_SERVER_ERROR);
            Assert.assertTrue(Strings.CI.contains(problem.getDetail(), "invalid rate limits"));
        }
    }

    @DataProvider(name = "rateLimitConfigAttributes")
    private Object[][] rateLimitConfigAttributes() {
        return new Object[][] {
                { RateLimitInit.ATTRIBUTE_RATE_LIMITS_CACHE },
                { RateLimitInit.ATTRIBUTE_RATE_LIMITS_CONFIGURATIONS },
                { RateLimitInit.ATTRIBUTE_RATE_LIMITS_REGISTRY }
        };
    }

    @Test(dataProvider = "rateLimitConfigAttributes")
    public void givenPartialRateLimitConfiguration_whenApplyingFilter_thenInternalServerError(String attribute) throws
            IOException {
        // Given
        ensureRateLimitConfiguration();
        this.attributes.remove(attribute);
        when(this.resourceInfo.getResourceClass()).thenAnswer(invocation -> DataResource.class);

        // When
        applyFilter();

        // Then
        verifyProblemResponse(Response.Status.INTERNAL_SERVER_ERROR);
    }

}
