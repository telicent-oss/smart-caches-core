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

import com.github.benmanes.caffeine.cache.Caffeine;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Optional;

public class TestCaffeineRegistryStore {

    protected static final String KEY = "key";
    protected static final String VALUE = "value";
    protected static final String OTHER = "other";

    private static CaffeineRegistryStore<String> createStore(int maximumSize) {
        return new CaffeineRegistryStore<>(Caffeine.newBuilder().maximumSize(maximumSize).build());
    }

    @Test
    public void givenCaffeineRegistryStore_whenComputeIfAbsent_thenAsExpected() {
        // Given
        CaffeineRegistryStore<String> store = createStore(10);

        // When
        String stored = store.computeIfAbsent(KEY, k -> VALUE);

        // Then
        Assert.assertEquals(stored, VALUE);
        Assert.assertEquals(store.computeIfAbsent(KEY, k -> OTHER), VALUE,
                            "If value is already cached should not be computed again");
    }

    @Test
    public void givenCaffeineRegistryStore_whenPutIfAbsent_thenAsExpected() {
        // Given
        CaffeineRegistryStore<String> store = createStore(10);

        // When
        String stored = store.putIfAbsent(KEY, VALUE);

        // Then
        Assert.assertEquals(stored, VALUE);
        Assert.assertEquals(store.putIfAbsent(KEY, OTHER), VALUE,
                            "If value is already cached should not be put again");
    }

    @Test
    public void givenEmptyCaffeineRegistryStore_whenFinding_thenNothingFound() {
        // Given
        CaffeineRegistryStore<String> store = createStore(10);

        // When
        Optional<String> found = store.find(KEY);

        // Then
        Assert.assertTrue(found.isEmpty());
    }

    @Test
    public void givenCaffeineRegistryStore_whenFindingPopulatedValue_thenFound() {
        // Given
        CaffeineRegistryStore<String> store = createStore(10);
        store.putIfAbsent(KEY, VALUE);

        // When
        Optional<String> found = store.find(KEY);

        // Then
        Assert.assertTrue(found.isPresent());
        Assert.assertEquals(found.get(), VALUE);
    }

    @Test
    public void givenEmptyCaffeineRegistryStore_whenRemoving_thenNothingRemoved() {
        // Given
        CaffeineRegistryStore<String> store = createStore(10);

        // When
        Optional<String> removed = store.remove(KEY);

        // Then
        Assert.assertTrue(removed.isEmpty());
    }

    @Test
    public void givenCaffeineRegistryStore_whenRemovedPopulatedValue_thenRemoved_andNoLongerFound() {
        // Given
        CaffeineRegistryStore<String> store = createStore(10);
        store.putIfAbsent(KEY, VALUE);

        // When
        Optional<String> removed = store.remove(KEY);

        // Then
        Assert.assertTrue(removed.isPresent());
        Assert.assertEquals(removed.get(), VALUE);

        // And
        Optional<String> found = store.find(KEY);
        Assert.assertTrue(found.isEmpty());
    }

    @Test
    public void givenEmptyCaffeineRegistryStore_whenReplaced_thenNothingReplaced_andReplacedValueFound() {
        // Given
        CaffeineRegistryStore<String> store = createStore(10);

        // When
        Optional<String> replaced = store.replace(KEY, OTHER);

        // Then
        Assert.assertTrue(replaced.isEmpty());

        // And
        Optional<String> found = store.find(KEY);
        Assert.assertTrue(found.isPresent());
        Assert.assertEquals(found.get(), OTHER);
    }

    @Test
    public void givenCaffeineRegistryStore_whenReplacingPopulatedValue_thenReplaced_andReplacedValueFound() {
        // Given
        CaffeineRegistryStore<String> store = createStore(10);
        store.putIfAbsent(KEY, VALUE);

        // When
        Optional<String> replaced = store.replace(KEY, OTHER);

        // Then
        Assert.assertTrue(replaced.isPresent());
        Assert.assertEquals(replaced.get(), VALUE);

        // And
        Optional<String> found = store.find(KEY);
        Assert.assertTrue(found.isPresent());
        Assert.assertEquals(found.get(), OTHER);
    }

    @Test
    public void givenCaffeineRegistryStore_whenMaxSizeExceeded_thenSomeOlderValuesDiscarded() {
        // Given
        CaffeineRegistryStore<String> store = createStore(1000);

        // When
        for (int i = 1; i <= 5_000; i++) {
            String keyValue = Integer.toString(i);
            store.putIfAbsent(keyValue, keyValue);
        }

        // Then
        Assert.assertNotEquals(store.values().size(), 5_000, "Caffeine should have evicted some older values");
    }
}
