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
package io.telicent.smart.cache.payloads;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;
import java.util.UUID;

/**
 * A generic envelope payload that provides a unique identifier, common metadata and a free-form body
 */
@Getter
@SuperBuilder(builderMethodName = "create")
@ToString
@EqualsAndHashCode
@Jacksonized
public class Envelope {

    /**
     * A Jackson {@link ObjectMapper} used to convert body values to different types, this is utilised by the
     * {@link #getBodyAs(Class)} method.
     * <p>
     * This enables the Java Time module (so Date's, Instant's etc) work seamlessly and disables the writing of dates as
     * timestamps in favour of Jackson's string formatting which is to write them as ISO8601 compliant date times.
     * </p>
     * <p>
     * This static field is public so developers can ensure that the same object mapper configuration is used when other
     * code, e.g. Kafka serdes, needs to read/write this type.  It also enables writing test cases against this type,
     * and any derived types that use the same Jackson {@link ObjectMapper} configuration as the type itself uses.
     * </p>
     */
    //@formatter:off
    public static final ObjectMapper JSON
            = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    //@formatter:on

    @NonNull
    private final UUID id;
    @NonNull
    private final Metadata metadata;
    @NonNull
    private Map<String, Object> body;

    /**
     * Gets the body converted into another class, assuming that the conversion is valid
     *
     * @param bodyClass Body Class
     * @param <T>       Body type
     * @return Converted body
     * @throws ClassCastException Thrown if the body is not convertible to the provided class
     */
    public <T> T getBodyAs(Class<T> bodyClass) {
        return JSON.convertValue(this.body, bodyClass);
    }
}
