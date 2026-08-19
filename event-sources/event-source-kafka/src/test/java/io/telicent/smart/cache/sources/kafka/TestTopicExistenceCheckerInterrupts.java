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

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that {@link TopicExistenceChecker} honours thread interruption.
 * <p>
 * The interrupt handling here is easy to get wrong in a way no functional test would notice: if the
 * {@link InterruptedException} is swallowed without restoring the thread's interrupt status then the retry loop keeps
 * spinning for the whole timeout and the caller never learns it was interrupted.  These tests pin down both halves of
 * that contract.
 * </p>
 */
public class TestTopicExistenceCheckerInterrupts {

    private static final String TOPIC = "test-topic";
    private static final String SERVER = "localhost:9092";

    /**
     * Exposes the {@code protected} check method, which is only reachable from within this package
     */
    private static final class ProbeChecker extends TopicExistenceChecker {
        private ProbeChecker(AdminClient adminClient) {
            super(adminClient, SERVER, List.of(TOPIC), null);
        }

        private boolean check(Duration timeout) {
            return doesTopicExist(TOPIC, timeout);
        }
    }

    /**
     * Ensures a set interrupt status never leaks into a subsequent test
     */
    @AfterMethod
    public void clearInterruptStatus() {
        Thread.interrupted();
    }

    @SuppressWarnings("unchecked")
    private static AdminClient interruptingAdminClient() throws Exception {
        KafkaFuture<Map<String, TopicDescription>> future = mock(KafkaFuture.class);
        when(future.get(anyLong(), any(TimeUnit.class))).thenThrow(new InterruptedException("Simulated interrupt"));

        DescribeTopicsResult result = mock(DescribeTopicsResult.class);
        when(result.allTopicNames()).thenReturn(future);

        AdminClient adminClient = mock(AdminClient.class);
        when(adminClient.describeTopics(anyCollection())).thenReturn(result);
        return adminClient;
    }

    @Test
    public void givenAnInterruptedTopicDescription_whenCheckingExistence_thenTheTopicIsNotReportedAsExisting() throws
            Exception {
        ProbeChecker checker = new ProbeChecker(interruptingAdminClient());
        try {
            Assert.assertFalse(checker.check(Duration.ofSeconds(30)),
                               "A topic whose existence check was interrupted must not be reported as existing");
        } finally {
            Thread.interrupted();
            checker.close();
        }
    }

    @Test
    public void givenAnInterruptedTopicDescription_whenCheckingExistence_thenTheInterruptStatusIsRestored() throws
            Exception {
        ProbeChecker checker = new ProbeChecker(interruptingAdminClient());
        try {
            checker.check(Duration.ofSeconds(30));

            // Thread.interrupted() both asserts and clears, so the status cannot leak out of this test
            Assert.assertTrue(Thread.interrupted(),
                              "Interrupt status must be restored so the calling thread can act on it");
        } finally {
            Thread.interrupted();
            checker.close();
        }
    }

    @Test
    public void givenAnInterruptedTopicDescription_whenCheckingExistence_thenTheRetryLoopIsAbandonedImmediately() throws
            Exception {
        ProbeChecker checker = new ProbeChecker(interruptingAdminClient());
        try {
            // A deliberately long timeout - if the interrupt were swallowed the loop would sleep and retry for the
            // full 30 seconds rather than giving up on the first failure
            long start = System.currentTimeMillis();
            checker.check(Duration.ofSeconds(30));
            long elapsed = System.currentTimeMillis() - start;

            Assert.assertTrue(elapsed < 5000,
                              "Expected the check to abandon its retry loop immediately, but it took " + elapsed
                                      + "ms");
        } finally {
            Thread.interrupted();
            checker.close();
        }
    }
}
