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

/**
 * Interface for security labels validation
 */
public interface SecurityLabelsValidator {

    /**
     * Validates whether the given raw labels are valid and supported by the plugin that provided this validator
     *
     * @param rawLabels Raw labels
     * @return True if valid, false if valid
     */
    boolean validate(byte[] rawLabels);
}
