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
package io.telicent.smart.cache.observability;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

/**
 * Utilities relating to presenting runtime information
 */
public class RuntimeInfo {

    private static final double KIBIBIT = 1024;
    private static final double MEBIBIT = KIBIBIT * KIBIBIT;
    private static final double GIBIBIT = MEBIBIT * KIBIBIT;
    private static final double TEBIBIT = GIBIBIT * KIBIBIT;

    /**
     * Private constructor prevents direct instantiation
     */
    private RuntimeInfo() {
    }

    /**
     * Prints the runtime information (memory, Java and OS) to the given logger
     *
     * @param logger Logger to output to
     */
    public static void printRuntimeInfo(Logger logger) {
        logger.info("Processors:   {}", Runtime.getRuntime().availableProcessors());
        printMemoryInfo(logger);
        logger.info("Java:         {}", System.getProperty("java.version"));
        logger.info("OS:           {} {} {}", System.getProperty("os.name"), System.getProperty("os.version"),
                    System.getProperty("os.arch"));
    }

    /**
     * Prints information about maximum, total and free memory
     * <p>
     * While max memory is generally fixed for the lifetime of a Java process the total and free memory will vary as the
     * application performs work and the JVM grows/shrinks the heap (depending on JVM settings).
     * </p>
     *
     * @param logger Logger to output to
     */
    public static void printMemoryInfo(Logger logger) {
        double rawMaxMemory = Runtime.getRuntime().maxMemory();
        double rawTotalMemory = Runtime.getRuntime().totalMemory();
        double rawFreeMemory = Runtime.getRuntime().freeMemory();
        Pair<Double, String> maxMemory = RuntimeInfo.parseMemory(rawMaxMemory);
        Pair<Double, String> totalMemory = RuntimeInfo.parseMemory(rawTotalMemory);
        Pair<Double, String> freeMemory = RuntimeInfo.parseMemory(rawFreeMemory);
        logger.info("Max Memory:   {} {}", String.format("%.2f", maxMemory.getKey()), maxMemory.getValue());
        logger.info("Total Memory: {} {}", String.format("%.2f", totalMemory.getKey()), totalMemory.getValue());
        logger.info("Free Memory:  {} {}", String.format("%.2f", freeMemory.getKey()), freeMemory.getValue());
    }

    /**
     * Prints library version information to the given logger.
     * <p>
     * This will only include libraries whose versions have previously been requested via
     * {@link LibraryVersion#get(String)}.
     * </p>
     *
     * @param logger Logger to output to
     */
    public static void printLibraryVersions(Logger logger) {
        for (String library : LibraryVersion.cachedLibraries()) {
            logger.info("Using library {} version {}", library, LibraryVersion.get(library));
        }
    }

    /**
     * Parses a raw memory value in bytes into a human-readable representation
     * <p>
     * The human-readable representation will be expressed in binary units, rather than decimal, units.  So for example
     * a parsed value of {@code 2 GiB} represents 2 Gibibytes, which is {@code 2147483648} bytes.
     * </p>
     * <p>
     * Due to the nature of floating point arithmetic some calculated values may be approximate.  This is especially
     * true if the output of this function is used in presenting a formatted value to the user where further truncation
     * of the value may occur.
     * </p>
     *
     * @param rawMemory Raw Memory
     * @return Human-readable representation consisting of a value and a unit
     */
    public static Pair<Double, String> parseMemory(double rawMemory) {
        if (rawMemory < MEBIBIT) {
            return Pair.of(rawMemory / KIBIBIT, "KiB");
        } else if (rawMemory < GIBIBIT) {
            return Pair.of(rawMemory / MEBIBIT, "MiB");
        } else if (rawMemory < TEBIBIT) {
            return Pair.of(rawMemory / GIBIBIT, "GiB");
        } else {
            return Pair.of(rawMemory / TEBIBIT, "TiB");
        }
    }
}
