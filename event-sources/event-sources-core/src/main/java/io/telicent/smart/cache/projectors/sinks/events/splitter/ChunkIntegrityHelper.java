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

/**
 * A helper for document integrity checking, used by both {@link SplitterSink} and
 * {@link io.telicent.smart.cache.sources.combiner.CombiningEventSource} so that the logic of how Checksums and Hashes
 * are calculated doesn't diverge between the two.
 */
public interface ChunkIntegrityHelper {
    /**
     * Algorithm identifier for CRC32 as used in {@link io.telicent.smart.cache.sources.TelicentHeaders#CHUNK_CHECKSUM}
     * and {@link io.telicent.smart.cache.sources.TelicentHeaders#ORIGINAL_CHECKSUM} headers
     */
    String CHECKSUM_ALGORITHM_CRC_32 = "crc32";
    /**
     * Algorithm identifier for SHA256 as used in {@link io.telicent.smart.cache.sources.TelicentHeaders#CHUNK_HASH} and
     * {@link io.telicent.smart.cache.sources.TelicentHeaders#ORIGINAL_HASH} headers
     */
    String HASH_ALGORITHM_SHA_256 = "sha256";

    /**
     * Gets a short algorithm identifier for the Checksum algorithm used
     *
     * @return Checksum algorithm identifier e.g. {@code crc32}
     */
    String checksumAlgorithm();

    /**
     * Gets a short algorithm identifier for the Hash algorithm used
     *
     * @return Hash algorithm identifier e.g. {@code sha256}
     */
    String hashAlgorithm();

    /**
     * Calculates a checksum for the given data
     *
     * @param data Data
     * @return Checksum
     */
    long calculateChecksum(byte[] data);

    /**
     * Calculates a hash for the given data
     *
     * @param data Data
     * @return Hash encoded into a string using Hexadecimal
     */
    String calculateHash(byte[] data);
}
