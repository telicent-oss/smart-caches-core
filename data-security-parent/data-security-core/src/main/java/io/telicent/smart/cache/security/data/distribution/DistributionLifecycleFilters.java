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
package io.telicent.smart.cache.security.data.distribution;

import org.apache.jena.sparql.core.DatasetGraph;

/**
 * Interface for managing lifecycle-aware filters applied to dataset graphs during data distribution
 */
public interface DistributionLifecycleFilters {

    /**
     * Install lifecycle-aware dataset filters if configured
     *
     * @param dataset       the dataset graph
     * @param applicationId the application ID
     * @param stateFile     the state file
     * @return true if filters installed
     */
    boolean installIfConfigured(DatasetGraph dataset, String applicationId, String stateFile);

}
