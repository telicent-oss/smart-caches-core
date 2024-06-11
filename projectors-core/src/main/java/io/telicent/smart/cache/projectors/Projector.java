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

/**
 * Projects from input(s) to output(s)
 *
 * @param <TInput>  Input type
 * @param <TOutput> Output type
 */
public interface Projector<TInput, TOutput> {

    /**
     * Projects the inputs to the output sink applying whatever projection logic is necessary to transform the input
     * format into the desired output format.
     * <p>
     * A given input may produce many outputs hence the use of the {@link Sink} abstraction.
     * </p>
     *
     * @param input Input
     * @param sink  Output sink
     */
    void project(TInput input, Sink<TOutput> sink);
}
