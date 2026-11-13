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
package io.telicent.smart.cache.distribution.lifecycle.status;

import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.*;
import java.util.concurrent.*;

public class TestDistributionStatus {
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> rows(DistributionStatus status) {
        return (List<Map<String, Object>>) status.snapshot("test").get("distributions");
    }
    @Test
    public void observesLocalStateAndUnknownDistributionsWithoutInventingCounts() {
        var status = new DistributionStatus();
        status.states(() -> Map.of("dist-a", "Active"));
        status.processed("dist-b");
        var rows = rows(status);
        Assert.assertEquals(rows.get(0).get("state"), "Active");
        Assert.assertEquals(rows.get(0).get("processedItems"), 0L);
        Assert.assertNull(rows.get(0).get("lastProcessedAt"));
        Assert.assertEquals(rows.get(1).get("state"), "Unregistered");
        Assert.assertEquals(rows.get(1).get("processedItems"), 1L);
        status.states(() -> null);
        Assert.assertEquals(rows(status).getFirst().get("state"), "Unknown");
        Assert.assertEquals(status.snapshot("test").get("lifecycleTracking"), false);
    }
    @Test
    public void concurrentUpdatesAndRestartsRemainDistinguishable() throws Exception {
        var status = new DistributionStatus();
        try (var executor = Executors.newFixedThreadPool(4)) {
            var tasks = new ArrayList<Callable<Void>>();
            for (int i = 0; i < 1000; i++) tasks.add(() -> { status.processed("dist-a"); rows(status); return null; });
            for (var result : executor.invokeAll(tasks)) result.get();
        }
        Assert.assertEquals(rows(status).getFirst().get("processedItems"), 1000L);
        Assert.assertNotEquals(new DistributionStatus().snapshot("test").get("instanceId"),
                               status.snapshot("test").get("instanceId"));
        Assert.assertTrue(rows(new DistributionStatus()).isEmpty());
    }
}
