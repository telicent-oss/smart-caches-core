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
package io.telicent.smart.cache.sources;

import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("java:S119")
public class TestDistributionIds {

    private static final String DISTRIBUTION_URI = "https://telicent.io/datasets/acled#2026-08-release";
    private static final String SIMPLE_ID = "acled";

    private static <TKey> Event<TKey, String> event(TKey key, String distributionIdHeader) {
        List<EventHeader> headers = distributionIdHeader != null ? List.of(
                new Header(TelicentHeaders.DISTRIBUTION_ID, distributionIdHeader)) : Collections.emptyList();
        return new SimpleEvent<>(headers, key, "value");
    }

    private static byte[] utf8(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    @DataProvider(name = "plainKeys")
    private Object[][] plainKeys() {
        return new Object[][] {
                { SIMPLE_ID },
                { DISTRIBUTION_URI },
                // A Distribution ID that is a URI routinely contains the key separator itself
                { "https://telicent.io/datasets/acled" },
                // A trailing segment that is not a UUID must not be stripped
                { DISTRIBUTION_URI + "/not-a-uuid" },
                // A UUID may legitimately be the whole Distribution ID
                { "13bce3bf-7edb-4efb-a54f-574327458dd7" }
        };
    }

    @Test(dataProvider = "plainKeys")
    public void givenPlainKey_whenDecoding_thenKeyIsTheDistributionId(String key) {
        // Given, When
        String actual = DistributionIds.fromKeyString(key);

        // Then
        Assert.assertEquals(actual, key);
    }

    @Test(dataProvider = "plainKeys")
    public void givenCompositeKey_whenDecoding_thenSuffixIsStripped(String distributionId) {
        // Given
        String key = distributionId + DistributionIds.KEY_SEPARATOR + UUID.randomUUID();

        // When
        String actual = DistributionIds.fromKeyString(key);

        // Then
        Assert.assertEquals(actual, distributionId);
    }

    @DataProvider(name = "noDistributionId")
    private Object[][] noDistributionId() {
        return new Object[][] { { null }, { "" }, { "   " } };
    }

    @Test(dataProvider = "noDistributionId")
    public void givenBlankKey_whenDecoding_thenNull(String key) {
        // Given, When and Then
        Assert.assertNull(DistributionIds.fromKeyString(key));
    }

    @Test
    public void givenKeyThatIsOnlyASeparatorAndUuid_whenDecoding_thenKeyIsTheDistributionId() {
        // Given - the separator is at index 0 so there is no Distribution ID portion to strip back to
        String key = DistributionIds.KEY_SEPARATOR + UUID.randomUUID();

        // When and Then
        Assert.assertEquals(DistributionIds.fromKeyString(key), key);
    }

    @Test
    public void givenUtf8KeyBytes_whenDecoding_thenDistributionId() {
        // Given, When and Then
        Assert.assertEquals(DistributionIds.fromKeyBytes(utf8(DISTRIBUTION_URI)), DISTRIBUTION_URI);
    }

    @Test
    public void givenNonUtf8KeyBytes_whenDecoding_thenNull() {
        // Given - 0xC3 starts a 2 byte sequence, 0x28 cannot continue it
        byte[] rawKey = new byte[] { (byte) 0xC3, (byte) 0x28 };

        // When and Then
        Assert.assertNull(DistributionIds.fromKeyBytes(rawKey),
                          "A key that is not valid UTF-8 does not convey a Distribution ID");
    }

    @DataProvider(name = "emptyKeyBytes")
    private Object[][] emptyKeyBytes() {
        return new Object[][] { { null }, { new byte[0] } };
    }

    @Test(dataProvider = "emptyKeyBytes")
    public void givenNoKeyBytes_whenDecoding_thenNull(byte[] rawKey) {
        // Given, When and Then
        Assert.assertNull(DistributionIds.fromKeyBytes(rawKey));
    }

    @Test
    public void givenUuidKey_whenDecoding_thenNull() {
        // Given - the lifecycle and action tracker topics are UUID keyed and those keys are not Distribution IDs
        Assert.assertNull(DistributionIds.fromKey(UUID.randomUUID()));
    }

    @Test
    public void givenNullKey_whenDecoding_thenNull() {
        // Given, When and Then
        Assert.assertNull(DistributionIds.fromKey(null));
    }

    @Test
    public void givenEventWithKeyOnly_whenResolving_thenKeyIsUsed() {
        // Given
        Event<String, String> event = event(DISTRIBUTION_URI, null);

        // When and Then
        Assert.assertEquals(DistributionIds.resolve(event), DISTRIBUTION_URI);
    }

    @Test
    public void givenEventWithHeaderOnly_whenResolving_thenHeaderIsUsed() {
        // Given - i.e. an event from a pipeline that predates message keys
        Event<String, String> event = event(null, DISTRIBUTION_URI);

        // When and Then
        Assert.assertEquals(DistributionIds.resolve(event), DISTRIBUTION_URI);
    }

    @Test
    public void givenEventWhereKeyAndHeaderDisagree_whenResolving_thenKeyWins() {
        // Given
        Event<String, String> event = event(DISTRIBUTION_URI, "some-other-distribution");

        // When and Then
        Assert.assertEquals(DistributionIds.resolve(event), DISTRIBUTION_URI,
                            "The message key is authoritative, the header is only the fallback");
    }

    @Test
    public void givenEventWithCompositeKeyAndHeader_whenResolving_thenKeyIsStrippedAndWins() {
        // Given
        Event<String, String> event =
                event(DISTRIBUTION_URI + DistributionIds.KEY_SEPARATOR + UUID.randomUUID(), "some-other-distribution");

        // When and Then
        Assert.assertEquals(DistributionIds.resolve(event), DISTRIBUTION_URI);
    }

    @Test
    public void givenEventWithUnusableKey_whenResolving_thenHeaderIsUsed() {
        // Given - a UUID key conveys no Distribution ID so the header must still be honoured
        Event<UUID, String> event = event(UUID.randomUUID(), DISTRIBUTION_URI);

        // When and Then
        Assert.assertEquals(DistributionIds.resolve(event), DISTRIBUTION_URI);
    }

    @Test
    public void givenEventWithNeither_whenResolving_thenNull() {
        // Given
        Event<String, String> event = event(null, null);

        // When and Then
        Assert.assertNull(DistributionIds.resolve(event));
    }

    @Test
    public void givenNullEvent_whenResolving_thenNull() {
        // Given, When and Then
        Assert.assertNull(DistributionIds.resolve(null));
        Assert.assertNull(DistributionIds.fromHeader(null));
    }

    @Test
    public void givenEventWithBlankHeader_whenReadingHeader_thenNull() {
        // Given
        Event<String, String> event = event(null, "   ");

        // When and Then
        Assert.assertNull(DistributionIds.fromHeader(event));
    }

    @Test
    public void givenDistributionId_whenGeneratingKeyBytes_thenRoundTrips() {
        // Given, When
        byte[] rawKey = DistributionIds.toKeyBytes(DISTRIBUTION_URI, DistributionKeyStrategy.DISTRIBUTION_ID_AND_UUID);

        // Then
        Assert.assertNotNull(rawKey);
        Assert.assertEquals(DistributionIds.fromKeyBytes(rawKey), DISTRIBUTION_URI);
    }

    @Test
    public void givenNoDistributionId_whenGeneratingKeyBytes_thenNull() {
        // Given, When and Then
        Assert.assertNull(DistributionIds.toKeyBytes(null, DistributionKeyStrategy.DISTRIBUTION_ID));
        Assert.assertNull(DistributionIds.toKeyBytes("  ", DistributionKeyStrategy.DISTRIBUTION_ID));
    }

    @Test
    public void givenNoStrategy_whenGeneratingKey_thenDefaultIsUsed() {
        // Given, When and Then
        Assert.assertEquals(DistributionIds.toKey(DISTRIBUTION_URI, null), DISTRIBUTION_URI);
    }
}
