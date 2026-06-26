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

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.jena.sparql.core.DatasetGraph;

/**
 * Interface for restoring security labels for a dataset graph from a previously created backup
 */
public interface SecurityLabelsRestore {

    /**
     * Restores security labels for the given dataset graph from the specified backup location
     *
     * @param datasetGraph the dataset graph whose security labels are to be restored
     * @param restorePath  the path from which to restore the security labels
     * @param node         the JSON object node containing backup metadata
     */
    void restore(DatasetGraph datasetGraph, String restorePath, ObjectNode node);
}
