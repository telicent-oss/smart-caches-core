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


import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

//TODO
// do I need it, what is it for?
// does it need to be tested?
public class HttpUserInfoLookup implements UserInfoLookup {

    private final RestTemplate restTemplate;
    private final String userInfoEndpoint;
    private final ObjectMapper objectMapper;

    public HttpUserInfoLookup(String userInfoEndpoint) {
        this.restTemplate = new RestTemplate();
        this.userInfoEndpoint = userInfoEndpoint;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public UserInfo getUserInfo(String accessToken) throws UserInfoLookupException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(userInfoEndpoint, HttpMethod.GET, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new UserInfoLookupException("Failed to fetch user info, status: " + response.getStatusCode());
            }

            return objectMapper.readValue(response.getBody(), UserInfo.class);

        } catch (Exception e) {
            throw new UserInfoLookupException("Error fetching user info", e);
        }
    }
}
