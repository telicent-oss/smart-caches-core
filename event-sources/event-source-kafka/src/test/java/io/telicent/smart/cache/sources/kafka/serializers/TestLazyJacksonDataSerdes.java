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

import io.telicent.smart.cache.sources.kafka.tombstones.Data;
import io.telicent.smart.cache.sources.kafka.tombstones.LazyData;
import io.telicent.smart.cache.sources.kafka.tombstones.LazyDataDeserializer;
import io.telicent.smart.cache.sources.kafka.tombstones.LazyDataSerializer;
import org.testng.Assert;

import java.nio.charset.StandardCharsets;

public class TestLazyJacksonDataSerdes extends AbstractLazyJacksonSerdesTest<Data, LazyData, LazyDataSerializer, LazyDataDeserializer> {

    public static final byte[] GOOD_JSON = """
            {
              "key": "test",
              "value": 1234
            }
            """.getBytes(StandardCharsets.UTF_8);


    @Override
    protected byte[] goodJson() {
        return GOOD_JSON;
    }

    @Override
    protected LazyData exemplarPopulated() {
        return new LazyData(new Data("test", 1234));
    }

    @Override
    protected LazyDataSerializer serializer() {
        return new LazyDataSerializer();
    }

    @Override
    protected LazyDataDeserializer deserializer() {
        return new LazyDataDeserializer();
    }

    @Override
    protected void verifyGoodData(Data data) {
        Assert.assertEquals(data.key(), "test");
        Assert.assertEquals(data.value(), 1234);
    }


}
