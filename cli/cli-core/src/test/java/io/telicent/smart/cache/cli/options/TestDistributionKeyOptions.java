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
package io.telicent.smart.cache.cli.options;

import com.github.rvesse.airline.SingleCommand;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.configuration.sources.NullSource;
import io.telicent.smart.cache.configuration.sources.PropertiesSource;
import io.telicent.smart.cache.projectors.sinks.CollectorSink;
import io.telicent.smart.cache.projectors.sinks.events.DistributionKeySink;
import io.telicent.smart.cache.sources.DistributionIds;
import io.telicent.smart.cache.sources.DistributionKeyStrategy;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventHeader;
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.TelicentHeaders;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.kafka.common.utils.Bytes;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

@SuppressWarnings("java:S119")
public class TestDistributionKeyOptions extends AbstractOptionsTests {

    private static final String DISTRIBUTION_ID = "http://example.org/distributions/1";

    private static DistributionKeyOptions parse(String... args) {
        return SingleCommand.singleCommand(DistributionKeyOptionsCommand.class).parse(args).keyOptions;
    }

    private static <TKey> SimpleEvent<TKey, String> headerOnlyEvent() {
        List<EventHeader> headers = List.of(new Header(TelicentHeaders.DISTRIBUTION_ID, DISTRIBUTION_ID));
        return new SimpleEvent<>(headers, null, "value");
    }

    @Test
    public void givenNoConfigOrArguments_whenParsing_thenDefaultsUsed() {
        // Given
        Configurator.setSingleSource(NullSource.INSTANCE);

        // When
        DistributionKeyOptions options = parse();

        // Then
        Assert.assertEquals(options.getStrategy(), DistributionKeyStrategy.DEFAULT);
        Assert.assertTrue(options.isEnabled());
    }

    @Test
    public void givenStrategyArgument_whenParsing_thenStrategyUsed() {
        // Given
        Configurator.setSingleSource(NullSource.INSTANCE);

        // When
        DistributionKeyOptions options = parse("--distribution-key-strategy", "distribution-id-and-uuid");

        // Then
        Assert.assertEquals(options.getStrategy(), DistributionKeyStrategy.DISTRIBUTION_ID_AND_UUID);
    }

    @Test
    public void givenStrategyConfig_whenParsing_thenConfigUsedAsDefault() {
        // Given
        Properties properties = new Properties();
        properties.put(DistributionKeyStrategy.CONFIG_KEY, "distribution-id-and-uuid");
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        DistributionKeyOptions options = parse();

        // Then
        Assert.assertEquals(options.getStrategy(), DistributionKeyStrategy.DISTRIBUTION_ID_AND_UUID);
    }

    @Test
    public void givenNoDistributionKeyArgument_whenParsing_thenDisabled() {
        // Given
        Configurator.setSingleSource(NullSource.INSTANCE);

        // When
        DistributionKeyOptions options = parse("--no-distribution-key");

        // Then
        Assert.assertFalse(options.isEnabled());
    }

    @Test
    public void givenEnabledConfigFalse_whenParsing_thenDisabled() {
        // Given
        Properties properties = new Properties();
        properties.put(DistributionKeyStrategy.ENABLED_CONFIG_KEY, "false");
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        DistributionKeyOptions options = parse();

        // Then
        Assert.assertFalse(options.isEnabled());
    }

    @Test
    public void givenBytesKeySink_whenSendingHeaderOnlyEvent_thenBytesKeyIsSetAndDestinationReceivesIt() {
        // Given
        Configurator.setSingleSource(NullSource.INSTANCE);
        DistributionKeyOptions options = parse();
        try (CollectorSink<Event<Bytes, String>> collector = CollectorSink.of()) {
            DistributionKeySink<Bytes, String> sink = options.<String>bytesKeySink(collector).build();
            try {
                // When
                sink.send(headerOnlyEvent());

                // Then
                Assert.assertEquals(collector.get().get(0).key(),
                                    Bytes.wrap(DISTRIBUTION_ID.getBytes(StandardCharsets.UTF_8)));
            } finally {
                sink.close();
            }
        }
    }

    @Test
    public void givenStringKeySinkWithUuidStrategy_whenSendingHeaderOnlyEvent_thenCompositeKeyIsSet() {
        // Given
        Configurator.setSingleSource(NullSource.INSTANCE);
        DistributionKeyOptions options = parse("--distribution-key-strategy", "distribution-id-and-uuid");
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            DistributionKeySink<String, String> sink = options.<String>stringKeySink(collector).build();
            try {
                // When
                sink.send(headerOnlyEvent());

                // Then
                String key = collector.get().get(0).key();
                Assert.assertTrue(key.startsWith(DISTRIBUTION_ID + DistributionIds.KEY_SEPARATOR));
                Assert.assertEquals(DistributionIds.fromKeyString(key), DISTRIBUTION_ID);
            } finally {
                sink.close();
            }
        }
    }

    @Test
    public void givenDisabledKeySink_whenSendingEvent_thenEventKeyIsUnchanged() {
        // Given
        Configurator.setSingleSource(NullSource.INSTANCE);
        DistributionKeyOptions options = parse("--no-distribution-key");
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            DistributionKeySink<String, String> sink = options.<String>stringKeySink(collector).build();
            try {
                // When
                sink.send(headerOnlyEvent());

                // Then
                Assert.assertNull(collector.get().get(0).key());
            } finally {
                sink.close();
            }
        }
    }
}
