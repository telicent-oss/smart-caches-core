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

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.jena.graph.Triple;

/**
 * A security labels applicator for cases where there is only a single default label that applies to all triples in the
 * event being processed
 */
@AllArgsConstructor
public final class DefaultLabelApplicator implements SecurityLabelsApplicator {

    @NonNull
    private final SecurityLabels<?> defaultLabel;

    @Override
    public SecurityLabels<?> labelForTriple(Triple triple) {
        return this.defaultLabel;
    }

    @Override
    public void close() {
        // No-op
    }
}
