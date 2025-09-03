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

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.PureJavaCrc32;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A helper for document integrity checking, used by both {@link SplitterSink} and TODO.  Serves to ensure that the
 * logic of how Checksums and Hashes are calculated doesn't diverge between the two.
 */
public final class ChunkIntegrityHelper {

    private final PureJavaCrc32 crc32 = new PureJavaCrc32();
    private final MessageDigest sha256;

    /**
     * Creates a new instance of the helper
     */
    public ChunkIntegrityHelper() {
        try {
            this.sha256 = MessageDigest.getInstance("SHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to initialize SHA256 algorithm.", e);
        }
    }

    /**
     * Calculates a CRC32 checksum for the given data
     *
     * @param data Data
     * @return Checksum
     */
    public long calculateChecksum(byte[] data) {
        synchronized (this.crc32) {
            crc32.update(data);
            long fullChecksum = crc32.getValue();
            crc32.reset();
            return fullChecksum;
        }
    }

    /**
     * Calculates a SHA256 hash for the given data
     *
     * @param data Data
     * @return SHA256 Hash encoded into a string using Hexadecimal
     */
    public String calculateHash(byte[] data) {
        String hash;
        synchronized (this.sha256) {
            this.sha256.update(data);
            hash = Hex.encodeHexString(this.sha256.digest());
            this.sha256.reset();
        }
        return hash;
    }
}
