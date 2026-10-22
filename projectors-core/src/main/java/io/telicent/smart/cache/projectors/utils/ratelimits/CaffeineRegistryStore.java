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

import com.github.benmanes.caffeine.cache.Cache;
import io.github.resilience4j.core.RegistryStore;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

/**
 * A Caffeine backed instance of a Resilience4j registry store that bounds the maximum number of registered
 * instances for an application
 *
 * @param <T> Instance type
 */
@AllArgsConstructor
public final class CaffeineRegistryStore<T> implements RegistryStore<T> {

    @NonNull
    private final Cache<String, T> cache;

    @Override
    public T computeIfAbsent(String key, Function<? super String, ? extends T> mappingFunction) {
        return this.cache.get(key, mappingFunction);
    }

    @Override
    public T putIfAbsent(String key, T value) {
        return this.computeIfAbsent(key, k -> value);
    }

    @Override
    public Optional<T> find(String key) {
        return Optional.ofNullable(this.cache.getIfPresent(key));
    }

    @Override
    public Optional<T> remove(String name) {
        T oldValue = this.cache.getIfPresent(name);
        this.cache.invalidate(name);
        return Optional.ofNullable(oldValue);
    }

    @Override
    public Optional<T> replace(String name, T newEntry) {
        T oldValue = this.cache.getIfPresent(name);
        this.cache.put(name, newEntry);
        return Optional.ofNullable(oldValue);
    }

    @Override
    public Collection<T> values() {
        return this.cache.asMap().values();
    }
}
