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
