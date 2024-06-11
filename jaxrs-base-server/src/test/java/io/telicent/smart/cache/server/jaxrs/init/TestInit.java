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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TestInit implements ServerConfigInit {
    public static final AtomicInteger INIT_CALLED = new AtomicInteger(0), DESTROY_CALLED = new AtomicInteger(0);

    public static final Comparator<ServerConfigInit> COMPARATOR = new InitPriorityComparator();

    /**
     * Resets the init counters
     */
    public static void reset() {
        INIT_CALLED.set(0);
        DESTROY_CALLED.set(0);
    }

    private final String name;
    private final int priority;

    public TestInit() {
        this("Test", 10);
    }

    public TestInit(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int priority() {
        return this.priority;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        INIT_CALLED.incrementAndGet();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DESTROY_CALLED.incrementAndGet();
    }

    @Test
    public void init_comparison() {
        // This and null comparisons
        Assert.assertEquals(COMPARATOR.compare(this, this), 0);
        Assert.assertEquals(COMPARATOR.compare(this, null), 1);
        Assert.assertEquals(COMPARATOR.compare(null, this), -1);
        Assert.assertEquals(COMPARATOR.compare(null, null), 0);

        // Identical but different instance comparisons
        Assert.assertEquals(COMPARATOR.compare(this, new TestInit()), 0);
        Assert.assertEquals(COMPARATOR.compare(new TestInit(), new TestInit()), 0);

        // Different value comparisons
        Assert.assertEquals(COMPARATOR.compare(this, new TestInit("Test", 100)), 1);
        Assert.assertTrue(COMPARATOR.compare(new TestInit("Foo", 100), new TestInit("Test", 100)) < 0);
    }

    @Test
    public void init_sorting_01() {
        List<ServerConfigInit> inits = new ArrayList<>();
        inits.add(this);
        inits.add(new TestInit("High", 100));
        inits.add(new TestInit("Low", -100));

        inits.sort(COMPARATOR);
        Assert.assertEquals(inits.get(0).getName(), "High");
        Assert.assertEquals(inits.get(1).getName(), "Test");
        Assert.assertEquals(inits.get(2).getName(), "Low");
    }
}
