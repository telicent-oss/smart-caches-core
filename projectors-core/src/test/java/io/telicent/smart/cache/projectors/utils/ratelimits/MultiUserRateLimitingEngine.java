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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiUserRateLimitingEngine extends RateLimitingEngine<Context> {

    private final RateLimiterRegistry registry = RateLimiterRegistry.ofDefaults();
    private final Map<String, RateLimiterConfig> configs = new HashMap<>();

    @Override
    protected RateLimiterRegistry getRegistry(Context context) {
        return this.registry;
    }

    @Override
    protected List<RateLimitSpec> discoverRateLimits(Context context) {
        return context.limits();
    }

    @Override
    protected RateLimiterConfig getOrCreateConfiguration(RateLimitSpec limit) {
        return configs.computeIfAbsent(limit.getName(), l -> RateLimitConfig.toConfig(limit));
    }

    @Override
    protected String getRequestTarget(Context context) {
        return "<unknown>";
    }

    @Override
    protected String getUser(Context context) {
        return context.user();
    }
}
