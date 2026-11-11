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
package io.telicent.smart.cache.server.jaxrs.init;

import jakarta.servlet.ServletContextEvent;
import org.apache.commons.lang3.RandomUtils;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

public class TestServiceLoadedServletContextInitialiser {

    private static final AtomicInteger IDS = new AtomicInteger();

    private static ServerConfigInit good(int priority, Consumer<ServletContextEvent> onInit,
                                         Consumer<ServletContextEvent> onDestroy) {
        return new ServerConfigInit() {
            @Override
            public String getName() {
                return "Good " + IDS.incrementAndGet();
            }

            @Override
            public int priority() {
                return priority;
            }

            @Override
            public void contextDestroyed(ServletContextEvent sce) {
                onDestroy.accept(sce);
            }

            @Override
            public void contextInitialized(ServletContextEvent sce) {
                onInit.accept(sce);
            }
        };
    }

    private static ServerConfigInit bad(int priority) {
        return new ServerConfigInit() {
            @Override
            public String getName() {
                return "Bad " + IDS.incrementAndGet();
            }

            @Override
            public int priority() {
                return priority;
            }

            @Override
            public void contextDestroyed(ServletContextEvent sce) {
                throw new RuntimeException("Failed to destroy");
            }

            @Override
            public void contextInitialized(ServletContextEvent sce) {
                throw new RuntimeException("Failed to init");
            }
        };
    }

    @Test
    public void givenNoInitRegistered_whenInit_thenNoOp() {
        // Given
        try (MockedStatic<ServerConfigInit> init = mockStatic(ServerConfigInit.class)) {
            init.when(ServerConfigInit::getConfigInitialisers).thenReturn(Collections.emptyList());
            ServletContextEvent sce = mock(ServletContextEvent.class);

            // When
            ServiceLoadedServletContextInitialiser initialiser = new ServiceLoadedServletContextInitialiser();
            initialiser.contextInitialized(sce);

            // Then
            verifyNoInteractions(sce);
        }
    }

    @Test
    public void givenMultipleInit_whenOneFailsInit_thenOthersStillCalled_andSameForDestroy() {
        // Given
        AtomicInteger ran = new AtomicInteger(0);
        try (MockedStatic<ServerConfigInit> init = mockStatic(ServerConfigInit.class)) {
            List<ServerConfigInit> inits = new ArrayList<>();
            inits.add(bad(10));
            inits.add(good(0, e -> ran.incrementAndGet(), e -> ran.incrementAndGet()));
            init.when(ServerConfigInit::getConfigInitialisers).thenReturn(inits);
            ServletContextEvent sce = mock(ServletContextEvent.class);

            // When
            ServiceLoadedServletContextInitialiser initialiser = new ServiceLoadedServletContextInitialiser();
            initialiser.contextInitialized(sce);

            // Then
            Assert.assertEquals(ran.get(), 1);

            // ANd
            initialiser.contextDestroyed(sce);
            Assert.assertEquals(ran.get(), 2);
        }
    }

    @Test(invocationCount = 25)
    public void givenManyInit_whenSomeFailInit_thenAllGoodSuccessful() {
        // Given
        AtomicInteger ran = new AtomicInteger(0);
        int good = RandomUtils.insecure().randomInt(1, 5);
        int bad = RandomUtils.insecure().randomInt(1, 5);
        try (MockedStatic<ServerConfigInit> init = mockStatic(ServerConfigInit.class)) {
            List<ServerConfigInit> inits = new ArrayList<>();
            for (int i = 1; i <= good; i++) {
                inits.add(good(RandomUtils.insecure().randomInt(0, 100), e -> ran.incrementAndGet(),
                               e -> ran.incrementAndGet()));
            }
            for (int i = 1; i <= bad; i++) {
                inits.add(bad(RandomUtils.insecure().randomInt(0, 100)));
            }
            init.when(ServerConfigInit::getConfigInitialisers).thenReturn(inits);
            ServletContextEvent sce = mock(ServletContextEvent.class);

            // When
            ServiceLoadedServletContextInitialiser initialiser = new ServiceLoadedServletContextInitialiser();
            initialiser.contextInitialized(sce);

            // Then
            Assert.assertEquals(ran.get(), good);
        }
    }
}
