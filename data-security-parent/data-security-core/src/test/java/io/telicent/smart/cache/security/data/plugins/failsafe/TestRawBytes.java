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
package io.telicent.smart.cache.security.data.plugins.failsafe;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests for {@link RawBytes}, which overrides {@code equals()}, {@code hashCode()} and {@code toString()}.
 * <p>
 * A record holding an array gets identity-based {@code equals()}/{@code hashCode()} by default, so two instances
 * wrapping identical bytes would compare unequal and behave badly in a {@link Set} or map. {@code toString()} is
 * overridden separately so that encoded security labels are never written into logs or error messages - that one is a
 * disclosure concern rather than a correctness one, and is asserted explicitly below.
 * </p>
 */
public class TestRawBytes {

    private static final String LABEL = "clearance=secret";

    private static RawBytes bytesOf(String value) {
        return new RawBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void givenTwoInstancesWithTheSameContent_whenComparing_thenTheyAreEqual() {
        Assert.assertEquals(bytesOf(LABEL), bytesOf(LABEL));
    }

    @Test
    public void givenTwoInstancesWithTheSameContent_whenHashing_thenHashCodesMatch() {
        Assert.assertEquals(bytesOf(LABEL).hashCode(), bytesOf(LABEL).hashCode());
    }

    @Test
    public void givenTheSameInstance_whenComparingWithItself_thenItIsEqual() {
        RawBytes bytes = bytesOf(LABEL);

        Assert.assertEquals(bytes, bytes);
    }

    @Test
    public void givenTwoInstancesWithDifferentContent_whenComparing_thenTheyAreNotEqual() {
        Assert.assertNotEquals(bytesOf(LABEL), bytesOf("clearance=public"));
    }

    @Test
    public void givenInstancesOfDifferentLength_whenComparing_thenTheyAreNotEqual() {
        Assert.assertNotEquals(new RawBytes(new byte[] { 1, 2, 3 }), new RawBytes(new byte[] { 1, 2 }));
    }

    @Test
    public void givenAnInstance_whenComparingWithNull_thenItIsNotEqual() {
        Assert.assertNotEquals(bytesOf(LABEL), null);
    }

    @Test
    public void givenAnInstance_whenComparingWithAnUnrelatedType_thenItIsNotEqual() {
        Assert.assertNotEquals(bytesOf(LABEL), LABEL);
    }

    @Test
    public void givenTwoEmptyInstances_whenComparing_thenTheyAreEqual() {
        Assert.assertEquals(new RawBytes(new byte[0]), new RawBytes(new byte[0]));
    }

    @Test
    public void givenInstancesWithEqualContent_whenUsedInASet_thenTheyDeduplicate() {
        // The point of the equals()/hashCode() overrides - without them this set would hold two entries
        Set<RawBytes> set = new HashSet<>();
        set.add(bytesOf(LABEL));
        set.add(bytesOf(LABEL));

        Assert.assertEquals(set.size(), 1);
    }

    @Test
    public void givenAnInstance_whenConvertingToString_thenOnlyTheLengthIsReported() {
        String actual = bytesOf(LABEL).toString();

        Assert.assertEquals(actual, "RawBytes[length=" + LABEL.length() + "]");
    }

    @Test
    public void givenAnInstance_whenConvertingToString_thenTheContentIsNotDisclosed() {
        // Encoded security labels must never reach a log or an error message
        Assert.assertFalse(bytesOf(LABEL).toString().contains(LABEL),
                           "toString() must not include the wrapped bytes");
    }

    @Test
    public void givenNullContent_whenConvertingToString_thenZeroLengthIsReported() {
        Assert.assertEquals(new RawBytes(null).toString(), "RawBytes[length=0]");
    }
}
