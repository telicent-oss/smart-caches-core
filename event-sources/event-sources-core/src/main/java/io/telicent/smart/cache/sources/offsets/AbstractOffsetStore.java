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
package io.telicent.smart.cache.sources.offsets;

import java.util.Objects;

/**
 * An abstract offset store implementation
 */
public abstract class AbstractOffsetStore implements OffsetStore {
    private boolean closed = false;

    /**
     * Ensures that the offset store has not been closed
     */
    protected final void ensureNotClosed() {
        if (this.closed) {
            throw new IllegalStateException("Offset Store has been closed");
        }
    }

    @Override
    public final boolean hasOffset(String key) {
        ensureNotClosed();
        Objects.requireNonNull(key, "Offset key cannot be null");
        return hasOffsetInternal(key);
    }

    /**
     * Determines whether an offset with the given key actually exists or not
     *
     * @param key Key
     * @return True if an offset exists, false otherwise
     */
    protected abstract boolean hasOffsetInternal(String key);

    @Override
    public final <T> void saveOffset(String key, T offset) {
        ensureNotClosed();
        Objects.requireNonNull(key, "Offset key cannot be null");
        this.saveOffsetInternal(key, offset);
    }

    /**
     * Saves the offset for the given key
     *
     * @param key    Key
     * @param offset Offset
     * @param <T>    Offset type
     */
    protected abstract <T> void saveOffsetInternal(String key, T offset);

    @SuppressWarnings("unchecked")
    @Override
    public final <T> T loadOffset(String key) {
        ensureNotClosed();
        Objects.requireNonNull(key, "Offset key cannot be null");

        Object rawOffset = getRawOffset(key);
        if (rawOffset == null) {
            return null;
        }
        // This cast will succeed/fail at runtime depending on the type parameter T
        return (T) rawOffset;
    }

    /**
     * Gets the raw offset for the given key
     *
     * @param key Key
     * @return Raw offset, may be {@code null} if no offset is stored for the given key
     */
    protected abstract Object getRawOffset(String key);

    @Override
    public final void deleteOffset(String key) {
        ensureNotClosed();
        Objects.requireNonNull(key, "Offset key cannot be null");
        deleteOffsetInternal(key);
    }

    /**
     * Deletes the offset for the given key
     *
     * @param key Key
     */
    protected abstract void deleteOffsetInternal(String key);

    @Override
    public final synchronized void flush() {
        ensureNotClosed();
        this.flushInternal();
    }

    /**
     * Flushes the offsets to persistent storage (if any)
     */
    protected abstract void flushInternal();

    @Override
    public final synchronized void close() {
        if (this.closed) {
            return;
        }
        this.closeInternal();
        this.closed = true;
    }

    /**
     * Closes the offset store releasing any held resources
     */
    protected abstract void closeInternal();
}
