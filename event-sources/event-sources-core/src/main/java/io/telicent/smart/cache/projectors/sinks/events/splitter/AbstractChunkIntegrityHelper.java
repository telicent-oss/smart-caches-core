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
import org.apache.commons.lang3.Strings;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.Checksum;

/**
 * The abstract implementation of a helper for document integrity checking
 */
public abstract class AbstractChunkIntegrityHelper implements ChunkIntegrityHelper {

    private final Checksum checksum;
    private final String checksumAlgorithmId, hashAlgorithmId;
    private final MessageDigest hash;

    /**
     * Creates a new instance of the helper
     */
    public AbstractChunkIntegrityHelper(Checksum checksumAlgorithm, String checksumAlgorithmId, String hashAlgorithm) {
        try {
            this.checksum = Objects.requireNonNull(checksumAlgorithm, "checksumAlgorithm cannot be null");
            this.checksumAlgorithmId =
                    Objects.requireNonNull(checksumAlgorithmId, "checksumAlgorithmId cannot be null");
            this.hash = MessageDigest.getInstance(hashAlgorithm);
            this.hashAlgorithmId = hashAlgorithm.toLowerCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to initialize " + hashAlgorithm + " hash algorithm.", e);
        }
    }


    @Override
    public final String checksumAlgorithm() {
        return this.checksumAlgorithmId;
    }


    @Override
    public final String hashAlgorithm() {
        return this.hashAlgorithmId;
    }

    @Override
    public final long calculateChecksum(byte[] data) {
        synchronized (this.checksum) {
            checksum.update(data);
            long fullChecksum = checksum.getValue();
            checksum.reset();
            return fullChecksum;
        }
    }

    @Override
    public final String calculateHash(byte[] data) {
        String hash;
        synchronized (this.hash) {
            this.hash.update(data);
            hash = Hex.encodeHexString(this.hash.digest());
            this.hash.reset();
        }
        return hash;
    }
}
