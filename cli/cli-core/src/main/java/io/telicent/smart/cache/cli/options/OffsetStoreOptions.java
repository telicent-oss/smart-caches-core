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
package io.telicent.smart.cache.cli.options;

import com.github.rvesse.airline.annotations.Option;
import io.telicent.smart.cache.sources.offsets.OffsetStore;
import io.telicent.smart.cache.sources.offsets.file.JsonOffsetStore;
import io.telicent.smart.cache.sources.offsets.file.YamlOffsetStore;

import java.io.File;

/**
 * Options relating to external offset stores
 */
public class OffsetStoreOptions {

    @Option(name = "--offsets-file", title = "OffsetsFile", description = "Specifies an application controlled file that will be used to store Kafka offsets in addition to Kafka Consumer Groups.")
    private File offsetsFile;

    /**
     * Gets the configured offset store (if any)
     *
     * @return Offset store if configured, otherwise {@code null}
     */
    public OffsetStore getOffsetStore() {
        if (this.offsetsFile != null) {
            if (isYamlFile(this.offsetsFile)) {
                return new YamlOffsetStore(this.offsetsFile);
            } if (isJsonFile(this.offsetsFile)) {
                return new JsonOffsetStore(this.offsetsFile);
            } else {
              throw new IllegalArgumentException("File extension not supported: " + this.offsetsFile);
            }
        }
        return null;
    }

    public static boolean isJsonFile(File file) {
        return file.getName().toLowerCase().endsWith(".json");
    }

    public static boolean isYamlFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".yaml") || fileName.endsWith(".yml");
    }
}
