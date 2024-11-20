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
package io.telicent.smart.caches.security.plugins.rdf.abac;

import io.telicent.smart.caches.security.plugins.AbstractSecurityPluginTests;
import io.telicent.smart.caches.security.plugins.SecurityPlugin;
import org.testng.annotations.DataProvider;

import java.nio.charset.StandardCharsets;

public class TestRdfAbacPlugin extends AbstractSecurityPluginTests {
    @Override
    protected SecurityPlugin<?, ?> getPlugin() {
        return new RdfAbacPlugin();
    }

    public static byte[] labelBytes(String label) {
        return label.getBytes(StandardCharsets.UTF_8);
    }

    @DataProvider(name = "validLabels")
    @Override
    protected Object[][] validLabels() {
        return new Object[][] {
                { labelBytes("") },
                { labelBytes("classification=S") },
                { labelBytes("classification=S && (org=foo || org=bar)")},
                { labelBytes("classification")}
        };
    }

    @DataProvider(name = "invalidLabels")
    @Override
    protected Object[][] invalidLabels() {
        return new Object[][] {
                { labelBytes("classification=") },
                { labelBytes("white space=bad") },
        };
    }
}
