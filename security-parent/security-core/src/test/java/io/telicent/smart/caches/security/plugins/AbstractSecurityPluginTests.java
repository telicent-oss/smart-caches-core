/**
 * Copyright (C) Telicent Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.telicent.smart.caches.security.plugins;

import io.telicent.smart.caches.security.AuthorizationProvider;
import io.telicent.smart.caches.security.entitlements.EntitlementsParser;
import io.telicent.smart.caches.security.entitlements.EntitlementsProvider;
import io.telicent.smart.caches.security.identity.IdentityProvider;
import io.telicent.smart.caches.security.labels.*;
import org.apache.jena.sparql.graph.GraphFactory;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Abstract test suite for security plugins, this verifies that a plugin fulfils the basic API contract
 * <p>
 * Note that individual plugins should provide their own test suites that test their plugin specific functionality
 * </p>
 */
public abstract class AbstractSecurityPluginTests {

    /**
     * The security plugin under test, populated by a call to {@link #getPlugin()} during the {@link #setup()} method
     */
    protected SecurityPlugin<?, ?> plugin;

    /**
     * Gets an instance of the security plugin that is going to be tested by this suite of abstract tests
     *
     * @return Security Plugin
     */
    protected abstract SecurityPlugin<?, ?> getPlugin();

    @BeforeClass
    public void setup() {
        this.plugin = getPlugin();
    }

    @AfterClass
    public void teardown() {
        this.plugin.close();
    }

    @Test
    public void givenPlugin_whenAccessingIdentityProvider_thenNotNull() {
        // Given and When
        IdentityProvider provider = this.plugin.identityProvider();

        // Then
        Assert.assertNotNull(provider);
    }

    @Test
    public void givenPlugin_whenAccessingEntitlementsParser_thenNotNull() {
        // Given and When
        EntitlementsParser<?> parser = this.plugin.entitlementsParser();

        // Then
        Assert.assertNotNull(parser);
    }

    @Test
    public void givenPlugin_whenAccessingLabelsParser_thenNotNull() {
        // Given and When
        SecurityLabelsParser<?> parser = this.plugin.labelsParser();

        // Then
        Assert.assertNotNull(parser);
    }

    @Test
    public void givenPlugin_whenAccessingEntitlementsProvider_thenNotNull() {
        // Given and When
        EntitlementsProvider<?> provider = this.plugin.entitlementsProvider();

        // Then
        Assert.assertNotNull(provider);
    }

    @Test
    public void givenPlugin_whenAccessingLabelsValidator_thenNotNull() {
        // Given and When
        SecurityLabelsValidator validator = this.plugin.labelsValidator();

        // Then
        Assert.assertNotNull(validator);
    }

    @Test
    public void givenPlugin_whenAccessingAuthorizationProvider_thenNotNull() {
        // Given and When
        AuthorizationProvider<?, ?> provider = this.plugin.authorizationProvider();

        // Then
        Assert.assertNotNull(provider);
    }

    @Test
    public void givenPlugin_whenAccessingLabelsApplicator_thenNotNull() {
        // Given and When
        SecurityLabelsApplicator<?> applicator =
                this.plugin.prepareLabelsApplicator(new byte[0], GraphFactory.createGraphMem());

        // Then
        Assert.assertNotNull(applicator);
    }

    @Test
    public void givenLabelWithWrongSchema_whenValidating_thenFails() {
        // Given
        short schema = incorrectSchema();
        byte[] label = SecurityPlugin.encodeSchemaPrefix(schema);

        // When
        SecurityLabelsValidator validator = this.plugin.labelsValidator();
        boolean valid = validator.validate(label);

        // Then
        Assert.assertFalse(valid);
    }

    private short incorrectSchema() {
        short schema = (short) ((this.plugin.defaultSchema() + 1) % Short.MAX_VALUE);
        if (this.plugin.supportsSchema(schema)) {
            throw new SkipException("Plugin supports the generated 'bad' schema identifier");
        }
        return schema;
    }

    @DataProvider(name = "validLabels")
    protected abstract Object[][] validLabels();

    @Test(dataProvider = "validLabels")
    public void givenValidLabel_whenValidating_thenValid(byte[] rawLabel) {
        // Given and When
        SecurityLabelsValidator validator = this.plugin.labelsValidator();
        boolean valid = validator.validate(rawLabel);

        // Then
        Assert.assertTrue(valid);
    }

    @Test(dataProvider = "validLabels")
    public void givenValidLabel_whenParsing_thenNotNull(byte[] rawLabel) throws MalformedLabelsException {
        // Given and When
        SecurityLabelsParser<?> parser = this.plugin.labelsParser();
        SecurityLabels<?> labels = parser.parseSecurityLabels(rawLabel);

        // Then
        Assert.assertNotNull(labels);
    }

    @Test(dataProvider = "validLabels", expectedExceptions = MalformedLabelsException.class)
    public void givenValidLabelPrependedWithBadPrefix_whenParsing_thenExceptionThrown(byte[] rawLabel) throws
            MalformedLabelsException {
        // Given
        SecurityLabelsParser<?> parser = this.plugin.labelsParser();
        byte[] modifiedLabel = new byte[rawLabel.length + 4];
        byte[] badPrefix = SecurityPlugin.encodeSchemaPrefix(incorrectSchema());
        System.arraycopy(badPrefix, 0, modifiedLabel, 0, badPrefix.length);
        System.arraycopy(rawLabel, 0, modifiedLabel, 4, rawLabel.length);

        // When and Then
        parser.parseSecurityLabels(modifiedLabel);
    }

    @DataProvider(name = "invalidLabels")
    protected abstract Object[][] invalidLabels();

    @Test(dataProvider = "invalidLabels")
    public void givenInvalidLabel_whenValidating_thenInvalid(byte[] rawLabel) {
        // Given and When
        SecurityLabelsValidator validator = this.plugin.labelsValidator();
        boolean valid = validator.validate(rawLabel);

        // Then
        Assert.assertFalse(valid);
    }

    @Test(dataProvider = "invalidLabels", expectedExceptions = MalformedLabelsException.class)
    public void givenInvalidLabel_whenParsing_thenExceptionThrown(byte[] rawLabel) throws MalformedLabelsException {
        // Given and When
        SecurityLabelsParser<?> parser = this.plugin.labelsParser();
        SecurityLabels<?> labels = parser.parseSecurityLabels(rawLabel);

        // Then
        Assert.assertNotNull(labels);
    }
}