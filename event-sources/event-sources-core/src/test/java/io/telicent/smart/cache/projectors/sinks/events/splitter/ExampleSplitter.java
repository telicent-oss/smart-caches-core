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
package io.telicent.smart.cache.projectors.sinks.events.splitter;

import java.util.List;

public class ExampleSplitter<TKey> extends AbstractByteBasedSplitter<TKey, ExampleSplittablePayload> {
    @Override
    protected byte[] getByteField(ExampleSplittablePayload value) {
        return value.getPayload();
    }

    @Override
    protected ExampleSplittablePayload splitValue(ExampleSplittablePayload original, byte[] chunk) {
        return ExampleSplittablePayload.builder().title(original.getTitle()).payload(chunk).build();
    }

    @Override
    protected ExampleSplittablePayload combineValues(List<ExampleSplittablePayload> values,
                                                     byte[] combined) {
        return ExampleSplittablePayload.builder().title(values.get(0).getTitle()).payload(combined).build();
    }
}
