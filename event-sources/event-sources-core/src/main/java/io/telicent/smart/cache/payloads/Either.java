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
package io.telicent.smart.cache.payloads;

import lombok.Getter;

/**
 * A helper container class that allows a payload value to be represented as one of two possible types
 *
 * @param <A> Possible type A
 * @param <B> Possible type B
 */
@Getter
public final class Either<A, B> {

    private final A a;
    private final B b;

    /**
     * Creates a new container
     * <p>
     * Only one of the value parameters is permitted to be non-null, if both are null or both are set, an error is thrown.
     * </p>
     *
     * @param a Value
     * @param b Value
     * @throws IllegalArgumentException Thrown if both values are set
     */
    public Either(A a, B b) {
        if (a != null && b != null) throw new IllegalArgumentException("Can't set both value types");
        if (a == null && b == null) throw new IllegalArgumentException("Must set one value types");
        this.a = a;
        this.b = b;
    }

    /**
     * Gets whether the container has a value of the first possible type
     *
     * @return True if the first possible type, false otherwise
     */
    public boolean isA() {
        return this.a != null;
    }

    /**
     * Gets whether the container has a value of the second possible type
     *
     * @return True if the second possible type, false otherwise
     */
    public boolean isB() {
        return this.b != null;
    }
}
