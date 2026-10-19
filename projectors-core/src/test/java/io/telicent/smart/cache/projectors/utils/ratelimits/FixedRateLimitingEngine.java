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
package io.telicent.smart.cache.projectors.utils.ratelimits;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.telicent.smart.cache.projectors.utils.WriteOnceReference;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@ToString
public class FixedRateLimitingEngine extends RateLimitingEngine<List<RateLimitSpec>> {

    @Builder.Default
    private final String target = "<unknown>";
    private final String user;
    private final RateLimiterRegistry registry = RateLimiterRegistry.ofDefaults();
    private final WriteOnceReference<RateLimiterConfig> config = new WriteOnceReference<>();

    @Override
    protected RateLimiterRegistry getRegistry(List<RateLimitSpec> context) {
        return this.registry;
    }

    @Override
    protected List<RateLimitSpec> discoverRateLimits(List<RateLimitSpec> context) {
        return context;
    }

    @Override
    protected RateLimiterConfig getOrCreateConfiguration(RateLimitSpec limit) {
        return this.config.computeIfAbsent(() -> RateLimitConfig.toConfig(limit));
    }

    @Override
    protected String getRequestTarget(List<RateLimitSpec> context) {
        return this.target;
    }

    @Override
    protected String getUser(List<RateLimitSpec> context) {
        return this.user;
    }

}
