package io.telicent.smart.caches.server.auth.roles;

import java.util.List;

/**
 * Represents authorization engine results
 *
 * @param status  Authorization status
 * @param reasons Reasons for this status, in the case of {@link AuthorizationStatus#DENIED} this should be an
 *                explanation of why access has been denied
 */
public record AuthorizationResult(AuthorizationStatus status, List<String> reasons) {

    public AuthorizationResult(AuthorizationStatus status, String reason) {
        this(status, List.of(reason));
    }
}
