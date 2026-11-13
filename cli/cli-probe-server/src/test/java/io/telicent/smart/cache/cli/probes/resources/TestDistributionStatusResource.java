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
package io.telicent.smart.cache.cli.probes.resources;

import jakarta.servlet.ServletContext;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.function.Supplier;

public class TestDistributionStatusResource {
    private ServletContext context(Supplier<?> supplier) {
        return (ServletContext) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { ServletContext.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("getAttribute")) {
                        Assert.assertEquals(args[0], DistributionStatusResource.ATTRIBUTE);
                        return supplier;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }
    @Test
    public void configuredDiagnosticsAreReturnedWithoutCredentials() {
        var snapshot = Map.of("application", "extractor", "distributions", Map.of("dist-a", "Active"));
        try (var response = new DistributionStatusResource().status(context(() -> snapshot))) {
            Assert.assertEquals(response.getStatus(), 200);
            Assert.assertEquals(response.getEntity(), snapshot);
            Assert.assertEquals(response.getHeaderString("Cache-Control"), "no-store");
        }
    }
    @Test
    public void missingSupplierHasNoDiagnosticInventory() {
        try (var response = new DistributionStatusResource().status(context(null))) {
            Assert.assertEquals(response.getStatus(), 404);
            Assert.assertNull(response.getEntity());
        }
    }
    @Test
    public void unconfiguredSnapshotHasNoDiagnosticInventory() {
        try (var response = new DistributionStatusResource().status(context(() -> null))) {
            Assert.assertEquals(response.getStatus(), 404);
            Assert.assertNull(response.getEntity());
        }
    }
}
