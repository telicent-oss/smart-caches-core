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
package io.telicent.smart.cache.security.data.requests;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.telicent.smart.cache.security.data.requests.MinimalRequestContext;
import io.telicent.smart.caches.configuration.auth.UserInfo;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

@SuppressWarnings("unchecked")
public class TestMinimalRequestContext {

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNullJwt_whenConstructing_thenNullPointerException() {
        new MinimalRequestContext(null, "user", null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNullUsername_whenConstructing_thenNullPointerException() {
        new MinimalRequestContext(Mockito.mock(Jws.class), null, null);
    }

    @Test
    public void givenMinimalData_whenUsingMinimalRequestContext_thenAsExpected() {
        // Given
        Jws<Claims> jws = Mockito.mock(Jws.class);
        String username = "test";

        // When
        MinimalRequestContext context = new MinimalRequestContext(jws, username, null);

        // Then
        Assert.assertSame(context.verifiedJwt(), jws);
        Assert.assertEquals(context.username(), "test");
        Assert.assertNull(context.userInfo());
        Assert.assertTrue(context.requestHeader("Test").isEmpty());
        Assert.assertNull(context.requestUri());
        Assert.assertNull(context.requestPath());
        Assert.assertNull(context.requestMethod());
    }

    @Test
    public void givenUserInfo_whenUsingMinimalRequestContext_thenUserInfoAvailable() {
        final Jws<Claims> jws = Mockito.mock(Jws.class);
        final UserInfo userInfo = UserInfo.builder().sub("test-user").build();

        final MinimalRequestContext context = new MinimalRequestContext(jws, "test-user", userInfo);

        Assert.assertSame(context.userInfo(), userInfo);
        Assert.assertEquals(context.username(), "test-user");
    }

    @Test
    public void givenAnyHeaderName_whenRequestingHeader_thenEmptyList() {
        final Jws<Claims> jws = Mockito.mock(Jws.class);
        final MinimalRequestContext context = new MinimalRequestContext(jws, "user", null);

        Assert.assertTrue(context.requestHeader("Authorization").isEmpty());
        Assert.assertTrue(context.requestHeader("X-Custom-Header").isEmpty());
        Assert.assertTrue(context.requestHeader(null).isEmpty());
    }
}
