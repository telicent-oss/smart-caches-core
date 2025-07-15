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
package io.telicent.smart.cache.actions.tracker;

import io.telicent.smart.cache.actions.tracker.model.ActionState;
import io.telicent.smart.cache.actions.tracker.model.ActionTransition;
import io.telicent.smart.cache.projectors.sinks.CollectorSink;
import io.telicent.smart.cache.sources.Event;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

public class TestPrimaryActionTracker {

    @Test
    public void givenPrimaryActionTracker_whenStartingUp_thenTransitionSentToSink() {
        // Given
        try (CollectorSink<Event<UUID, ActionTransition>> sink = CollectorSink.of()) {
            try (ActionTracker tracker = new PrimaryActionTracker("test", sink)) {

                // When
                tracker.startupComplete();

                // Then
                Assert.assertFalse(sink.get().isEmpty());
                Assert.assertEquals(sink.get().size(), 1);
                Event<UUID, ActionTransition> event = sink.get().get(0);
                verifyTransitionEvent(event, ActionState.STARTING, ActionState.READY, null);
            }
        }
    }

    private static void verifyTransitionEvent(Event<UUID, ActionTransition> event, ActionState expectedFrom,
                                              ActionState expectedTo, String expectedAction) {
        Assert.assertNotNull(event);
        ActionTransition transition = event.value();
        Assert.assertNotNull(transition);
        Assert.assertEquals(event.key(), transition.getId());
        Assert.assertEquals(transition.getFrom(), expectedFrom);
        Assert.assertEquals(transition.getTo(), expectedTo);
        Assert.assertEquals(transition.getAction(), expectedAction);
    }

    @Test
    public void givenPrimaryActionTracker_whenMakingTransitions_thenTransitionSentToSink() {
        // Given
        try (CollectorSink<Event<UUID, ActionTransition>> sink = CollectorSink.of()) {
            try (ActionTracker tracker = new PrimaryActionTracker("test", sink)) {

                // When
                tracker.startupComplete();
                tracker.start("backup");
                tracker.finish("backup");
                tracker.start("restore");
                tracker.finish("restore");

                // Then
                Assert.assertFalse(sink.get().isEmpty());
                Assert.assertEquals(sink.get().size(), 5);
                Event<UUID, ActionTransition> event = sink.get().get(0);
                verifyTransitionEvent(event, ActionState.STARTING, ActionState.READY, null);
                event = sink.get().get(1);
                verifyTransitionEvent(event, ActionState.READY, ActionState.PROCESSING, "backup");
                event = sink.get().get(2);
                verifyTransitionEvent(event, ActionState.PROCESSING, ActionState.READY, "backup");
                event = sink.get().get(3);
                verifyTransitionEvent(event, ActionState.READY, ActionState.PROCESSING, "restore");
                event = sink.get().get(4);
                verifyTransitionEvent(event, ActionState.PROCESSING, ActionState.READY, "restore");
            }
        }
    }

    @Test
    public void givenPrimaryActionTracker_whenMakingIllegalTransition_thenNothingSentToSink() {
        // Given
        try (CollectorSink<Event<UUID, ActionTransition>> sink = CollectorSink.of()) {
            try (ActionTracker tracker = new PrimaryActionTracker("test", sink)) {

                // When
                Assert.assertThrows(IllegalStateException.class, () -> tracker.finish("backup"));

                // Then
                Assert.assertTrue(sink.get().isEmpty());
            }
        }
    }
}
