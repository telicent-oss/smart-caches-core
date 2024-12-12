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
package io.telicent.smart.cache.security.plugins.failsafe;

import io.telicent.smart.cache.security.AbstractSecurityPrimitive;
import io.telicent.smart.cache.security.entitlements.Entitlements;
import io.telicent.smart.cache.security.labels.SecurityLabels;

import java.util.Base64;

/**
 * A raw security primitive where the decoded form of the primitive is merely a wrapper around its encoded bytes
 * <p>
 * Intended primarily for use by {@link FailSafePlugin} but other plugins <strong>MAY</strong> wish to use this in cases
 * where they are asked to apply labels in other schemas.  They could still allow an application to apply those labels
 * even if they themselves can't process them in other ways.
 * </p>
 */
public final class RawPrimitive extends AbstractSecurityPrimitive
        implements Entitlements<RawBytes>, SecurityLabels<RawBytes> {

    private final RawBytes rawBytes;

    /**
     * Creates a new raw primitive, package constructor intended only for use by {@link FailSafePlugin}
     *
     * @param encoded Encoded bytes
     */
    RawPrimitive(byte[] encoded) {
        super(FailSafePlugin.SCHEMA, encoded);
        this.rawBytes = new RawBytes(this.encoded());
    }

    /**
     * Creates a new raw primitive
     *
     * @param schema  Schema ID
     * @param encoded Encoded primitive (labels/entitlements)
     */
    public RawPrimitive(short schema, byte[] encoded) {
        super(schema, encoded);
        this.rawBytes = new RawBytes(this.encoded());
    }

    @Override
    public RawBytes decodedEntitlements() {
        return rawBytes;
    }

    @Override
    public RawBytes decodedLabels() {
        return rawBytes;
    }

    @Override
    public String toDebugString() {
        return Base64.getEncoder().encodeToString(this.encoded());
    }
}
