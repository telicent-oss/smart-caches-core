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
package io.telicent.smart.cache.projectors.sinks.events.splitter;

import org.apache.commons.codec.digest.PureJavaCrc32;

import java.util.Locale;

/**
 * The default helper for document integrity checking, uses {@link PureJavaCrc32} for the checksums, and SHA256 for the
 * hashes.
 */
public final class DefaultChunkIntegrityHelper extends AbstractChunkIntegrityHelper {

    /**
     * Creates a new instance of the helper
     */
    public DefaultChunkIntegrityHelper() {
        super(new PureJavaCrc32(), ChunkIntegrityHelper.CHECKSUM_ALGORITHM_CRC_32,
              ChunkIntegrityHelper.HASH_ALGORITHM_SHA_256.toUpperCase(
                      Locale.ROOT));
    }
}
