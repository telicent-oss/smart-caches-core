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
package io.telicent.smart.cache.actions.tracker;

import io.telicent.smart.cache.actions.tracker.model.ActionState;

/**
 * An Action Tracker allows applications composed of multiple microservices to track and respond to actions e.g. a
 * backup/restore operation, in appropriate and coordinated ways.
 * <p>
 * An action tracker basically represents a finite state machine which transitions between the defined
 * {@link io.telicent.smart.cache.actions.tracker.model.ActionTransition} states according to defined rules i.e. not all
 * transitions are considered legal.  If an illegal transition is attempted then an {@link IllegalStateException} is
 * thrown.
 * </p>
 * <p>
 * An action tracker currently only permits a single action to be in process at any one time, attempting to track more
 * than one action will result in an {@link IllegalStateException} being thrown.
 * </p>
 * <p>
 * As an example consider an application composed of two microservices - a projector that loads data into the
 * application, and an API server that provides access to that data.  The API server would provide the ability for a
 * user to trigger a backup/restore operation and the projector may wish to respond to that e.g. after a restore
 * operation it likely wants to re-seek to the correct Kafka offsets for the restored application state.
 * </p>
 * <p>
 * By using a {@link PrimaryActionTracker} and a {@link SecondaryActionTracker} in the API server and Projector
 * respectively the projector can be informed of backup/restore operations happening and react accordingly.
 * </p>
 */
public interface ActionTracker extends AutoCloseable {

    /**
     * Gets the current state
     *
     * @return Current state
     */
    ActionState getState();

    /**
     * Gets the current action (if any)
     *
     * @return Current Action, or {@code null} if no current action i.e. not in state {@link ActionState#PROCESSING}
     */
    String getAction();

    /**
     * Informs the tracker that application startup has completed
     * <p>
     * <strong>SHOULD</strong> only be called once by an application, subsequent calls are generally ignored
     * </p>
     */
    void startupComplete();

    /**
     * Informs the tracker that an action is started
     *
     * @param action Action
     * @throws IllegalStateException Another action is already in progress
     */
    void start(String action);

    /**
     * Informs the tracker that an action is finished
     *
     * @param action Action
     * @throws IllegalStateException No action is in progress, or the given action does not match the previously started
     *                               action
     */
    void finish(String action);

    /**
     * Closes the action tracker releasing any resources it might be holding
     */
    void close();
}
