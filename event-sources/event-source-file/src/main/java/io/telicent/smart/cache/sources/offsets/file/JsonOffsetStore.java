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
package io.telicent.smart.cache.sources.offsets.file;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

/**
 * A file backed offset store with offsets serialised as JSON
 */
public class JsonOffsetStore extends AbstractJacksonOffsetStore {

    /**
     * Creates a new file backed offset store that will use Jackson's JSON support to serialise the offsets to the file
     * as JSON
     *
     * @param offsetsFile Offsets file
     */
    public JsonOffsetStore(File offsetsFile) {
        super(new ObjectMapper().enable(DeserializationFeature.USE_LONG_FOR_INTS), offsetsFile);
    }
}
