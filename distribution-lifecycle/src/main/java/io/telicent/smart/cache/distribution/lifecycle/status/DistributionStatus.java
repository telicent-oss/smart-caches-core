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

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/** Process-scoped counts of input attempts reaching the host application's completion boundary.
 * This can include handled rejections. Counts are diagnostic, not distinct documents or persisted totals. */
public final class DistributionStatus {
    private final String instanceId = UUID.randomUUID().toString();
    private final String startedAt = Instant.now().toString();
    private final Map<String, Progress> progress = new ConcurrentHashMap<>();
    private volatile Supplier<Map<String, String>> states = () -> null;
    private static final class Progress {
        long count;
        String lastProcessedAt;
        synchronized void increment() { count++; lastProcessedAt = Instant.now().toString(); }
        synchronized Map<String, Object> snapshot() {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("processedItems", count);
            result.put("lastProcessedAt", lastProcessedAt);
            return result;
        }
    }
    public void states(Supplier<Map<String, String>> supplier) { this.states = Objects.requireNonNull(supplier); }
    public void processed(String distributionId) {
        if (distributionId != null && !distributionId.isBlank()) {
            progress.computeIfAbsent(distributionId, ignored -> new Progress()).increment();
        }
    }
    public Map<String, Object> snapshot(String application) {
        Map<String, String> localStates = states.get();
        Set<String> ids = new TreeSet<>(progress.keySet());
        if (localStates != null) ids.addAll(localStates.keySet());
        List<Map<String, Object>> distributions = new ArrayList<>();
        for (String id : ids) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("distributionId", id);
            row.put("state", localStates == null ? "Unknown" : localStates.getOrDefault(id, "Unregistered"));
            row.put("processedItems", 0L);
            row.put("lastProcessedAt", null);
            Progress value = progress.get(id);
            if (value != null) row.putAll(value.snapshot());
            distributions.add(row);
        }
        return Map.of("schemaVersion", 1, "application", application, "instanceId", instanceId,
                      "startedAt", startedAt, "observedAt", Instant.now().toString(),
                      "countScope", "process", "countSemantics", "completed-input-attempts",
                      "lifecycleTracking", localStates != null, "distributions", distributions);
    }
}
