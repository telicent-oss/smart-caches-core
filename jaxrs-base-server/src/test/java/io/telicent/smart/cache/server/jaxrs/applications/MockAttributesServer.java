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

import io.telicent.jena.abac.core.AttributesStore;

/**
 * A Mock Attributes Server intended for testing authorisation in other JAX-RS based server modules
 */
public class MockAttributesServer extends AbstractAppEntrypoint {

    private final int port;

    /**
     * Creates a new Mock Attributes Server
     *
     * @param port  Port
     * @param store Attributes Store
     */
    public MockAttributesServer(int port, AttributesStore store) {
        if (store != null) {
            System.getProperties().put(AttributesStore.class.getCanonicalName(), store);
        } else {
            System.getProperties().remove(AttributesStore.class.getCanonicalName());
        }
        this.port = port;
    }

    @Override
    protected ServerBuilder buildServer() {
        return ServerBuilder.create()
                            .application(MockAttributesApplication.class)
                            .port(this.port)
                            .displayName("Mock Attributes Server");
    }

    /**
     * Starts the mock server
     */
    public void start() {
        this.run(false);
    }

    /**
     * Shuts down the mock server
     */
    public void shutdown() {
        this.server.shutdownNow();
    }

    public String getUserLookupUrl() {
        if (this.server == null) {
            return null;
        }
        return "http://localhost:" + this.server.getPort() + "/users/lookup/{user}";
    }

    public String getHierarchyLookupUrl() {
        if (this.server == null) {
            return null;
        }
        return "http://localhost:" + this.server.getPort() + "/hierarchies/lookup/{name}";
    }
}
