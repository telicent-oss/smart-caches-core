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
package io.telicent.smart.cache.projectors.sinks;

import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.sinks.builder.AbstractForwardingSinkBuilder;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A sink that passes items through unchanged <strong>BUT</strong> manages a set of {@link Closeable} resources that are
 * closed when the sinks {@link #close()} method is called.  The sink guarantees to close those resources regardless of
 * whether the destination sink closure fails, or any individual {@link Closeable#close()} fails.
 * <p>
 * This is useful for pipelines where there are leakable resources used that aren't easily encapsulated in another sink
 * implementation that will close those resources.
 * </p>
 * <p>
 * Generally it is recommended that this sink be placed first in the chain of sinks as it will propagate the
 * {@link #close()} call down to the destination sink prior to closing the resources it has been instructed to clean
 * up.
 * </p>
 *
 * @param <T> Item type
 */
@ToString(callSuper = true)
public class CleanupSink<T> extends AbstractTransformingSink<T, T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupSink.class);

    private final List<Closeable> cleanups = new ArrayList<>();

    /**
     * Creates a new cleanup sink
     *
     * @param destination Destination
     * @param cleanups    Resources to clean up
     */
    CleanupSink(Sink<T> destination, List<Closeable> cleanups) {
        super(destination);
        this.cleanups.addAll(Objects.requireNonNull(cleanups, "Resources to cleanup cannot be null"));
        this.cleanups.removeIf(Objects::isNull);
    }

    @Override
    protected T transform(T item) {
        return item;
    }

    /**
     * Gets the number of {@link Closeable} resources held for cleanup by this sink
     *
     * @return Resources count
     */
    public int resourcesCount() {
        return this.cleanups.size();
    }

    @Override
    public void close() {
        try {
            super.close();
        } finally {
            LOGGER.info("Cleaning up {} closeable resources...", cleanups.size());
            for (Closeable closeable : cleanups) {
                try {
                    closeable.close();
                    LOGGER.info("Cleaned up resource {}", closeable);
                } catch (Throwable e) {
                    LOGGER.warn("Failed to clean up resource {}", closeable, e);
                }
            }
            LOGGER.info("Cleaned up {} closeable resources", cleanups.size());
        }
    }

    /**
     * Creates a new cleanup sink builder
     *
     * @param <TItem> Item type
     * @return Cleanup sink builder
     */
    public static <TItem> Builder<TItem> create() {
        return new Builder<>();
    }

    /**
     * Builder for cleanup sinks
     *
     * @param <TItem> Item type
     */
    public static class Builder<TItem>
            extends AbstractForwardingSinkBuilder<TItem, TItem, CleanupSink<TItem>, Builder<TItem>> {
        private final List<Closeable> cleanups = new ArrayList<>();

        /**
         * Adds a single closeable resource to clean up
         *
         * @param closeable Closeable resource
         * @return Builder
         */
        public Builder<TItem> resource(Closeable closeable) {
            if (closeable != null) {
                this.cleanups.add(closeable);
            }
            return this;
        }

        /**
         * Adds a single auto-closeable resource to clean up
         * <p>
         * Note that since {@link CleanupSink} manages {@link Closeable} resources by default, and {@link AutoCloseable}
         * resources have a slightly different interface the provided {@link AutoCloseable} will be wrapped into an
         * anonymous {@link Closeable}.
         * </p>
         *
         * @param closeable Auto-closeable resource
         * @return Builder
         */
        public Builder<TItem> resource(final AutoCloseable closeable) {
            if (closeable != null) {
                this.cleanups.add(new Closeable() {
                    @Override
                    public void close() throws IOException {
                        // NB - AutoCloseable throws a different exception type so catch and wrap if necessary
                        try {
                            closeable.close();
                        } catch (Exception e) {
                            throw new IOException(e);
                        }
                    }

                    @Override
                    public String toString() {
                        // NB - Make sure we preserve access to the original toString() when wrapping it as the
                        //      CleanupSink uses these values in logging, if we don't do this then useful information
                        //      may get lost from the logs
                        return closeable.toString();
                    }
                });
            }
            return this;
        }

        /**
         * Adds multiple closeable resource(s) to clean up
         *
         * @param resources Closeable resources
         * @return Builder
         */
        public Builder<TItem> resources(Closeable... resources) {
            if (resources != null) {
                CollectionUtils.addAll(this.cleanups, resources);
            }
            return this;
        }

        /**
         * Adds multiple closeable resource(s) to clean up
         *
         * @param resources Closeable resources
         * @return Builder
         */
        public Builder<TItem> resources(Collection<Closeable> resources) {
            if (resources != null) {
                CollectionUtils.addAll(this.cleanups, resources);
            }
            return this;
        }

        @Override
        public CleanupSink<TItem> build() {
            this.cleanups.removeIf(Objects::isNull);
            return new CleanupSink<>(this.getDestination(), this.cleanups);
        }
    }

}
