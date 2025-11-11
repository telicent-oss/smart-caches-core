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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Builder;
import lombok.ToString;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A caching decorator for {@link UserInfoLookup} instances
 */
@ToString
public class CachingUserInfoLookup implements UserInfoLookup {

    private final UserInfoLookup delegate;
    private final int cacheSize;
    private final Duration cacheDuration;
    @ToString.Exclude
    private final Cache<String, UserInfo> cache;

    /**
     * Creates a new caching decorator
     *
     * @param delegate      Underlying lookup
     * @param cacheSize     Maximum cache size
     * @param cacheDuration How long to cache user info for
     */
    @Builder
    public CachingUserInfoLookup(UserInfoLookup delegate, int cacheSize, Duration cacheDuration) {
        this.delegate = Objects.requireNonNull(delegate, "delegate lookup cannot be null");
        if (cacheSize <= 0) {
            throw new IllegalArgumentException("cacheSize must be positive");
        }
        this.cacheSize = cacheSize;
        this.cacheDuration = Objects.requireNonNull(cacheDuration, "cacheDuration cannot be null");
        if (this.cacheDuration.compareTo(Duration.ZERO) <= 0) {
            throw new IllegalArgumentException("cacheDuration must be positive");
        }
        this.cache = Caffeine.newBuilder().maximumSize(cacheSize).expireAfterWrite(cacheDuration).build();
    }

    @Override
    public UserInfo lookup(String bearerToken) throws UserInfoLookupException {
        final AtomicReference<UserInfoLookupException> errorHolder = new AtomicReference<>();
        UserInfo info = this.cache.get(bearerToken, t -> {
            try {
                return this.delegate.lookup(t);
            } catch (UserInfoLookupException e) {
                errorHolder.set(e);
                return null;
            }
        });
        if (errorHolder.get() != null) {
            throw errorHolder.get();
        } else {
            return info;
        }
    }

    @Override
    public void close() throws IOException {
        this.cache.invalidateAll();
        this.delegate.close();
    }
}
