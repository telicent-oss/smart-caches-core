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
package io.telicent.smart.cache.security.data.labels;

import io.telicent.smart.cache.security.data.DataSecurityException;
import org.apache.jena.sparql.core.DatasetGraph;

/**
 * Interface for compacting the security labels store associated with a dataset graph
 */
public interface SecurityLabelsCompact {

    /**
     * Compacts the security labels store for the given dataset graph, removing stale or orphaned label entries
     *
     * @param datasetGraph the dataset graph whose labels store is to be compacted
     * @throws DataSecurityException if an error occurs during compaction
     */
    void compact(DatasetGraph datasetGraph) throws DataSecurityException;

}
