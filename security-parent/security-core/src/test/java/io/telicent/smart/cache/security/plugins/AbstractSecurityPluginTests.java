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
import io.telicent.smart.cache.security.attributes.UserAttributes;
import io.telicent.smart.cache.security.attributes.AttributesParser;
import io.telicent.smart.cache.security.attributes.AttributesProvider;
import io.telicent.smart.cache.security.attributes.MalformedAttributesException;
import io.telicent.smart.cache.security.identity.IdentityProvider;
import io.telicent.smart.cache.security.labels.*;
import io.telicent.smart.cache.security.requests.MinimalRequestContext;
import io.telicent.smart.cache.security.requests.RequestContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphFactory;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Abstract test suite for security plugins, this verifies that a plugin fulfils the basic API contract
 * <p>
 * Note that individual plugins should provide their own test suites that test their plugin specific functionality
 * </p>
 */
public abstract class AbstractSecurityPluginTests {

    public static final Triple TEST_TRIPLE = Triple.create(NodeFactory.createURI("https://example.org/test"),
                                                           NodeFactory.createURI("https://example.org/test"),
                                                           NodeFactory.createLiteralString("test"));
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
    public void givenPlugin_whenAccessingAttributesParser_thenNotNull() {
        // Given and When
        AttributesParser parser = this.plugin.attributesParser();

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
    public void givenPlugin_whenAccessingAttributesProvider_thenNotNull() {
        // Given and When
        AttributesProvider provider = this.plugin.attributesProvider();

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
    public void givenPlugin_whenAccessingAuthorizer_thenNotNull() {
        // Given and When
        UserAttributes<?> userAttributes = getTestAttributes();
        Authorizer authorizer =
                this.plugin.prepareAuthorizer(userAttributes);

        // Then
        Assert.assertNotNull(authorizer);
    }

    /**
     * Gets user attributes for the {@code test} user for the purposes of testing.  Done by calling
     * {@link SecurityPlugin#attributesProvider()} and then calling
     * {@link AttributesProvider#attributesForUser(RequestContext)} by default.
     * <p>
     * If a plugin to be tested is not able to provide test attributes in that way then they should return a suitable
     * test instance.
     * </p>
     *
     * @return Test attributes
     * @throws MalformedAttributesException Thrown if the plugin can't produce test attributes
     */
    protected UserAttributes<?> getTestAttributes() {
        return this.plugin.attributesProvider()
                          .attributesForUser(new MinimalRequestContext(getTestJws("test"), "test"));
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
    @SuppressWarnings("unchecked")
    protected Jws<Claims> getTestJws(String username) {
        Jws<Claims> jws = Mockito.mock(Jws.class);
        Claims claims = Mockito.mock(Claims.class);
        when(claims.getSubject()).thenReturn(username);
        when(jws.getPayload()).thenReturn(claims);
        return jws;
    }

    @Test
    public void givenPlugin_whenAccessingLabelsApplicator_thenNotNull() {
        // Given and When
        SecurityLabelsApplicator applicator =
                this.plugin.prepareLabelsApplicator(new byte[0], GraphFactory.createGraphMem());

        // Then
        Assert.assertNotNull(applicator);
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
    public void givenValidLabel_whenParsing_thenNotNull_andHasDebugString(byte[] rawLabel) {
        // Given and When
        SecurityLabelsParser parser = this.plugin.labelsParser();
        SecurityLabels<?> labels = parser.parseSecurityLabels(rawLabel);

        // Then
        Assert.assertNotNull(labels);

        // And
        String debug = labels.toDebugString();
        Assert.assertNotNull(debug);
    }

    @Test(dataProvider = "validLabels", expectedExceptions = MalformedLabelsException.class)
    public void givenValidLabelPrependedWithJunkPrefix_whenParsing_thenExceptionThrown(byte[] rawLabel) {
        // Given
        SecurityLabelsParser parser = this.plugin.labelsParser();
        byte[] badPrefix = "junk/data=".getBytes(StandardCharsets.UTF_8);
        byte[] modifiedLabel = new byte[rawLabel.length + badPrefix.length];
        System.arraycopy(badPrefix, 0, modifiedLabel, 0, badPrefix.length);
        System.arraycopy(rawLabel, 0, modifiedLabel, badPrefix.length, rawLabel.length);

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
    public void givenInvalidLabel_whenParsing_thenExceptionThrown(byte[] rawLabel) {
        // Given and When
        SecurityLabelsParser parser = this.plugin.labelsParser();
        SecurityLabels<?> labels = parser.parseSecurityLabels(rawLabel);

        // Then
        Assert.assertNotNull(labels);
    }

    /**
     * Provides one/more example labels that when evaluated by the plugins {@link Authorizer} implementation against the
     * user attributes provided by {@link #getTestAttributes()} should return {@code true} for
     * {@link Authorizer#canRead(SecurityLabels)}
     *
     * @return Accessible Labels
     */
    @DataProvider(name = "accessibleLabels")
    protected abstract Object[][] accessibleLabels();

    /**
     * Provides one/more example labels that when evaluated by the plugins {@link Authorizer} implementation against the
     * user attributes provided by {@link #getTestAttributes()} should return {@code false} for
     * {@link Authorizer#canRead(SecurityLabels)}
     *
     * @return Forbidden Labels
     */
    @DataProvider(name = "forbiddenLabels")
    protected abstract Object[][] forbiddenLabels();

    @Test(dataProvider = "accessibleLabels")
    public void givenAccessibleLabel_whenMakingAccessDecisions_thenCorrectDecisionIsMade(byte[] rawLabel) {
        // Given
        SecurityLabelsParser parser = this.plugin.labelsParser();
        SecurityLabels<?> label = parser.parseSecurityLabels(rawLabel);
        try (Authorizer authorizer = this.plugin.prepareAuthorizer(this.getTestAttributes())) {
            // When
            boolean decision = authorizer.canRead(label);

            // Then
            Assert.assertTrue(decision);
        }
    }

    @Test(dataProvider = "forbiddenLabels")
    public void givenForbiddenLabel_whenMakingAccessDecisions_thenCorrectDecisionIsMade(byte[] rawLabel) {
        // Given
        SecurityLabelsParser parser = this.plugin.labelsParser();
        SecurityLabels<?> label = parser.parseSecurityLabels(rawLabel);
        try (Authorizer authorizer = this.plugin.prepareAuthorizer(this.getTestAttributes())) {
            // When
            boolean decision = authorizer.canRead(label);

            // Then
            Assert.assertFalse(decision);
        }
    }

    @Test(dataProvider = "validLabels")
    public void givenEmptyLabelsGraph_whenPreparingApplicator_thenDefaultApplicator(byte[] validLabel) {
        // Given
        Graph labelsGraph = GraphFactory.createDefaultGraph();

        // When
        SecurityLabelsApplicator applicator =
                this.plugin.prepareLabelsApplicator(validLabel, labelsGraph);

        // Then
        Assert.assertTrue(applicator instanceof DefaultLabelApplicator);
        SecurityLabels<?> labels = applicator.labelForTriple(TEST_TRIPLE);
        Assert.assertNotNull(labels);
        Assert.assertEquals(labels.encoded(), validLabel);
    }

    @Test(dataProvider = "validLabels")
    public void givenNullLabelsGraph_whenPreparingApplicator_thenDefaultApplicator(byte[] validLabel) {
        // Given and When
        SecurityLabelsApplicator applicator =
                this.plugin.prepareLabelsApplicator(validLabel, null);

        // Then
        Assert.assertTrue(applicator instanceof DefaultLabelApplicator);
        SecurityLabels<?> labels = applicator.labelForTriple(TEST_TRIPLE);
        Assert.assertNotNull(labels);
        Assert.assertEquals(labels.encoded(), validLabel);
    }

    @Test(dataProvider = "validLabels")
    public void givenLabelsGraph_whenPreparingApplicator_thenPluginApplicator(byte[] validLabel) {
        if (validLabel.length == 0) {
            throw new SkipException("Default Labels cannot be blank");
        }

        // Given
        Graph labelsGraph = GraphFactory.createDefaultGraph();
        labelsGraph.add(TEST_TRIPLE);

        // When
        SecurityLabelsApplicator applicator =
                this.plugin.prepareLabelsApplicator(validLabel, labelsGraph);

        // Then
        Assert.assertFalse(applicator instanceof DefaultLabelApplicator);
        SecurityLabels<?> labels = applicator.labelForTriple(TEST_TRIPLE);
        Assert.assertNotNull(labels);
        Assert.assertEquals(labels.encoded(), validLabel);
    }

    @Test(dataProvider = "accessibleLabels")
    public void givenNullAttributes_whenPreparingAuthorizer_thenNothingAuthorized(byte[] label) {
        // Given and When
        SecurityLabels<?> labels = this.plugin.labelsParser().parseSecurityLabels(label);
        try (Authorizer authorizer = this.plugin.prepareAuthorizer(null)) {
            // Then
            Assert.assertFalse(authorizer.canRead(labels));
        }
    }

    @Test(dataProvider = "accessibleLabels")
    public void givenUnsupportedAttributes_whenPreparingAuthorizer_thenNothingAuthorized(byte[] label) {
        // Given and When
        UserAttributes<?> attributes = mock(UserAttributes.class);
        SecurityLabels<?> labels = this.plugin.labelsParser().parseSecurityLabels(label);
        try (Authorizer authorizer = this.plugin.prepareAuthorizer(attributes)) {
            // Then
            Assert.assertFalse(authorizer.canRead(labels));
        }
    }
}
