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
package io.telicent.smart.cache.sources.kafka;

import org.testng.ITestResult;
import org.testng.util.RetryAnalyzerCount;

/**
 * A test retry analyzer for CLI tests that are "flaky", this is used for tests that can sometimes encounter timing
 * issues where the test Kafka cluster is sometimes not ready in time
 */
public class FlakyKafkaTest extends RetryAnalyzerCount {

    /**
     * Creates a new analyzer with a default maximum retries of 3
     */
    public FlakyKafkaTest() {
        super();
        this.setCount(3);
    }

    @Override
    public boolean retryMethod(ITestResult result) {
        // No need to retry if the test already succeeded or was skipped
        if (result.isSuccess() || result.getStatus() == ITestResult.SKIP) {
            return false;
        }

        // Always retry the test, this method is only called by the base class when we're within our maximum permitted
        // retries (3) so always safe to return true
        Utils.logStdOut("Retrying failed test %s with status %s", result.getName(), getTestStatus(result));
        return true;
    }

    private static String getTestStatus(final ITestResult result) {
        return switch (result.getStatus()) {
            case ITestResult.CREATED -> "SKIP";
            case ITestResult.SUCCESS -> "SUCCESS";
            case ITestResult.FAILURE -> "FAILURE";
            case ITestResult.SKIP -> "SKIP";
            case ITestResult.SUCCESS_PERCENTAGE_FAILURE -> "SUCCESS_PERCENTAGE_FAILURE";
            case ITestResult.STARTED -> "STARTED";
            default -> "Uknown["+result.getStatus()+"]";
        };
    }
}
