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
package io.telicent.smart.cache.server.jaxrs.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Suppresses repeated noisy log messages while still logging the first and optionally later repeats.
 */
public class RepeatedMessageFilter extends TurboFilter {

    private volatile Cache<String, AtomicLong> repeatCounts;

    private Set<String> loggerNameSuffixes = Set.of();
    private Set<Level> levels = Set.of(Level.WARN);
    private Set<String> messageSubstrings = Set.of();
    private Set<String> pathSubstrings = Set.of();
    private int maxLoggedOccurrences;
    private int repeatInterval = 25;
    private int maxCacheSize = 128;

    public void setLoggerNameSuffixes(String loggerNameSuffixes) {
        this.loggerNameSuffixes = splitCsv(loggerNameSuffixes);
    }

    public void setLevels(String levels) {
        this.levels = splitCsv(levels).stream()
                                      .map(Level::valueOf)
                                      .collect(Collectors.toUnmodifiableSet());
    }

    public void setMessageSubstrings(String messageSubstrings) {
        this.messageSubstrings = splitCsv(messageSubstrings);
    }

    public void setPathSubstrings(String pathSubstrings) {
        this.pathSubstrings = splitCsv(pathSubstrings);
    }

    public void setMaxLoggedOccurrences(int maxLoggedOccurrences) {
        this.maxLoggedOccurrences = maxLoggedOccurrences;
    }

    public void setRepeatInterval(int repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    @Override
    public void start() {
        if (this.repeatInterval < 2) {
            addError("repeatInterval must be at least 2");
            return;
        }
        if (this.maxCacheSize < 1) {
            addError("maxCacheSize must be at least 1");
            return;
        }
        if (this.maxLoggedOccurrences < 0) {
            addError("maxLoggedOccurrences must be at least 0");
            return;
        }
        if (this.loggerNameSuffixes.isEmpty()) {
            addError("loggerNameSuffixes must contain at least one logger suffix");
            return;
        }
        if (this.levels.isEmpty()) {
            addError("levels must contain at least one logging level");
            return;
        }
        if (this.messageSubstrings.isEmpty()) {
            addError("messageSubstrings must contain at least one message substring");
            return;
        }

        this.repeatCounts = Caffeine.newBuilder()
                                    .maximumSize(this.maxCacheSize)
                                    .build();
        super.start();
    }

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        if (!isStarted() || level == null || logger == null || format == null) {
            return FilterReply.NEUTRAL;
        }
        if (!this.levels.contains(level)) {
            return FilterReply.NEUTRAL;
        }
        if (!matchesLogger(logger.getName())) {
            return FilterReply.NEUTRAL;
        }

        String message = renderMessage(format, params, t);
        if (!matchesMessage(message)) {
            return FilterReply.NEUTRAL;
        }
        if (!matchesPath(message)) {
            return FilterReply.NEUTRAL;
        }

        String key = logger.getName() + '\n' + message;
        long count = this.repeatCounts.get(key, ignored -> new AtomicLong(0L)).incrementAndGet();
        if (this.maxLoggedOccurrences > 0) {
            return count <= this.maxLoggedOccurrences ? FilterReply.NEUTRAL : FilterReply.DENY;
        }
        return count == 1L || count % this.repeatInterval == 0L ? FilterReply.NEUTRAL : FilterReply.DENY;
    }

    private boolean matchesLogger(String loggerName) {
        for (String suffix : this.loggerNameSuffixes) {
            if (loggerName.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesMessage(String message) {
        for (String substring : this.messageSubstrings) {
            if (message.contains(substring)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesPath(String message) {
        if (this.pathSubstrings.isEmpty()) {
            return true;
        }
        for (String substring : this.pathSubstrings) {
            if (message.contains(substring)) {
                return true;
            }
        }
        return false;
    }

    private static String renderMessage(String format, Object[] params, Throwable t) {
        String message;
        if (params == null || params.length == 0) {
            message = format;
        } else {
            message = MessageFormatter.arrayFormat(format, params, t).getMessage();
        }
        if (t == null) {
            return message;
        }

        String throwableMessage = t.getMessage();
        if (throwableMessage == null || throwableMessage.isBlank()) {
            return message + " [" + t.getClass().getName() + "]";
        }
        return message + " [" + t.getClass().getName() + ": " + throwableMessage + "]";
    }

    private static Set<String> splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(value.split(","))
                     .map(String::trim)
                     .filter(part -> !part.isEmpty())
                     .collect(Collectors.toUnmodifiableSet());
    }
}