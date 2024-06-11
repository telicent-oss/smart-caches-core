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

/**
 * Contains constants pertaining to metrics collected by the {@link ProjectorDriver}
 */
public class DriverMetricNames {

    /**
     * Metric used to track how many total times the projection has stalled i.e. received no new events for projection
     */
    public static final String STALLS_TOTAL = "messaging.stalls.total";

    /**
     * Metric used to track how many consecutive times the projection has stalled i.e. received no new events for
     * projection
     */
    public static final String STALLS_CONSECUTIVE = "messaging.stalls.consecutive";

    /**
     * Description of the consecutive stalls metric
     */
    public static final String STALLS_CONSECUTIVE_DESCRIPTION =
            "Number of consecutive times that the event source was stalled i.e. returned no events";

    /**
     * Description of the total stalls metric
     */
    public static final String STALLS_TOTAL_DESCRIPTION =
            "Total number of times that the event source was stalled i.e. returned no new events";

    private DriverMetricNames() {
    }
}
