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
package io.telicent.smart.cache.sources;

/**
 * Provides constants related to Telicent's standard event headers
 */
public class TelicentHeaders {

    private TelicentHeaders() {
    }

    /**
     * Event header used to specify the default security labels for data that is not more specifically labelled
     */
    public static final String SECURITY_LABEL = "Security-Label";

    /**
     * Event header used to track the execution path for an event
     */
    public static final String EXEC_PATH = "Exec-Path";

    /**
     * Useful summary information which may identify reason for the dead letter event
     */
    public static final String DEAD_LETTER_REASON = "Dead-Letter-Reason";

    public static final String REQUEST_ID = "Request-ID";
}
