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
package io.telicent.smart.cache.projectors.driver;

import io.telicent.smart.cache.projectors.Projector;
import io.telicent.smart.cache.projectors.Sink;

/**
 * Marker interface for projectors that need to be made aware of stalls in the projection in order to trigger some
 * action, see {@link #stalled(Sink)} for discussion.
 *
 * @param <TOutput> Output type
 */
public interface StallAwareProjector<TInput, TOutput> extends Projector<TInput, TOutput> {

    /**
     * Notifies the projector that the projection has stalled i.e. there are currently no new events available
     * <p>
     * This is intended to allow for advanced projectors that need to take some action when a stall is encountered.  For
     * example, they might generate a marker event sent to the output sink to trigger something to happen.  Another
     * possible use case is when the projector/sink is batching the output, then they might choose to commit an open
     * output batch to avoid undue delays in received data being visible in the target data store.
     * </p>
     *
     * @param sink Output sink
     */
    void stalled(Sink<TOutput> sink);
}
