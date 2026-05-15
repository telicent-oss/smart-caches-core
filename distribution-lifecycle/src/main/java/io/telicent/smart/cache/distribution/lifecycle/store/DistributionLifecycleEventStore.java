package io.telicent.smart.cache.distribution.lifecycle.store;

import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAcknowledgement;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;

import java.util.List;
import java.util.UUID;

/**
 * The distribution lifecycle event store tracks the active lifecycle events and their acknowledgements
 */
public interface DistributionLifecycleEventStore {

    /**
     * Adds a lifecycle action to the store
     *
     * @param action Lifecycle action
     */
    void add(LifecycleAction action);

    /**
     * Adds a lifecycle acknowledgement to the store
     *
     * @param ack Lifecycle acknowledgement
     */
    void add(LifecycleAcknowledgement ack);

    /**
     * Gets all the lifecycle events that are considered active
     * <p>
     * This <strong>MUST</strong> return any event where there are either zero application acknowledgements, or there is
     * one/more application that has yet to reach the
     * {@link io.telicent.smart.cache.distribution.lifecycle.ApplicationState#Completed} state.
     * </p>
     *
     * @return Active events (if any)
     */
    List<LifecycleAction> activeEvents();

}
