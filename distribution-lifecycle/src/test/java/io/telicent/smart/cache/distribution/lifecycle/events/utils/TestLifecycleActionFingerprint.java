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
package io.telicent.smart.cache.distribution.lifecycle.events.utils;

import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.distribution.util.HexGenerator;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class TestLifecycleActionFingerprint {

    private static final UUID EVENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OTHER_EVENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    /**
     * The fingerprint of {@link #reference()}, precomputed independently of this codebase.
     * <p>
     * This is deliberately a hard coded value.  The whole point of the fingerprint is that services can compute it
     * separately and reach the same verdict, so an accidental change to the canonical encoding must fail the build
     * rather than silently give every service a new set of fingerprints.  If you intend to change the encoding then
     * change {@link LifecycleActionFingerprint#FINGERPRINT_VERSION} and update this value.
     * </p>
     */
    private static final String REFERENCE_FINGERPRINT =
            "94ce441e65a5d22c9aca91b5522c62dce36abbc674690ce36f9cccac02170ae5";

    /**
     * Values chosen to exercise the three ways of counting Unicode, written as escapes so this source file stays pure
     * ASCII and cannot be corrupted in transit:
     * <ul>
     *     <li>{@code distribution-} plus U+1F9EA TEST TUBE, which is 15 Java chars, 14 code points and 17 UTF-8 bytes</li>
     *     <li>Japanese characters, which are 1 Java char each but 3 UTF-8 bytes each</li>
     *     <li>Latin e with diaeresis, which is 1 Java char but 2 UTF-8 bytes</li>
     * </ul>
     */
    private static final String UNICODE_DISTRIBUTION_ID = "distribution-\uD83E\uDDEA";
    private static final String UNICODE_DATASET_ID = "\u65E5\u672C\u8A9E-dataset";
    private static final String UNICODE_USER = "t\u00EBst@t\u00EBst.org";

    /**
     * The fingerprint of {@link #unicodeReference()}, precomputed independently of this codebase, see
     * {@link #REFERENCE_FINGERPRINT} for why this is hard coded
     */
    private static final String UNICODE_REFERENCE_FINGERPRINT =
            "4172a203fff2bf51d6928d9b0315060301516457c882152d9dde6f8862c8de2f";

    private static LifecycleAction.LifecycleActionBuilder builder() {
        return LifecycleAction.builder()
                              .eventId(EVENT_ID)
                              .distributionId("test-distribution")
                              .datasetId("test-dataset")
                              .user("test@test.org")
                              .state(new LifecycleStateTransition(DistributionLifecycleState.Registered,
                                                                  DistributionLifecycleState.Active));
    }

    private static LifecycleAction reference() {
        return builder().build();
    }

    private static LifecycleAction unicodeReference() {
        return builder().eventId(OTHER_EVENT_ID)
                        .distributionId(UNICODE_DISTRIBUTION_ID)
                        .datasetId(UNICODE_DATASET_ID)
                        .user(UNICODE_USER)
                        .build();
    }

    private static LifecycleAction modified(Consumer<LifecycleAction.LifecycleActionBuilder> modification) {
        LifecycleAction.LifecycleActionBuilder builder = builder();
        modification.accept(builder);
        return builder.build();
    }

    @Test
    public void givenTwoIdenticalActions_whenFingerprinting_thenFingerprintsMatch() {
        // Given
        LifecycleAction a = reference();
        LifecycleAction b = reference();

        // When and Then
        Assert.assertNotSame(a, b);
        Assert.assertEquals(LifecycleActionFingerprint.of(b), LifecycleActionFingerprint.of(a));
        Assert.assertTrue(LifecycleActionFingerprint.matches(a, b));
        Assert.assertNull(LifecycleActionFingerprint.describeDifference(a, b));
    }

    @Test
    public void givenReferenceAction_whenFingerprinting_thenFingerprintIsStable() {
        // Given
        LifecycleAction action = reference();

        // When
        String fingerprint = LifecycleActionFingerprint.of(action);

        // Then
        Assert.assertEquals(fingerprint, REFERENCE_FINGERPRINT);
        Assert.assertEquals(fingerprint, HexGenerator.sha256Hex(LifecycleActionFingerprint.canonicalForm(action)));
    }

    @Test
    public void givenReferenceAction_whenBuildingCanonicalForm_thenVersionIsIncluded_andAllSemanticFieldsArePresent() {
        // Given
        LifecycleAction action = reference();

        // When
        String canonical = LifecycleActionFingerprint.canonicalForm(action);

        // Then
        Assert.assertTrue(canonical.startsWith(LifecycleActionFingerprint.FINGERPRINT_VERSION + "\n"), canonical);
        for (String field : List.of("eventId", "distributionId", "datasetId", "state.from", "state.to", "user")) {
            Assert.assertTrue(canonical.contains("\n" + field + ":"), "Missing field " + field + " in " + canonical);
        }
    }

    @Test
    public void givenReferenceAction_whenGettingFields_thenFieldsAreInCanonicalOrder() {
        // Given
        LifecycleAction action = reference();

        // When
        Map<String, String> fields = LifecycleActionFingerprint.fields(action);

        // Then
        Assert.assertEquals(List.copyOf(fields.keySet()),
                            List.of("eventId", "distributionId", "datasetId", "state.from", "state.to", "user"));
        Assert.assertEquals(fields.get("distributionId"), "test-distribution");
        Assert.assertEquals(fields.get("state.to"), DistributionLifecycleState.Active.name());
    }

    @DataProvider
    public Object[][] semanticChanges() {
        return new Object[][] {
                { "eventId", (Consumer<LifecycleAction.LifecycleActionBuilder>) b -> b.eventId(OTHER_EVENT_ID) },
                {
                        "distributionId",
                        (Consumer<LifecycleAction.LifecycleActionBuilder>) b -> b.distributionId("other-distribution")
                },
                { "datasetId", (Consumer<LifecycleAction.LifecycleActionBuilder>) b -> b.datasetId("other-dataset") },
                { "datasetId", (Consumer<LifecycleAction.LifecycleActionBuilder>) b -> b.datasetId(null) },
                { "datasetId", (Consumer<LifecycleAction.LifecycleActionBuilder>) b -> b.datasetId("") },
                { "user", (Consumer<LifecycleAction.LifecycleActionBuilder>) b -> b.user("other@test.org") },
                {
                        "state.from",
                        (Consumer<LifecycleAction.LifecycleActionBuilder>) b -> b.state(
                                new LifecycleStateTransition(DistributionLifecycleState.Unregistered,
                                                             DistributionLifecycleState.Active))
                },
                {
                        "state.to",
                        (Consumer<LifecycleAction.LifecycleActionBuilder>) b -> b.state(
                                new LifecycleStateTransition(DistributionLifecycleState.Registered,
                                                             DistributionLifecycleState.Withdrawn))
                }
                };
    }

    @Test(dataProvider = "semanticChanges")
    public void givenActionsDifferingInOneField_whenFingerprinting_thenFingerprintsDiffer(String field,
                                                                                          Consumer<LifecycleAction.LifecycleActionBuilder> change) {
        // Given
        LifecycleAction original = reference();
        LifecycleAction changed = modified(change);

        // When
        String difference = LifecycleActionFingerprint.describeDifference(original, changed);

        // Then
        Assert.assertNotEquals(LifecycleActionFingerprint.of(changed), LifecycleActionFingerprint.of(original));
        Assert.assertFalse(LifecycleActionFingerprint.matches(original, changed));
        Assert.assertNotNull(difference);
        Assert.assertTrue(difference.contains(field), "Expected " + field + " to be reported in " + difference);
    }

    @Test
    public void givenActionsWithNullAndEmptyDatasetId_whenFingerprinting_thenFingerprintsDiffer() {
        // Given
        LifecycleAction absent = modified(b -> b.datasetId(null));
        LifecycleAction empty = modified(b -> b.datasetId(""));

        // When and Then
        Assert.assertNotEquals(LifecycleActionFingerprint.of(empty), LifecycleActionFingerprint.of(absent));
    }

    @Test
    public void givenActionsWhoseFieldValuesCouldRunTogether_whenFingerprinting_thenFingerprintsDiffer() {
        // Given
        // These would collide under a naive delimiter based encoding, the length prefixed canonical form must keep
        // them apart
        LifecycleAction runTogether = modified(x -> x.distributionId("ab").datasetId(null));
        LifecycleAction splitApart = modified(x -> x.distributionId("a").datasetId("b"));
        LifecycleAction injected =
                modified(x -> x.distributionId("test-distribution\ndatasetId:12:test-dataset").datasetId(null));

        // When and Then
        Assert.assertNotEquals(LifecycleActionFingerprint.of(splitApart), LifecycleActionFingerprint.of(runTogether));
        Assert.assertNotEquals(LifecycleActionFingerprint.of(injected), LifecycleActionFingerprint.of(reference()));
    }

    @Test
    public void givenNullActions_whenMatching_thenBothNullMatch_andOneNullDoesNot() {
        // Given
        LifecycleAction action = reference();

        // When and Then
        Assert.assertTrue(LifecycleActionFingerprint.matches(null, null));
        Assert.assertFalse(LifecycleActionFingerprint.matches(action, null));
        Assert.assertFalse(LifecycleActionFingerprint.matches(null, action));
        Assert.assertTrue(LifecycleActionFingerprint.matches(action, action));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNullAction_whenFingerprinting_thenNullPointerException() {
        // Given, When and Then
        LifecycleActionFingerprint.of(null);
    }

    @Test
    public void givenNullActions_whenDescribingDifference_thenDifferenceIsReported() {
        // Given
        LifecycleAction action = reference();

        // When and Then
        Assert.assertNull(LifecycleActionFingerprint.describeDifference(null, null));
        Assert.assertNotNull(LifecycleActionFingerprint.describeDifference(null, action));
        Assert.assertNotNull(LifecycleActionFingerprint.describeDifference(action, null));
    }

    @Test
    public void givenActionsDifferingInSeveralFields_whenDescribingDifference_thenAllDifferencesAreReported() {
        // Given
        LifecycleAction original = reference();
        LifecycleAction changed = modified(b -> b.user("other@test.org")
                                                 .state(new LifecycleStateTransition(
                                                         DistributionLifecycleState.Registered,
                                                         DistributionLifecycleState.Deleted)));

        // When
        String difference = LifecycleActionFingerprint.describeDifference(original, changed);

        // Then
        Assert.assertNotNull(difference);
        Assert.assertTrue(difference.contains("state.to: existing=Active, rejected=Deleted"), difference);
        Assert.assertTrue(difference.contains("user: existing=test@test.org, rejected=other@test.org"), difference);
        Assert.assertTrue(difference.contains("; "), difference);
        Assert.assertFalse(difference.contains("distributionId"), difference);
    }

    @Test
    public void givenUnicodeAction_whenFingerprinting_thenFingerprintIsStable() {
        // Given
        LifecycleAction action = unicodeReference();

        // When
        String fingerprint = LifecycleActionFingerprint.of(action);

        // Then
        Assert.assertEquals(fingerprint, UNICODE_REFERENCE_FINGERPRINT);
    }

    @Test
    public void givenUnicodeAction_whenBuildingCanonicalForm_thenLengthsAreUtf8ByteCounts() {
        // Given
        LifecycleAction action = unicodeReference();

        // When
        String canonical = LifecycleActionFingerprint.canonicalForm(action);

        // Then
        // NB - The lengths recorded MUST be UTF-8 byte counts, if they were Java String lengths these would read 15,
        //      11 and 13 respectively, and an implementation in another language would disagree with us
        Assert.assertEquals(UNICODE_DISTRIBUTION_ID.length(), 15);
        Assert.assertTrue(canonical.contains("distributionId:17:" + UNICODE_DISTRIBUTION_ID), canonical);
        Assert.assertTrue(canonical.contains("datasetId:17:" + UNICODE_DATASET_ID), canonical);
        Assert.assertTrue(canonical.contains("user:15:" + UNICODE_USER), canonical);
    }

    @Test
    public void givenAnyAction_whenFingerprinting_thenItIsTheSha256OfTheCanonicalBytes() throws Exception {
        // Given
        for (LifecycleAction action : List.of(reference(), unicodeReference())) {
            byte[] canonicalBytes = LifecycleActionFingerprint.canonicalBytes(action);

            // When
            // NB - Computed here without any of our own helpers, so this asserts what an independent implementation
            //      would have to do, i.e. hash the UTF-8 bytes of the canonical form
            String independent = HexFormat.of()
                                          .formatHex(MessageDigest.getInstance("SHA-256").digest(canonicalBytes));

            // Then
            Assert.assertEquals(LifecycleActionFingerprint.of(action), independent);
            Assert.assertEquals(new String(canonicalBytes, StandardCharsets.UTF_8),
                                LifecycleActionFingerprint.canonicalForm(action));
        }
    }

    @Test
    public void givenActionsDifferingOnlyInNonAsciiValues_whenFingerprinting_thenFingerprintsDiffer() {
        // Given
        LifecycleAction plain = modified(b -> b.datasetId("dataset"));
        LifecycleAction accented = modified(b -> b.datasetId("dat\u00EBset"));
        LifecycleAction emoji = modified(b -> b.datasetId("dataset\uD83E\uDDEA"));

        // When and Then
        Assert.assertNotEquals(LifecycleActionFingerprint.of(accented), LifecycleActionFingerprint.of(plain));
        Assert.assertNotEquals(LifecycleActionFingerprint.of(emoji), LifecycleActionFingerprint.of(plain));
        Assert.assertNotEquals(LifecycleActionFingerprint.of(emoji), LifecycleActionFingerprint.of(accented));
    }

    @Test
    public void givenAnActionWithNoStateTransition_whenFingerprinting_thenTheStateFieldsAreAbsentRatherThanThrowing() {
        // Given
        // NB - Mocked because the builder rejects a null state, but fingerprinting runs on error paths where an
        //      incomplete action may be all we have to report on, so it must not throw
        LifecycleAction incomplete = Mockito.mock(LifecycleAction.class);
        Mockito.when(incomplete.getEventId()).thenReturn(EVENT_ID);
        Mockito.when(incomplete.getDistributionId()).thenReturn("test-distribution");
        Mockito.when(incomplete.getDatasetId()).thenReturn("test-dataset");
        Mockito.when(incomplete.getUser()).thenReturn("test@test.org");

        // When
        Map<String, String> fields = LifecycleActionFingerprint.fields(incomplete);
        String canonical = LifecycleActionFingerprint.canonicalForm(incomplete);

        // Then
        Assert.assertNull(incomplete.getState());
        Assert.assertNull(fields.get("state.from"));
        Assert.assertNull(fields.get("state.to"));
        Assert.assertTrue(canonical.contains("state.from:-1:"), canonical);
        Assert.assertTrue(canonical.contains("state.to:-1:"), canonical);
        // And it is still a usable fingerprint, and still distinguishable from the complete action
        Assert.assertEquals(LifecycleActionFingerprint.of(incomplete).length(), 64);
        Assert.assertFalse(LifecycleActionFingerprint.matches(incomplete, reference()));
        Assert.assertTrue(LifecycleActionFingerprint.describeDifference(reference(), incomplete)
                                                    .contains("state.from"));
    }
}
