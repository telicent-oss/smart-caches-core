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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.*;
import java.util.function.Supplier;

/** Lightweight internal adapter for applications without a JAX-RS probe server. */
public final class DistributionStatusServer implements AutoCloseable {
    private final HttpServer server;
    private final ExecutorService executor = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "distribution-status");
        thread.setDaemon(true);
        return thread;
    });
    public DistributionStatusServer(int port, String token, Supplier<?> snapshot) throws IOException {
        if (token == null || token.isBlank()) throw new IllegalArgumentException("A collector token is required");
        byte[] expected = ("Bearer " + token).getBytes(StandardCharsets.UTF_8);
        ObjectMapper json = new ObjectMapper();
        server = HttpServer.create(new InetSocketAddress(port), 8);
        server.setExecutor(executor);
        server.createContext("/admin/distributions", exchange -> {
            try {
                if (!exchange.getRequestURI().getPath().equals("/admin/distributions")) {
                    exchange.sendResponseHeaders(404, -1); return;
                }
                String authorization = exchange.getRequestHeaders().getFirst("Authorization");
                if (authorization == null || !MessageDigest.isEqual(expected, authorization.getBytes(StandardCharsets.UTF_8))) {
                    exchange.sendResponseHeaders(401, -1); return;
                }
                if (!exchange.getRequestMethod().equals("GET")) {
                    exchange.getResponseHeaders().set("Allow", "GET");
                    exchange.sendResponseHeaders(405, -1); return;
                }
                byte[] payload;
                try { payload = json.writeValueAsBytes(snapshot.get()); }
                catch (Exception error) { exchange.sendResponseHeaders(503, -1); return; }
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Cache-Control", "no-store");
                exchange.sendResponseHeaders(200, payload.length);
                exchange.getResponseBody().write(payload);
            } finally { exchange.close(); }
        });
    }
    public void start() { server.start(); }
    public int port() { return server.getAddress().getPort(); }
    @Override
    public void close() { server.stop(0); executor.shutdownNow(); }
}
