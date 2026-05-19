package io.telicent.smart.cache.distribution.lifecycle.events.listeners;

import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple distribution lifecycle listener that logs lifecycle transitions
 */
public class LoggingListener implements DistributionLifecycleListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingListener.class);


    @Override
    public void accept(LifecycleAction action) {
        LOGGER.info("Distribution {} transitioned from {} to {}", action.getDistributionId(),
                    action.getState().getFrom(), action.getState().getTo());
    }
}
