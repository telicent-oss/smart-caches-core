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
package io.telicent.smart.cache.security.plugins.rdf.abac;

import io.telicent.jena.abac.core.AttributesStoreAuthServer;
import io.telicent.jena.abac.core.AttributesStoreRemote;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.configuration.sources.PropertiesSource;
import io.telicent.smart.caches.configuration.auth.AuthConstants;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Properties;

public class TestRdfAbacPluginConfiguration {

    @BeforeClass
    public void setup() {
        Configurator.reset();
    }

    @AfterMethod
    public void teardown() {
        Configurator.reset();
    }

    @Test
    public void givenUserInfoUrl_whenConfiguring_thenAttributeStoreAuthServerUsed() {
        // Given
        Properties properties = new Properties();
        properties.put(AuthConstants.ENV_USERINFO_URL, "https://example.org/userinfo");
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        RdfAbacPlugin plugin = new RdfAbacPlugin();

        // Then
        Assert.assertTrue(plugin.getAttributesStore() instanceof AttributesStoreAuthServer);
    }

    @Test
    public void givenUserAttributesUrl_whenConfiguring_thenAttributeStoreRemoteUsed() {
        // Given
        Properties properties = new Properties();
        properties.put(AuthConstants.ENV_USER_ATTRIBUTES_URL, "https://example.org/users/{user}");
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        RdfAbacPlugin plugin = new RdfAbacPlugin();

        // Then
        Assert.assertTrue(plugin.getAttributesStore() instanceof AttributesStoreRemote);
    }
}
