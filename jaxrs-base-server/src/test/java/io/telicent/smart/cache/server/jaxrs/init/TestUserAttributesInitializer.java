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
package io.telicent.smart.cache.server.jaxrs.init;

import io.telicent.jena.abac.core.AttributesStore;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.configuration.sources.ConfigurationSource;
import io.telicent.smart.cache.configuration.sources.PropertiesSource;
import io.telicent.smart.caches.configuration.auth.AuthConstants;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.mockito.Mockito.*;

public class TestUserAttributesInitializer {

    private void verifyHierarchyUrlCalculation(String attributesUrl, String expectedHierarchyUrl) {
        String actualHierarchyUrl = UserAttributesInitializer.calculateHierarchyLookupUrl(attributesUrl);
        Assert.assertEquals(actualHierarchyUrl, expectedHierarchyUrl);
    }

    @BeforeMethod
    public void testSetup() {
        Configurator.reset();
    }

    @AfterClass
    public void cleanup() {
        Configurator.reset();
    }

    @Test
    public void calculate_hierarchy_url_01() {
        verifyHierarchyUrlCalculation(null, null);
    }

    @Test
    public void calculate_hierarchy_url_02() {
        verifyHierarchyUrlCalculation("", "");
    }

    @Test
    public void calculate_hierarchy_url_03() {
        verifyHierarchyUrlCalculation("foo", "foo");
    }

    @Test
    public void calculate_hierarchy_url_04() {
        verifyHierarchyUrlCalculation("/users/lookup/{user}", "/hierarchies/lookup/{name}");
    }

    @Test
    public void calculate_hierarchy_url_05() {
        verifyHierarchyUrlCalculation("/user/lookup/{user}", "/hierarchies/lookup/{name}");
    }

    @Test
    public void calculate_hierarchy_url_06() {
        verifyHierarchyUrlCalculation("/lookup/{user}", "/lookup/{name}");
    }

    @Test
    public void calculate_hierarchy_url_07() {
        verifyHierarchyUrlCalculation("/lookup/{foo}", "/lookup/{foo}");
    }

    private static void verifyDestruction(ServletContextEvent sce, ServletContext context,
                                          UserAttributesInitializer initializer) {
        initializer.contextDestroyed(sce);
        verify(context, times(1)).removeAttribute(eq(AttributesStore.class.getCanonicalName()));
    }

    private static UserAttributesInitializer verifyInitialisation(ServletContextEvent sce,
                                                                  ServletContext context) {
        UserAttributesInitializer initializer = new UserAttributesInitializer();
        initializer.contextInitialized(sce);

        verify(context, times(1)).setAttribute(eq(AttributesStore.class.getCanonicalName()), any());
        verify(context, never()).removeAttribute(eq(AttributesStore.class.getCanonicalName()));
        return initializer;
    }

    @Test
    public void user_attributes_disabled() {
        Properties properties = new Properties();
        properties.put(ConfigurationSource.asSystemPropertyKey(AuthConstants.ENV_USER_ATTRIBUTES_URL),
                       AuthConstants.AUTH_DISABLED);
        Configurator.setSingleSource(new PropertiesSource(properties));

        ServletContextEvent sce = mock(ServletContextEvent.class);
        ServletContext context = mock(ServletContext.class);
        when(sce.getServletContext()).thenReturn(context);

        UserAttributesInitializer initializer =
                verifyInitialisation(sce, context);
        verifyDestruction(sce, context, initializer);
    }

    @Test
    public void user_attributes_url_01() {
        Properties properties = new Properties();
        properties.put(ConfigurationSource.asSystemPropertyKey(AuthConstants.ENV_USER_ATTRIBUTES_URL),
                       "https://example.org/users/lookup/{user}");
        Configurator.setSingleSource(new PropertiesSource(properties));

        ServletContextEvent sce = mock(ServletContextEvent.class);
        ServletContext context = mock(ServletContext.class);
        when(sce.getServletContext()).thenReturn(context);

        UserAttributesInitializer initializer =
                verifyInitialisation(sce, context);
        verifyDestruction(sce, context, initializer);
    }

    @Test
    public void user_attributes_url_02() {
        Properties properties = new Properties();
        properties.put(ConfigurationSource.asSystemPropertyKey(AuthConstants.ENV_USER_ATTRIBUTES_URL),
                       "https://example.org/users/lookup/{user}");
        properties.put(ConfigurationSource.asSystemPropertyKey(AuthConstants.ENV_ATTRIBUTE_HIERARCHY_URL),
                       "https://example.org/labels/hierarchies/{hierarchy}");
        Configurator.setSingleSource(new PropertiesSource(properties));

        ServletContextEvent sce = mock(ServletContextEvent.class);
        ServletContext context = mock(ServletContext.class);
        when(sce.getServletContext()).thenReturn(context);

        UserAttributesInitializer initializer =
                verifyInitialisation(sce, context);
        verifyDestruction(sce, context, initializer);
    }
}
