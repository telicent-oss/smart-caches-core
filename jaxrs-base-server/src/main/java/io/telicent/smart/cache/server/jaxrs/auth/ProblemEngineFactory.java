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
package io.telicent.smart.cache.server.jaxrs.auth;

import io.telicent.servlet.auth.jwt.JwtAuthenticationEngine;
import io.telicent.servlet.auth.jwt.jaxrs3.JaxRs3EngineProvider;
import io.telicent.servlet.auth.jwt.sources.HeaderSource;

import java.util.List;

/**
 * An authentication engine factory that creates {@link JwtAuthEngineWithProblemChallenges} instances
 */
public class ProblemEngineFactory extends JaxRs3EngineProvider {

    @Override
    public int priority() {
        // Declare our priority as 1 higher than the default JAX-RS engine provider so that we are used in preference to
        // that
        return super.priority() + 1;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <TRequest, TResponse> JwtAuthenticationEngine<TRequest, TResponse> createEngine(
            List<HeaderSource> headerSources, String realm, List<String> usernameClaims, String[] rolesClaim) {
        return (JwtAuthenticationEngine<TRequest, TResponse>) new JwtAuthEngineWithProblemChallenges(headerSources,
                                                                                                     realm,
                                                                                                     usernameClaims.toArray(
                                                                                                             new String[0]),
                                                                                                     rolesClaim);
    }
}
