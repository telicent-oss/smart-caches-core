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
package io.telicent.smart.cache.security.identity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.RequiredTypeException;
import io.telicent.servlet.auth.jwt.configuration.ConfigurationParameters;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.configuration.sources.PropertiesSource;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.*;

public class TestDefaultIdentityProvider {

    @BeforeClass
    public void setup() {
        Configurator.reset();
    }

    @AfterMethod
    public void teardown() {
        Configurator.reset();
    }

    @SuppressWarnings("unchecked")
    private Jws<Claims> mockJws(String subject, Map<String, Object> claims) {
        Claims payload = mock(Claims.class);
        when(payload.getSubject()).thenReturn(subject);
        when(payload.containsKey(any())).thenAnswer(
                invocationOnMock -> claims.containsKey(invocationOnMock.getArgument(0, String.class)));
        when(payload.get(any(), any())).thenAnswer(invocationOnMock -> {
            String name = invocationOnMock.getArgument(0);
            Class<?> cls = invocationOnMock.getArgument(1);

            if (claims.containsKey(name)) {
                Object value = claims.get(name);
                if (value.getClass().equals(cls)) {
                    return value;
                } else {
                    throw new RequiredTypeException(
                            "Claim " + name + " not of required type " + cls.getCanonicalName());
                }
            } else {
                return null;
            }
        });

        Jws<Claims> jws = (Jws<Claims>) mock(Jws.class);
        when(jws.getPayload()).thenReturn(payload);
        return jws;
    }

    @DataProvider(name = "JWTs")
    private Object[][] jwts() {
        return new Object[][] {
                { mockJws("test", Collections.emptyMap()), "test" },
                { mockJws("test", Map.of("preferred_name", "Mr T. Test")), "Mr T. Test" },
                { mockJws("test", Map.of("preferred_name", "", "email", "test@test.org")), "test@test.org" },
                { mockJws("test", Map.of("preferred_name", "", "email", "  ", "username", "tester")), "tester" },
                { mockJws("test", Map.of("preferred_name", true, "email", 12345, "username", new Object())), "test" },
        };
    }

    @Test(dataProvider = "JWTs")
    public void givenDefaultIdentityProvider_whenObtainingIdentity_thenExpectedIdentityReturned(Jws<Claims> jws,
                                                                                                String expectedIdentity) {
        // Given
        IdentityProvider provider = new DefaultIdentityProvider();

        // When
        String id = provider.identityForUser(jws);

        // Then
        Assert.assertEquals(id, expectedIdentity);
    }

    @Test(dataProvider = "JWTs")
    public void givenDefaultIdentityProviderWithCustomConfig_whenObtainingIdentity_thenIgnoresNormalClaims(Jws<Claims> jws, String expectedIdentity) {
        // Given
        Properties properties = new Properties();
        properties.put(ConfigurationParameters.PARAM_USERNAME_CLAIMS, "custom1,custom2");
        Configurator.setSingleSource(new PropertiesSource(properties));
        IdentityProvider provider = new DefaultIdentityProvider();

        // When
        String id = provider.identityForUser(jws);

        // Then
        Assert.assertEquals(id, "test");
    }

    @Test
    public void givenDefaultIdentityProviderWithCustomConfig_whenObtainingIdentityFromCustomJws_thenUsesCustomClaims() {
        // Given
        Jws<Claims> jws = mockJws("test", Map.of("custom1", "Mr Timothy Test"));
        Properties properties = new Properties();
        properties.put(ConfigurationParameters.PARAM_USERNAME_CLAIMS, "custom1,custom2");
        Configurator.setSingleSource(new PropertiesSource(properties));
        IdentityProvider provider = new DefaultIdentityProvider();

        // When
        String id = provider.identityForUser(jws);

        // Then
        Assert.assertEquals(id, "Mr Timothy Test");
    }
}
