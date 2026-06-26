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
import org.apache.jena.sparql.core.Quad;

/**
 * Interface for removing security labels associated with specific quads in a dataset graph
 */
public interface SecurityLabelsRemover {

    /**
     * Removes the security labels associated with the given quad from the dataset graph
     *
     * @param datasetGraph the dataset graph from which to remove the security labels
     * @param quad         the quad whose security labels should be removed
     * @throws DataSecurityException if an error occurs while removing the security labels
     */
    void remove(DatasetGraph datasetGraph, Quad quad) throws DataSecurityException;

}
