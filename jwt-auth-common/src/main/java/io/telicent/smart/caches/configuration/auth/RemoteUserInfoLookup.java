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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

public class RemoteUserInfoLookup implements UserInfoLookup {
    private final HttpClient http;
    private final ObjectMapper objectMapper;
    private final Duration timeout;

    public RemoteUserInfoLookup() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(),
                new ObjectMapper(),
                Duration.ofSeconds(10));
    }

    public RemoteUserInfoLookup(HttpClient httpClient, ObjectMapper objectMapper, Duration timeout) {
        this.http = Objects.requireNonNull(httpClient);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.timeout = timeout != null ? timeout : Duration.ofSeconds(10);
    }

    @Override
    public UserInfo lookup(String userInfoEndpoint, String bearerToken) throws UserInfoLookupException {
        if (userInfoEndpoint == null || userInfoEndpoint.isBlank()) {
            throw new UserInfoLookupException("userInfoEndpoint must be provided");
        }
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new UserInfoLookupException("bearerToken must be provided");
        }

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(userInfoEndpoint))
                    .timeout(timeout)
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + bearerToken)
                    .GET()
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

            int status = resp.statusCode();
            String body = resp.body();

            if (status == 200) {
                try {
                    return objectMapper.readValue(body, UserInfo.class);
                } catch (IOException ex) {
                    throw new UserInfoLookupException("Failed to parse userinfo response", ex);
                }
            } else if (status == 404) {
                throw new UserInfoLookupException("Endpoint " + userInfoEndpoint + " not found");
            } else if (status == 401 || status == 403) {
                throw new UserInfoLookupException("Unauthorized when calling userinfo endpoint (status " + status + ")");
            } else {
                throw new UserInfoLookupException("Unexpected status " + status + " from userinfo endpoint. Body: " + body);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new UserInfoLookupException("Request interrupted", ex);
        } catch (IOException ex) {
            throw new UserInfoLookupException("I/O error when calling userinfo endpoint", ex);
        } catch (IllegalArgumentException ex) {
            throw new UserInfoLookupException("Invalid userInfoEndpoint URL", ex);
        }
    }

    @Override
    public void close() throws IOException {
        // HttpClient does not allow for explicit shutdown
    }
}
