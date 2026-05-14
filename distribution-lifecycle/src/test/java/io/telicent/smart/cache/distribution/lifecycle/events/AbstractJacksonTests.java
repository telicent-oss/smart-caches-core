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
package io.telicent.smart.cache.distribution.lifecycle.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;

public abstract class AbstractJacksonTests {

    private final ObjectMapper json;

    AbstractJacksonTests() {
        this.json = createObjectMapper();
    }

    protected ObjectMapper createObjectMapper() {
        return new ObjectMapper().enable(DeserializationFeature.USE_LONG_FOR_INTS);
    }

    /**
     * Verifies that values round trip successfully through JSON serialization and deserialization
     *
     * @param value Value
     * @param cls   Value type
     * @param <T>   Value type
     * @return Reparsed value
     * @throws JsonProcessingException Thrown if serialization/deserialization fails
     */
    protected <T> T verifyRoundTrip(T value, Class<T> cls) throws JsonProcessingException {
        // When
        T reparsed = roundTrip(value, cls);

        // Then
        Assert.assertEquals(reparsed, value);
        return reparsed;
    }

    /**
     * Round trips a value through JSON serialization and deserialization
     * <p>
     * Note unlike {@link #verifyRoundTrip(Object, Class)} this does not do an equality comparison between the original
     * and reparsed value.  This may be used when the types used are known to be reparsed as not the final target type,
     * e.g. when round tripping an {@link io.telicent.smart.cache.payloads.Envelope} the raw body will not necessarily
     * reflect the input types until {@link io.telicent.smart.cache.payloads.Envelope#getBodyAs(Class)} is used to
     * convert the body to the desired type.
     * </p>
     *
     * @param value Value
     * @param cls   Value type
     * @param <T>   Value type
     * @return Reparsed value
     * @throws JsonProcessingException Thrown if serialization/deserialization fails
     */
    protected <T> T roundTrip(T value, Class<T> cls) throws JsonProcessingException {
        // When
        String json = this.json.writeValueAsString(value);

        // Then
        Assert.assertNotNull(json);
        return this.json.readValue(json, cls);
    }
}
