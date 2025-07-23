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
package io.telicent.smart.caches.configuration.auth;

import io.telicent.servlet.auth.jwt.configuration.RuntimeConfigurationAdaptor;
import io.telicent.smart.cache.configuration.Configurator;

import java.util.function.Function;

import static io.telicent.servlet.auth.jwt.configuration.ConfigurationParameters.*;
import static io.telicent.servlet.auth.jwt.verifier.aws.AwsVerificationProvider.PARAM_AWS_REGION;
import static org.apache.commons.lang3.Strings.CS;

/**
 * An adaptor from our environment variable driven configuration approach to the JWT Servlet Auth libraries runtime
 * independent configuration mechanism
 */
public abstract class TelicentConfigurationAdaptor implements RuntimeConfigurationAdaptor {
    private final boolean usingAws;

    /**
     * Creates a new adaptor
     */
    public TelicentConfigurationAdaptor() {
        this.usingAws =
                CS.startsWith(Configurator.get(AuthConstants.ENV_JWKS_URL), AuthConstants.AUTH_PREFIX_AWS);
    }

    @Override
    public final String getParameter(String param) {
        // Adapt from the parameter names that the JWT Servlet Auth library uses to our defined environment variables
        // where appropriate and inject our default configuration as needed
        return switch (param) {
            case PARAM_JWKS_URL -> getFromConfigurator(AuthConstants.ENV_JWKS_URL, x -> this.usingAws ? null : x);
            case PARAM_AWS_REGION -> getFromConfigurator(AuthConstants.ENV_JWKS_URL, x -> this.usingAws ? x.substring(
                    AuthConstants.AUTH_PREFIX_AWS.length()) : null);
            case PARAM_HEADER_NAMES -> getFromConfiguratorOrDefault(param, AuthConstants.DEFAULT_AUTH_HEADER_NAMES);
            case PARAM_HEADER_PREFIXES ->
                    getFromConfiguratorOrDefault(param, AuthConstants.DEFAULT_AUTH_HEADER_PREFIXES);
            case PARAM_USERNAME_CLAIMS -> getFromConfiguratorOrDefault(param, AuthConstants.DEFAULT_USERNAME_CLAIMS);
            // NB - ServerBuilder handles preparing the path exclusions so don't permit re-configuration of this
            case PARAM_PATH_EXCLUSIONS -> null;
            default -> getFromConfiguratorOrDefault(param, null);
        };
    }

    /**
     * Gets a value retrieved from our {@link Configurator} API, optionally transforming/filtering the configured value
     *
     * @param param     Parameter
     * @param transform A transform/filter function to apply to the raw value, may return {@code null} if the retrieved
     *                  raw value shouldn't be used
     * @return Configuration value, or {@code null} if not set/filtered by the transform function
     */
    String getFromConfigurator(String param, Function<String, String> transform) {
        String rawValue = Configurator.get(param);
        if (rawValue == null) {
            return null;
        }
        return transform.apply(rawValue);
    }

    /**
     * Gets a value retrieved from our {@link Configurator} API or the provided default value
     *
     * @param param        Parameter
     * @param defaultValue Default value to use if this parameter is not configured
     * @return Configured value
     */
    String getFromConfiguratorOrDefault(String param, String defaultValue) {
        return Configurator.get(new String[] { param }, defaultValue);
    }
}
