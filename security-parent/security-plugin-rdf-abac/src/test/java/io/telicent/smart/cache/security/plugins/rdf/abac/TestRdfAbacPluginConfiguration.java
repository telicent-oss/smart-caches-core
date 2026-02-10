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
