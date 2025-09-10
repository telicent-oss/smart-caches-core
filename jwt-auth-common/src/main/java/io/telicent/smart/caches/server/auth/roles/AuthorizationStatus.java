package io.telicent.smart.caches.server.auth.roles;

/**
 * Possible authorization statuses
 */
public enum AuthorizationStatus {
    /**
     * Authorization not applicable for the requested resource
     */
    NOT_APPLICABLE,
    /**
     * User allowed access to requested resource
     */
    ALLOWED,
    /**
     * User denied access to request resource
     */
    DENIED;
}
