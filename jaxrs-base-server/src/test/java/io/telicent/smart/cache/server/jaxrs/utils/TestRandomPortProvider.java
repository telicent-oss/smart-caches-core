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
package io.telicent.smart.cache.server.jaxrs.utils;

import io.telicent.smart.cache.server.jaxrs.applications.MockApplication;
import io.telicent.smart.cache.server.jaxrs.applications.Server;
import io.telicent.smart.cache.server.jaxrs.applications.ServerBuilder;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

// java:S2925 - Thread.sleep is required when waiting on real Kafka/Docker in integration tests
@SuppressWarnings("java:S2925")
public class TestRandomPortProvider {

    public static final int BASE_PORT = 30_000;

    @BeforeMethod
    public void setup() {
        RandomPortProvider.freePortRange(BASE_PORT, BASE_PORT + 100);
    }

    @Test
    public void givenTwoRandomPortProvidersWithSameBase_whenObtainingPorts_thenNoOverlapsShouldOccur() {
        // Given
        RandomPortProvider a = new RandomPortProvider(BASE_PORT, 0, 1);
        RandomPortProvider b = new RandomPortProvider(BASE_PORT, 0, 1);

        // When
        int portA = a.newPort();
        int portB = b.newPort();

        // Then
        Assert.assertNotEquals(portA, portB);
        Set<Integer> obtained = new HashSet<>();
        for (int i = 1; i <= 10; i++) {
            Assert.assertTrue(obtained.add(a.newPort()), "Provider A returned an already used port number");
            Assert.assertTrue(obtained.add(b.newPort()), "Provider B returned an already used port number");
        }
    }

    @Test
    public void givenRandomPortProviderAndExistingServerOnPort_whenObtainingPort_thenExistingServerPortAvoided() throws
            IOException {
        // Given
        RandomPortProvider portProvider = new RandomPortProvider(BASE_PORT, 0, 1);
        Server server = ServerBuilder.create().application(MockApplication.class)
                                     .port(BASE_PORT + 1).displayName("Test").build();
        try {
            server.start();

            // When
            int port = portProvider.newPort();

            // Then
            Assert.assertNotEquals(port, server.getPort());
        } finally {
            server.stop();
        }
    }

    @Test
    public void givenPortBelowEphemeralRange_whenCheckingIfEphemeral_thenFalse() {
        // Given, When and Then
        // Every base port used by this project's tests must be safe, the ephemeral range is where the OS hands out
        // local ports for outbound connections so a server can lose a race for one at any moment
        Assert.assertFalse(RandomPortProvider.isEphemeralPort(BASE_PORT),
                           "This test class' own base port must not be in the ephemeral range");
        Assert.assertFalse(RandomPortProvider.isEphemeralPort(1024));
    }

    @Test
    public void givenPortInEphemeralRange_whenCheckingIfEphemeral_thenTrue() {
        // Given, When and Then
        // The detected range varies by OS, but the Linux default lower bound is in every range we care about
        Assert.assertTrue(RandomPortProvider.isEphemeralPort(RandomPortProvider.DEFAULT_EPHEMERAL_PORT_MAX));
    }

    @Test(invocationCount = 3)
    public void givenRandomPortProviderAndExistingProcessOnPort_whenObtainingPort_thenExistingProcessPortAvoided() throws
            IOException, InterruptedException {
        // Given
        RandomPortProvider portProvider = new RandomPortProvider(BASE_PORT, 0, 1);
        Process process = Runtime.getRuntime().exec(new String[] { "nc", "-l", Integer.toString(BASE_PORT + 1)});
        Thread.sleep(500); // Wait briefly for nc to open the socket
        if (!process.isAlive()) {
            throw new SkipException("Failed to launch nc server for test");
        }
        try {
            // When
            int port = portProvider.newPort();

            // Then
            Assert.assertNotEquals(port, BASE_PORT + 1);
        } finally {
            process.destroyForcibly();
        }
    }
}
