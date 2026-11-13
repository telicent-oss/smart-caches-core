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
package io.telicent.smart.cache.distribution.lifecycle.status;

import org.testng.Assert;
import org.testng.annotations.Test;
import java.net.URI;
import java.net.http.*;

public class TestDistributionStatusServer {
    @Test
    public void protectsDiagnosticInventoryAndExposesTheCommonContract() throws Exception {
        var status = new DistributionStatus();
        status.processed("dist-a");
        try (var server = new DistributionStatusServer(0, "test-token", () -> status.snapshot("graph"));
             var client = HttpClient.newHttpClient()) {
            server.start();
            var uri = URI.create("http://127.0.0.1:" + server.port() + "/admin/distributions");
            Assert.assertEquals(client.send(HttpRequest.newBuilder(uri).GET().build(),
                                            HttpResponse.BodyHandlers.ofString()).statusCode(), 401);
            var request = HttpRequest.newBuilder(uri).header("Authorization", "Bearer test-token").GET().build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Assert.assertEquals(response.statusCode(), 200);
            var body = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response.body());
            Assert.assertEquals(body.path("distributions").get(0).path("processedItems").asInt(), 1);
            Assert.assertEquals(body.path("distributions").get(0).path("state").asText(), "Unknown");
            Assert.assertEquals(response.headers().firstValue("Cache-Control").orElse(""), "no-store");
        }
    }
}
