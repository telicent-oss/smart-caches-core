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
package io.telicent.smart.cache.sources.kafka.serializers;

import org.apache.jena.riot.Lang;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

public abstract class AbstractRdfDeserializerTests<T> {

    /**
     * Creates a fresh instance of the deserializer to be tested
     *
     * @return Deserializer
     */
    protected abstract AbstractRdfDeserializer<T> createDeserializer();

    /**
     * Gets the default language for the deserializer if no other explicit configuration
     *
     * @return Default language
     */
    protected abstract Lang getDefaultLanguage();

    @Test
    public void deserialize_configure_01() {
        try (AbstractRdfDeserializer<T> deserializer = createDeserializer()) {
            Assert.assertEquals(deserializer.getDefaultLang(), getDefaultLanguage());

            deserializer.configure(Collections.emptyMap(), false);
            Assert.assertEquals(deserializer.getDefaultLang(), getDefaultLanguage());
        }
    }
}
