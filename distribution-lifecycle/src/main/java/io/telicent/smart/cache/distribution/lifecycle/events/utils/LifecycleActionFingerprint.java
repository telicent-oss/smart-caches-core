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

import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.distribution.util.HexGenerator;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Computes a canonical fingerprint for a {@link LifecycleAction}.
 * <p>
 * Lifecycle Action Event IDs are immutable, i.e. an Event ID identifies exactly one action for all time.  Services
 * therefore need to decide whether a re-delivered event is a harmless duplicate, which happens routinely because the
 * lifecycle topic is replayed on startup, or a genuine conflict where an Event ID has been reused with different
 * content.  Making that decision with {@link Object#equals(Object)} is brittle because it depends on the exact Java
 * class, its declared fields and its Lombok generated equality, none of which are stable across services or across
 * versions of this library.
 * </p>
 * <p>
 * This class instead derives the decision from the semantic fields of the action only:
 * </p>
 * <ul>
 *     <li>{@code eventId}</li>
 *     <li>{@code distributionId}</li>
 *     <li>{@code datasetId}</li>
 *     <li>{@code state.from}</li>
 *     <li>{@code state.to}</li>
 *     <li>{@code user}</li>
 * </ul>
 * <p>
 * Those fields are written into a {@linkplain #canonicalForm(LifecycleAction) canonical form}, a deterministic text
 * encoding that is then hashed to give the {@linkplain #of(LifecycleAction) fingerprint}.  Two actions are the same
 * action if, and only if, their fingerprints are equal, so any service that implements the same encoding reaches the
 * same verdict, and fingerprints can safely be logged, reported over an API or compared across processes.
 * </p>
 * <p>
 * The encoding is defined in terms of bytes, not Java {@code String}s, so that implementations in other languages
 * agree with this one:
 * </p>
 * <ol>
 *     <li>Everything is UTF-8, and it is the UTF-8 bytes that are hashed.</li>
 *     <li>
 *         The canonical form starts with the line {@value #FINGERPRINT_VERSION}, then one line per field in the order
 *         listed above.  Lines are separated by a single line feed ({@code U+000A}), including a trailing one.
 *     </li>
 *     <li>
 *         Each field line is {@code name:length:value}, where {@code length} is the number of <strong>UTF-8
 *         bytes</strong> in the value written in decimal, or {@code -1} when the value is absent, in which case the
 *         value is empty.  Lengths are deliberately byte counts rather than character counts because languages
 *         disagree about what a character is, e.g. Java counts a UTF-16 code unit so an emoji counts twice, whilst
 *         Python counts a code point so it counts once.
 *     </li>
 *     <li>The fingerprint is the SHA-256 of those bytes, rendered as lower case hex.</li>
 * </ol>
 * <p>
 * The canonical form is versioned via {@value #FINGERPRINT_VERSION}.  If the set of fields covered ever changes then
 * that version <strong>MUST</strong> be changed as well, so that fingerprints computed under different rules can never
 * be mistaken for one another.
 * </p>
 */
public final class LifecycleActionFingerprint {

    /**
     * Version identifier for the canonical form, included in the canonical form itself so that fingerprints computed
     * under different encoding rules can never compare equal
     */
    public static final String FINGERPRINT_VERSION = "distribution-lifecycle-action-fingerprint/v1";

    /**
     * Length written for a {@code null} field value, chosen because no real value can have a negative length so a
     * {@code null} value can never be confused with any actual value, including the empty string
     */
    private static final int NULL_LENGTH = -1;

    /**
     * Separator between the length prefix and the value, and between a field name and its length
     */
    private static final char FIELD_SEPARATOR = ':';

    /**
     * Line separator, hard coded rather than platform dependent as the canonical form must not vary by platform
     */
    private static final char LINE_SEPARATOR = '\n';

    private LifecycleActionFingerprint() {
        // Utility class, not meant to be instantiated
    }

    /**
     * Gets the semantic fields of an action, in canonical order
     * <p>
     * This is the single definition of what the fingerprint covers, it is used both to build the
     * {@linkplain #canonicalForm(LifecycleAction) canonical form} and to
     * {@linkplain #describeDifference(LifecycleAction, LifecycleAction) describe differences} so the two can never
     * disagree about which fields matter.
     * </p>
     *
     * @param action Lifecycle action
     * @return Map of field names to values, in canonical order, values may be {@code null}
     * @throws NullPointerException Thrown if the provided action is {@code null}
     */
    public static Map<String, String> fields(LifecycleAction action) {
        Objects.requireNonNull(action, "Action cannot be null");
        LifecycleStateTransition state = action.getState();

        // NB - LinkedHashMap as the canonical form depends upon a fixed field order
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("eventId", Objects.toString(action.getEventId(), null));
        fields.put("distributionId", action.getDistributionId());
        fields.put("datasetId", action.getDatasetId());
        // NB - state is declared @NonNull but we are defensive here as fingerprinting is used on error paths where an
        //      incomplete action may be all we have to report on
        fields.put("state.from", state != null ? Objects.toString(state.getFrom(), null) : null);
        fields.put("state.to", state != null ? Objects.toString(state.getTo(), null) : null);
        fields.put("user", action.getUser());
        return fields;
    }

    /**
     * Computes the canonical form of an action
     * <p>
     * Each field is written as {@code name:length:value} on its own line, where {@code length} is the number of UTF-8
     * bytes in the value, or {@code -1} when the value is {@code null}.  Encoding the length means that field values
     * which themselves contain delimiter characters, or newlines, cannot be arranged to look like a different set of
     * field values, i.e. the encoding is unambiguous.
     * </p>
     * <p>
     * It is the {@linkplain #canonicalBytes(LifecycleAction) UTF-8 bytes} of this text that are hashed, this method
     * exists so the encoding can be inspected and asserted upon directly.
     * </p>
     *
     * @param action Lifecycle action
     * @return Canonical form
     * @throws NullPointerException Thrown if the provided action is {@code null}
     */
    public static String canonicalForm(LifecycleAction action) {
        StringBuilder builder = new StringBuilder();
        builder.append(FINGERPRINT_VERSION).append(LINE_SEPARATOR);
        for (Map.Entry<String, String> field : fields(action).entrySet()) {
            String value = field.getValue();
            builder.append(field.getKey())
                   .append(FIELD_SEPARATOR)
                   .append(value != null ? utf8Length(value) : NULL_LENGTH)
                   .append(FIELD_SEPARATOR)
                   .append(value != null ? value : "")
                   .append(LINE_SEPARATOR);
        }
        return builder.toString();
    }

    /**
     * Computes the canonical bytes of an action, i.e. the exact bytes that are hashed to produce the
     * {@linkplain #of(LifecycleAction) fingerprint}
     *
     * @param action Lifecycle action
     * @return Canonical form encoded as UTF-8
     * @throws NullPointerException Thrown if the provided action is {@code null}
     */
    public static byte[] canonicalBytes(LifecycleAction action) {
        return canonicalForm(action).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Gets the number of UTF-8 bytes in a value
     *
     * @param value Value
     * @return Length in UTF-8 bytes
     */
    private static int utf8Length(String value) {
        return value.getBytes(StandardCharsets.UTF_8).length;
    }

    /**
     * Computes the fingerprint of an action, a SHA-256 hash of its {@linkplain #canonicalBytes(LifecycleAction)
     * canonical bytes}
     *
     * @param action Lifecycle action
     * @return Fingerprint as a lower case hex string
     * @throws NullPointerException Thrown if the provided action is {@code null}
     */
    public static String of(LifecycleAction action) {
        // NB - HexGenerator hashes the UTF-8 bytes of the string it is given, i.e. exactly canonicalBytes()
        return HexGenerator.sha256Hex(canonicalForm(action));
    }

    /**
     * Determines whether two actions are semantically the same action
     * <p>
     * Two {@code null} actions are considered to match, a {@code null} and a non-{@code null} action never match.
     * </p>
     *
     * @param a First action
     * @param b Second action
     * @return True if both actions have the same fingerprint, false otherwise
     */
    public static boolean matches(LifecycleAction a, LifecycleAction b) {
        if (a == null || b == null) {
            return a == b;
        } else if (a == b) {
            return true;
        }
        return Objects.equals(of(a), of(b));
    }

    /**
     * Describes how two actions differ, intended for logging, dead letter reasons and conflict reports
     *
     * @param existing Action already held, i.e. the one that is being kept
     * @param rejected Action that conflicts with it, i.e. the one that is being rejected
     * @return Human readable description of the differing fields, or {@code null} if the actions match
     */
    public static String describeDifference(LifecycleAction existing, LifecycleAction rejected) {
        if (matches(existing, rejected)) {
            return null;
        } else if (existing == null || rejected == null) {
            return existing == null ? "no existing action to compare against" : "no replacement action to compare with";
        }

        Map<String, String> existingFields = fields(existing);
        Map<String, String> rejectedFields = fields(rejected);
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> field : existingFields.entrySet()) {
            String was = field.getValue();
            String now = rejectedFields.get(field.getKey());
            if (Objects.equals(was, now)) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append("; ");
            }
            builder.append(field.getKey()).append(": existing=").append(was).append(", rejected=").append(now);
        }

        // NB - Should be unreachable, differing fingerprints imply at least one differing field, but a fingerprint
        //      collision would land here and must not be reported as an empty difference
        return builder.isEmpty() ? "fields are equal but fingerprints differ" : builder.toString();
    }
}
