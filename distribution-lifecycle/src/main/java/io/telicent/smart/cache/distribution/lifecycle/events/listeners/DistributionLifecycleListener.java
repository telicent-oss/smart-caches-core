package io.telicent.smart.cache.distribution.lifecycle.events.listeners;

import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;

/**
 * Interface for listeners to distribution lifecycle events, used by
 * {@link io.telicent.smart.cache.distribution.lifecycle.tracker.DistributionLifecycleTracker}
 */
public interface DistributionLifecycleListener {

    /**
     * Accepts a lifecycle action event
     *
     * @param action Lifecycle action
     */
    void accept(LifecycleAction action);
}
