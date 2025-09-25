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
package io.telicent.smart.cache.server.jaxrs.init;

import io.jsonwebtoken.Identifiable;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.LocatorAdapter;
import io.jsonwebtoken.security.InvalidKeyException;
import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.JwkSet;
import io.telicent.servlet.auth.jwt.JwtServletConstants;
import io.telicent.servlet.auth.jwt.verification.SignedJwtVerifier;
import io.telicent.smart.cache.server.jaxrs.auth.JwtAuthEngineWithProblemChallenges;
import jakarta.servlet.ServletContextEvent;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.util.Objects;
import java.util.stream.Collectors;

public class MockKeyServerInit implements ServerConfigInit {
    private static final Logger LOGGER = LoggerFactory.getLogger(MockKeyServerInit.class);

    @Override
    public String getName() {
        return "Mock Key Server Self-Auth Initialisation";
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        SignedJwtVerifier verifier = new SignedJwtVerifier(
                new InternalJwksLocator((JwkSet) sce.getServletContext().getAttribute("jwks")));
        LOGGER.info("Using JwtVerifier: {}", verifier);
        sce.getServletContext()
           .setAttribute(JwtServletConstants.ATTRIBUTE_JWT_VERIFIER, verifier);
        sce.getServletContext()
           .setAttribute(JwtServletConstants.ATTRIBUTE_JWT_ENGINE, new JwtAuthEngineWithProblemChallenges());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServerConfigInit.super.contextDestroyed(sce);
    }

    @AllArgsConstructor
    private final static class InternalJwksLocator extends LocatorAdapter<Key> {

        private final JwkSet jwks;

        @Override
        protected Key locate(JwsHeader header) {
            Jwk<?> jwk = this.jwks.getKeys()
                                  .stream()
                                  .filter(k -> Objects.equals(k.getId(), header.getKeyId()))
                                  .findFirst()
                                  .orElse(null);
            if (jwk == null) {
                throw new InvalidKeyException("No Key ID " + header.getKeyId() + " known on this server");
            }
            return jwk.toKey();
        }

        @Override
        public String toString() {
            return "InternalJwksLocator{" + this.jwks.getKeys().size() + " key(s): [" + this.jwks.getKeys()
                                                                                                 .stream()
                                                                                                 .map(Identifiable::getId)
                                                                                                 .filter(StringUtils::isNotBlank)
                                                                                                 .collect(
                                                                                                         Collectors.joining(
                                                                                                                 ", ")) + "]}";
        }
    }
}
