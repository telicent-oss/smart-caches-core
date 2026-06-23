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
package io.telicent.smart.cache.distribution.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static io.telicent.smart.cache.distribution.util.HexGenerator.sha256Hex;

/**
 * Utility class for generating document IDs.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DocumentIdGenerator {

    public static final String MISSING_DISTRIBUTION_ID_ERROR =
            "Rejected event because ROUTE_TO_NAMED_GRAPHS=true and the required Distribution-Id header was missing";
    private static final String DISTRIBUTION_ID_COMPONENT = "|distributionId=";
    private static final String DISTRIBUTION_SCOPED_ID_PREFIX = "distribution-sha256:";

    /**
     * When configured, generates a deterministic unique ID by combining the entity URI and distribution ID.
     * @param entityUri the entity URI
     * @param distributionId the distribution ID
     * @param enabledCombined true when combined ID is required
     * @return the combined ID when enabled otherwise the entity URI
     */
    public static String generateDocumentId(String entityUri, String distributionId, boolean enabledCombined) {
        if (!enabledCombined) {
            return entityUri;
        }
        if (StringUtils.isBlank(distributionId)) {
            throw new IllegalStateException(MISSING_DISTRIBUTION_ID_ERROR);
        }
        final String combinedIdentifiers = entityUri + DISTRIBUTION_ID_COMPONENT
                + URLEncoder.encode(distributionId, StandardCharsets.UTF_8);
        return DISTRIBUTION_SCOPED_ID_PREFIX + sha256Hex(combinedIdentifiers);
    }

}
