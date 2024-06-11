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
package io.telicent.smart.cache.projectors;

import java.util.Objects;

/**
 * A projector that simply passes the received events directly to a sink
 *
 * @param <T> Input and Output type
 */
public class NoOpProjector<T> implements Projector<T, T> {
    @Override
    public void project(T t, Sink<T> sink) {
        Objects.requireNonNull(t, "Input cannot be null");
        Objects.requireNonNull(sink, "Output sink cannot be null");
        sink.send(t);
    }
}
