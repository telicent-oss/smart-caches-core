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

import io.telicent.smart.cache.security.plugins.SecurityPlugin;
import io.telicent.smart.cache.security.Authorizer;
import io.telicent.smart.cache.security.attributes.UserAttributes;
import io.telicent.smart.cache.security.labels.SecurityLabels;
import io.telicent.smart.cache.security.requests.RequestContext;

/**
 * An authorizer that denies all access
 * <p>
 * This is intended primarily for use by the {@link FailSafePlugin} but may also be used be other plugins if they are
 * asked to prepare an {@link Authorizer} via
 * {@link SecurityPlugin#prepareAuthorizer(UserAttributes)} instance using an
 * {@link UserAttributes} instance they do not support.
 * </p>
 */
public final class FailSafeAuthorizer implements Authorizer {

    /**
     * Public singleton instance of the authorizer
     */
    public static final Authorizer INSTANCE = new FailSafeAuthorizer();

    /**
     * Private constructor to prevent direct instantiation, use {@link #INSTANCE} to obtain the singleton instance
     * instead
     */
    private FailSafeAuthorizer() {

    }

    @Override
    public boolean canRead(SecurityLabels<?> labels) {
        return false;
    }

    @Override
    public boolean canWrite(SecurityLabels<?> labels) {
        return false;
    }

    @Override
    public boolean canMakeRequest(SecurityLabels<?> labels, RequestContext context) {
        return false;
    }

    @Override
    public void close() throws Exception {
        // No-op
    }
}
