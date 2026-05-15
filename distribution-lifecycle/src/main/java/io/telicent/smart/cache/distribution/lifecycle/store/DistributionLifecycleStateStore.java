package io.telicent.smart.cache.distribution.lifecycle.store;

import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;

import java.util.Map;

/**
 * The distribution lifecycle state store tracks current states for each known distribution
 */
public interface DistributionLifecycleStateStore {

    /**
     * Gets the current lifecycle state for the given distribution
     *
     * @param distributionId Distribution ID
     * @return Current lifecycle state, <strong>MUST</strong> return {@link DistributionLifecycleState#Unregistered} if
     * not a known Distribution ID
     */
    DistributionLifecycleState getLifecycleState(String distributionId);

    /**
     * Sets the current lifecycle state for the given distribution
     *
     * @param distributionId Distribution ID
     * @param state          New lifecycle state
     * @throws IllegalArgumentException <strong>MUST</strong> be thrown if the provided state is not a valid transition
     *                                  from the current state
     */
    void setLifecycleState(String distributionId, DistributionLifecycleState state);

    /**
     * Gets the states of all known distributions
     *
     * @return Distribution lifecycle states
     */
    Map<String, DistributionLifecycleState> getLifecycleStates();
}
