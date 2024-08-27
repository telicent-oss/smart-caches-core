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
package io.telicent.smart.cache.server.jaxrs.applications;

import io.telicent.jena.abac.AttributeValueSet;
import io.telicent.jena.abac.Hierarchy;
import io.telicent.jena.abac.attributes.Attribute;
import io.telicent.jena.abac.attributes.AttributeValue;
import io.telicent.jena.abac.attributes.ValueTerm;
import io.telicent.jena.abac.core.AttributesStore;
import io.telicent.jena.abac.core.AttributesStoreLocal;
import io.telicent.jena.abac.core.AttributesStoreRemote;
import io.telicent.smart.cache.server.jaxrs.utils.RandomPortProvider;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

public class TestAttributesServer {

    private static final RandomPortProvider PORT = new RandomPortProvider(17333);

    @BeforeMethod
    public void setup() {
        System.getProperties().remove(AttributesStore.class);
    }


    @Test
    public void givenMockAttributesServerWithNoStore_whenStartingAndLookingUpAttributes_thenNullIsReturned() throws
            IOException {
        // Given
        MockAttributesServer server = new MockAttributesServer(PORT.newPort(), null);
        try {
            // When
            server.start();
            AttributesStoreRemote remote = createRemoteStore(server);
            AttributeValueSet values = remote.attributes("test");

            // Then
            Assert.assertNull(values);
        } finally {
            server.shutdown();
        }
    }

    @Test
    public void givenMockAttributesServerWithValidStore_whenStartingAndLookingUpAttributes_thenAttributesAreReturned() throws
            IOException {
        // Given
        AttributeValueSet expected = AttributeValueSet.of(
                List.of(AttributeValue.of("name", ValueTerm.value("Thomas T. Test")),
                        AttributeValue.of("admin", ValueTerm.value(true))));
        AttributesStoreLocal local = new AttributesStoreLocal();
        local.put("test", expected);
        MockAttributesServer server = new MockAttributesServer(PORT.newPort(), local);
        try {
            // When
            server.start();
            AttributesStoreRemote remote = createRemoteStore(server);
            AttributeValueSet actual = remote.attributes("test");

            // Then
            Assert.assertNotNull(actual);
            Assert.assertEquals(actual, expected);
        } finally {
            server.shutdown();
        }
    }

    @Test
    public void givenMockAttributesServerWithNoStore_whenStartingAndLookingUpHierarchies_thenNullIsReturned() throws
            IOException {
        // Given
        MockAttributesServer server = new MockAttributesServer(PORT.newPort(), null);
        try {
            // When
            server.start();
            AttributesStoreRemote remote = createRemoteStore(server);
            Hierarchy hierarchy = remote.getHierarchy(Attribute.create("clearance"));

            // Then
            Assert.assertNull(hierarchy);
        } finally {
            server.shutdown();
        }
    }

    @Test
    public void givenMockAttributesServerWithHierarchyAwareStore_whenStartingAndLookingUpHierarchies_thenHierarchyIsReturned() throws
            IOException {
        // Given
        AttributesStoreLocal local = new AttributesStoreLocal();
        Attribute key = Attribute.create("clearance");
        local.addHierarchy(Hierarchy.create(key, "P", "O", "S", "TS"));
        MockAttributesServer server = new MockAttributesServer(PORT.newPort(), local);
        try {
            // When
            server.start();
            AttributesStoreRemote remote = createRemoteStore(server);
            Hierarchy hierarchy = remote.getHierarchy(key);

            // Then
            Assert.assertNotNull(hierarchy);
            Assert.assertEquals(hierarchy.values().size(), 4);
        } finally {
            server.shutdown();
        }
    }

    private static AttributesStoreRemote createRemoteStore(MockAttributesServer server) {
        return new AttributesStoreRemote(server.getUserLookupUrl(), server.getHierarchyLookupUrl());
    }
}
