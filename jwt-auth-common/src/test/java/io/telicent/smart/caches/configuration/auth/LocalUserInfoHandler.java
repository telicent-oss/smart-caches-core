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

package io.telicent.smart.caches.configuration.auth;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import io.jsonwebtoken.*;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.*;

public class LocalUserInfoHandler {

    private final PublicKey publicKey;
    private HttpServer server;

    public LocalUserInfoHandler(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/userinfo", new UserInfoHandler());
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    private class UserInfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String auth = exchange.getRequestHeaders().getFirst("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                sendResponse(exchange, 401, "{\"error\":\"Missing token\"}");
                return;
            }

            String token = auth.substring("Bearer ".length());

            try {
                // Extracts JWT header and checks for keyId
                String headerJson = new String(
                        Base64.getUrlDecoder().decode(token.split("\\.")[0]),
                        StandardCharsets.UTF_8
                );
                Map<?,?> headerMap = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(headerJson, Map.class);
                String kid = (String) headerMap.get("kid");
                if (kid == null) {
                    sendResponse(exchange, 401, "{\"error\":\"Missing key ID in token\"}");
                    return;
                }

                Jws<Claims> jws = Jwts.parser()
                        .verifyWith(publicKey)
                        .build()
                        .parseSignedClaims(token);

                Claims claims = jws.getPayload();
                if ("force-error".equals(claims.getSubject())) {
                    sendResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
                    return;
                }
                else if ("malformed-json".equals(claims.getSubject())) {
                    sendResponse(exchange, 200, "{ this is not valid JSON ");
                    return;
                }

                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("sub", claims.getSubject());
                userInfo.put("roles", claims.getOrDefault("roles", List.of("USER")));
                userInfo.put("preferred_name", claims.get("preferred_name", String.class));
                userInfo.put("permissions", claims.getOrDefault("permissions", List.of("api.read")));
                userInfo.put("attributes", claims.getOrDefault("attributes", Map.of()));

                String body = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(userInfo);
                sendResponse(exchange, 200, body);

            } catch (Exception ex) {
                sendResponse(exchange, 401, "{\"error\":\"Invalid token: " + ex.getMessage() + "\"}");
            }
        }

        private void sendResponse(HttpExchange exchange, int status, String body) throws IOException {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, body.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body.getBytes());
            }
        }
    }
}

