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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A provider of a sequence of port numbers starting from some base port with a random adjustment applied.
 * <p>
 * This is designed to reduce test failures that can happen when the OS is slow to release ports.
 * </p>
 * <p>
 * This class attempts to coordinate port usage both within the same JVM and across processes by actively checking
 * whether other instances within the JVM have already used a port, and checking whether the port is in-use by another
 * process.  This also includes a cross JVM port "reservation" mechanism where instances of this class attempt to
 * coordinate the reservation of ports by writing marker files to the {@link #RESERVED_PORTS_DIR} to indicate which
 * process has reserved a given port.
 * </p>
 * <p>
 * For best performance users should try to ensure that they choose base ports for each instance of this class that are
 * several hundred port numbers apart to reduce the chance of port overlap between instances since port coordination
 * does have some overheads associated with it.
 * </p>
 */
public final class RandomPortProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(RandomPortProvider.class);

    /**
     * Shared tracker of used ports across all instances of this within the JVM
     */
    private static final Set<Integer> USED_PORTS = new ConcurrentSkipListSet<>();
    /**
     * Shared random number generator across all instances of this within the JVM
     */
    private static final Random RANDOM = new Random();
    /**
     * Temporary directory used to track "reserved" ports, see {@link #reservePort(int)}
     */
    public static final File RESERVED_PORTS_DIR = new File("target/random-ports");
    /**
     * How long the provider waits in attempting to connect to a socket to determine if a given port is already in-use
     */
    public static final int SOCKET_CONNECT_ATTEMPT_TIMEOUT = 200;

    static {
        if (!RESERVED_PORTS_DIR.exists()) {
            if (!RESERVED_PORTS_DIR.mkdirs()) {
                throw new RuntimeException(
                        "Failed to create reserved ports directory to ensure no clashes between separate threads and/or processes using RandomPortProvider in parallel");
            }
        }
    }

    /**
     * Marks a range of ports as free for the purposes of the port provider
     * <p>
     * Generally only needs to be called where unit tests are being repeatedly run without a clean between them to avoid
     * eventually running out of the expected ports.  Or where a test specifically requires a specific port to attempt
     * to be selected.
     * </p>
     *
     * @param min Minimum port number to mark as free
     * @param max Maximum port number to mark as free
     */
    public static void freePortRange(int min, int max) {
        if (RESERVED_PORTS_DIR.exists() && RESERVED_PORTS_DIR.isDirectory()) {
            for (File f : Objects.requireNonNullElse(RandomPortProvider.RESERVED_PORTS_DIR.listFiles(), new File[0])) {
                try {
                    int port = Integer.parseInt(f.getName());
                    if (port >= min && port <= max) {
                        f.delete();
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }
        USED_PORTS.removeIf(p -> p >= min && p <= max);
    }

    private final AtomicInteger port;

    /**
     * Creates a new random port provider
     * <p>
     * Please see notes on class about coordination of port usage across instances.
     * </p>
     *
     * @param basePort Base port to use as starting point for generating the sequence of port numbers
     */
    public RandomPortProvider(int basePort) {
        this(basePort, 5, 50);
    }

    /**
     * Package constructor intended primarily for testing where setting the min and max increment bounds on the random
     * adjustment to the given base port can be useful to force two instances to have the exact same base port.
     *
     * @param basePort     Base port to use as starting point for generating the sequence of port numbers
     * @param minIncrement Minimum increment to use for the random adjustment of the base port
     * @param maxIncrement Maximum increment to use for the random adjustment of the base port
     */
    RandomPortProvider(int basePort, int minIncrement, int maxIncrement) {
        this.port = new AtomicInteger(basePort + RANDOM.nextInt(minIncrement, maxIncrement));
        LOGGER.info("[PID {}] Initial random port is {}", ProcessHandle.current().pid(), this.port.get());
    }

    /**
     * Gets the current port number in use
     *
     * @return Current port number
     */
    public int getPort() {
        return this.port.get();
    }

    /**
     * Gets a new port number to use
     *
     * @return New port number
     */
    public int newPort() {
        int nextPort = this.port.incrementAndGet();
        while (USED_PORTS.contains(nextPort) || portInUse(nextPort)) {
            // The port has already been used by another instance of this, or another process on the same machine
            USED_PORTS.add(nextPort);
            nextPort = this.port.incrementAndGet();
        }
        LOGGER.info("[PID {}] Next random port is {}", ProcessHandle.current().pid(), nextPort);
        return nextPort;
    }

    /**
     * Checks whether the given port is already in-use
     * <p>
     * It does this by trying to open a connection to the port, if that succeeds then the port is considered in-use and
     * won't be used.  If not in-use then it will attempt to {@link #reservePort(int)} for this JVMs usage.
     * </p>
     *
     * @param port Port to test
     * @return True if the port is in-use, false if not in-use and successfully reserved
     */
    private boolean portInUse(int port) {
        try (Socket socket = new Socket()) {
            // If we successfully connect then the port is in use and we should avoid it
            socket.connect(localhost(port), SOCKET_CONNECT_ATTEMPT_TIMEOUT);
            return true;
        } catch (Throwable e) {
            // Nothing was using the port at this point in time
            // If we failed to reserve the port then another process reserved it first
            return !reservePort(port);
        }
    }

    /**
     * Attempts to "reserve" a port by writing and reading a marker file to allow instances of this class across
     * separate JVM processes to coordinate and avoid clashing PIDs.
     * <p>
     * As race conditions abound here this is the best effort attempt at "reserving" the port.  It firstly checks
     * whether an existing reservation file exists for the port bailing out if it does.  Secondly it writes the current
     * JVMs PID to the reservation file, thirdly it reads back the reservation file to see whether anyone else overwrote
     * the file with a different PID.  Finally, if we've got this far then we again check whether we can connect to the
     * port refusing to reserve if the port has become used in the meantime.
     * </p>
     *
     * @param port Port to attempt to reserve
     * @return True if the port was reserved, false otherwise
     */
    private boolean reservePort(int port) {
        File reservation = new File(RESERVED_PORTS_DIR, Integer.toString(port));
        if (reservation.exists()) {
            // Another process already wrote the reservation file
            return false;
        }

        // Write out a file with our PID
        try (FileWriter writer = new FileWriter(reservation)) {
            writer.append(Long.toString(ProcessHandle.current().pid()));
        } catch (IOException e) {
            return false;
        }

        // Read in back in and verify our PID is what's currently written to it
        try (BufferedReader reader = new BufferedReader(new FileReader(reservation))) {
            long pid = Long.parseLong(reader.readLine());
            // Possible another process tries to write the reservation file at the same time as us
            if (pid != ProcessHandle.current().pid()) {
                return false;
            }
        } catch (Throwable e) {
            // Something else wrote the file, or the file contents were not readable/expected
            return false;
        }

        // Finally double check that nothing has opened the port in the meantime
        try (Socket socket = new Socket()) {
            socket.connect(localhost(port), SOCKET_CONNECT_ATTEMPT_TIMEOUT);
            return false;
        } catch (Throwable e) {
            // Still nothing listening on the socket so now considered reserved successfully
            // At this point mark the reservation file for deletion on exit as we know this JVM has "reserved" the port.
            // Therefore, once this JVM exits safe to release the "reservation"
            reservation.deleteOnExit();
            return true;
        }
    }

    /**
     * Gets a socket address for attempting to connect to the given port on {@code localhost}
     *
     * @param port Port number
     * @return Localhost address for the port number
     */
    private static InetSocketAddress localhost(int port) {
        return new InetSocketAddress("localhost", port);
    }
}
