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
package io.telicent.smart.cache.observability.events;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static io.telicent.smart.cache.observability.events.CounterEvent.counterEvent;
import static io.telicent.smart.cache.observability.events.DurationEvent.durationEvent;
import static org.mockito.Mockito.*;

public class EventUtilTest {

    @Mock
    EventDispatcher<ComponentEvent> dispatcher;

    @Test
    public void whenEmitIsCalledWithDispatcherAndSomeEvents_thenTheDispatcherIsCalledToDispatchEachEvent() {
        // Given a dispatcher and some events to be dispatched
        MockitoAnnotations.openMocks(this);
        ComponentEvent event1 = counterEvent("event1");
        ComponentEvent event2 = durationEvent("event2", 1000, 2000);

        // When emit is called to dispatch the events through the dispatcher
        EventUtil.emit(dispatcher, event1, event2);

        // Then the dispatcher was called to emit each event
        verify(dispatcher).dispatch(event1);
        verify(dispatcher).dispatch(event2);
        verifyNoMoreInteractions(dispatcher);
    }
}