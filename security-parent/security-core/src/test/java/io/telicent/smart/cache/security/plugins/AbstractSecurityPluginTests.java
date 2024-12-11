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
package io.telicent.smart.cache.security.plugins;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.telicent.smart.cache.security.Authorizer;
import io.telicent.smart.cache.security.entitlements.Entitlements;
import io.telicent.smart.cache.security.entitlements.EntitlementsParser;
import io.telicent.smart.cache.security.entitlements.EntitlementsProvider;
import io.telicent.smart.cache.security.entitlements.MalformedEntitlementsException;
import io.telicent.smart.cache.security.identity.IdentityProvider;
import io.telicent.smart.cache.security.labels.*;
import io.telicent.smart.cache.security.requests.MinimalRequestContext;
import org.apache.jena.sparql.graph.GraphFactory;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

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
    protected SecurityPlugin plugin;

    /**
     * Gets an instance of the security plugin that is going to be tested by this suite of abstract tests
     *
     * @return Security Plugin
     */
    protected abstract SecurityPlugin getPlugin();

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
        EntitlementsParser parser = this.plugin.entitlementsParser();

        // Then
        Assert.assertNotNull(parser);
    }

    @Test
    public void givenPlugin_whenAccessingLabelsParser_thenNotNull() {
        // Given and When
        SecurityLabelsParser parser = this.plugin.labelsParser();

        // Then
        Assert.assertNotNull(parser);
    }

    @Test
    public void givenPlugin_whenAccessingEntitlementsProvider_thenNotNull() {
        // Given and When
        EntitlementsProvider provider = this.plugin.entitlementsProvider();

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
    public void givenPlugin_whenAccessingAuthorizer_thenNotNull() throws MalformedEntitlementsException {
        // Given and When
        Entitlements<?> entitlements = getTestEntitlements();
        Authorizer authorizer =
                this.plugin.prepareAuthorizer(entitlements);

        // Then
        Assert.assertNotNull(authorizer);
    }

    /**
     * Gets entitlements for the {@code test} user for the purposes of testing.  Done by calling
     * {@link SecurityPlugin#entitlementsProvider()} and then calling
     * {@link EntitlementsProvider#entitlementsForUser(Jws, String)} by default.
     * <p>
     * If a plugin to be tested is not able to provide test entitlements in that way then they should return a suitable
     * test instance.
     * </p>
     *
     * @return Test entitlements
     * @throws MalformedEntitlementsException Thrown if the plugin can't produce test entitlements
     */
    protected Entitlements<?> getTestEntitlements() throws MalformedEntitlementsException {
        return this.plugin.entitlementsProvider()
                          .entitlementsForUser(new MinimalRequestContext(getTestJws("test"), "test"));
    }

    /**
     * Gets a fake/mock JWS for a given user.
     * <p>
     * The default implementation just using Mockito to mock out a JWS
     * </p>
     *
     * @param username Username that should be present in the JWS
     * @return JWS
     */
    protected Jws<Claims> getTestJws(String username) {
        Jws<Claims> jws = Mockito.mock(Jws.class);
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn(username);
        when(jws.getPayload()).thenReturn(claims);
        return jws;
    }

    @Test
    public void givenPlugin_whenAccessingLabelsApplicator_thenNotNull() throws MalformedLabelsException {
        // Given and When
        SecurityLabelsApplicator applicator =
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
        SecurityLabelsParser parser = this.plugin.labelsParser();
        SecurityLabels<?> labels = parser.parseSecurityLabels(rawLabel);

        // Then
        Assert.assertNotNull(labels);
    }

    @Test(dataProvider = "validLabels", expectedExceptions = MalformedLabelsException.class)
    public void givenValidLabelPrependedWithBadPrefix_whenParsing_thenExceptionThrown(byte[] rawLabel) throws
            MalformedLabelsException {
        // Given
        SecurityLabelsParser parser = this.plugin.labelsParser();
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
        SecurityLabelsParser parser = this.plugin.labelsParser();
        SecurityLabels<?> labels = parser.parseSecurityLabels(rawLabel);

        // Then
        Assert.assertNotNull(labels);
    }

    /**
     * Provides one/more example labels that when evaluated by the plugins {@link Authorizer} implementation against the
     * user entitlements provided by {@link #getTestEntitlements()} should return {@code true} for
     * {@link Authorizer#canRead(SecurityLabels)}
     *
     * @return Accessible Labels
     */
    @DataProvider(name = "accessibleLabels")
    protected abstract Object[][] accessibleLabels();

    /**
     * Provides one/more example labels that when evaluated by the plugins {@link Authorizer} implementation against the
     * user entitlements provided by {@link #getTestEntitlements()} should return {@code false} for
     * {@link Authorizer#canRead(SecurityLabels)}
     *
     * @return Forbidden Labels
     */
    @DataProvider(name = "forbiddenLabels")
    protected abstract Object[][] forbiddenLabels();

    @Test(dataProvider = "accessibleLabels")
    public void givenAccessibleLabel_whenMakingAccessDecisions_thenCorrectDecisionIsMade(byte[] rawLabel) throws
            MalformedLabelsException, MalformedEntitlementsException {
        // Given
        SecurityLabelsParser parser = this.plugin.labelsParser();
        SecurityLabels<?> label = parser.parseSecurityLabels(rawLabel);
        Authorizer authorizer = this.plugin.prepareAuthorizer(this.getTestEntitlements());

        // When
        boolean decision = authorizer.canRead(label);

        // Then
        Assert.assertTrue(decision);
    }

    @Test(dataProvider = "forbiddenLabels")
    public void givenForbiddenLabel_whenMakingAccessDecisions_thenCorrectDecisionIsMade(byte[] rawLabel) throws
            MalformedLabelsException, MalformedEntitlementsException {
        // Given
        SecurityLabelsParser parser = this.plugin.labelsParser();
        SecurityLabels<?> label = parser.parseSecurityLabels(rawLabel);
        Authorizer authorizer = this.plugin.prepareAuthorizer(this.getTestEntitlements());

        // When
        boolean decision = authorizer.canRead(label);

        // Then
        Assert.assertFalse(decision);
    }
}
