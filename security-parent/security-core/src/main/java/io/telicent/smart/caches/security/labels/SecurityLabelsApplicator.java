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
package io.telicent.smart.caches.security.labels;

import org.apache.jena.graph.Triple;

/**
 * Interface for security labels applicator
 *
 * @param <T> Decoded labels type
 */
public interface SecurityLabelsApplicator<T> {

    /**
     * Returns the security label that applies to the given triple
     *
     * @param triple Triple
     * @return Security Label, or {@code null} if no security label applies
     */
    SecurityLabels<T> labelForTriple(Triple triple);
}