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

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.util.Date;

@Getter
@Builder(builderMethodName = "create")
@ToString
@EqualsAndHashCode
@Jacksonized
public class Metadata implements Serializable {

    @NonNull
    private final String generatedBy, generatorVersion, documentFormat;
    @NonNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Date generatedAt;

}
