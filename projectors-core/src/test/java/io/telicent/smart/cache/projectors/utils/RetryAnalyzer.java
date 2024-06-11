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
package io.telicent.smart.cache.projectors.utils;

import org.testng.ITestResult;
import org.testng.util.RetryAnalyzerCount;

/**
 * A test retry analyzer for tests that are "flaky", this is typically used for tests that are looking at timing or
 * concurrency issues that run reliably on a developers' machine but can randomly fail when running on CI/CD
 * infrastructure.
 */
public class RetryAnalyzer extends RetryAnalyzerCount {

    /**
     * Creates a new analyzer with a default maximum retries of 3
     */
    public RetryAnalyzer() {
        super();
        this.setCount(3);
    }

    @Override
    public boolean retryMethod(ITestResult result) {
        // Always retry the test, this method is only called by the base class when we're within our maximum permitted
        // retries (3) so always safe to return true
        return true;
    }
}
