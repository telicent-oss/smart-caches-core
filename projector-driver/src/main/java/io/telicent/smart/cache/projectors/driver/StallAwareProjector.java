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
 * @param <O> Output type
 */
public interface StallAwareProjector<I, O> extends Projector<I, O> {

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
    void stalled(Sink<O> sink);

    /**
     * Notifies the projector that the driver is idle i.e. the most recent poll of the event source returned no new
     * events
     * <p>
     * Unlike {@link #stalled(Sink)}, which is only called when a stall first occurs, this is called on <strong>every
     * </strong> poll of the event source that returns no new events.  It exists so that a projector on a quiet topic
     * regains control between polls, allowing it to react to external state changes, for example a request from another
     * thread that it pause at a safe point.  Without this a projector that stalled some time ago would sit in the
     * driver's poll loop indefinitely and never observe such a request.
     * </p>
     * <p>
     * Implementations MUST therefore be cheap and MUST NOT assume that anything has changed since the last call.  Any
     * expensive reaction to the projection stalling, e.g. flushing a sink or emitting marker events, belongs in
     * {@link #stalled(Sink)} instead.  Note that an implementation MAY block here, e.g. to hold the projection at a
     * pause point, so no further events are polled until it returns.
     * </p>
     * <p>
     * The default implementation does nothing.
     * </p>
     *
     * @param sink Output sink
     */
    default void idle(Sink<O> sink) {
        // Nothing to do by default
    }
}
