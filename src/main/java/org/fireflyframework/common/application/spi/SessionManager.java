package org.fireflyframework.common.application.spi;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * SPI interface for session management.
 * <p>
 * Implementations of this interface provide the mechanism for retrieving and managing
 * user session contexts. Platform-specific implementations (e.g., for banking, e-commerce)
 * should implement this interface to integrate with their identity/session infrastructure.
 * </p>
 */
public interface SessionManager<T> {

    /**
     * Retrieves the current session context for the given token.
     *
     * @param token the authentication token
     * @return a Mono emitting the session context
     */
    Mono<T> getSessionContext(String token);

    /**
     * Creates or retrieves the session associated with the current web exchange.
     * <p>
     * Extracts the authentication token from the exchange and returns the enriched
     * session context. If no active session exists, a new one may be created.
     * </p>
     *
     * @param exchange the current server web exchange
     * @return a Mono emitting the session context
     */
    Mono<T> createOrGetSession(ServerWebExchange exchange);

    /**
     * Validates whether the given token represents a valid session.
     *
     * @param token the authentication token
     * @return a Mono emitting true if the session is valid
     */
    Mono<Boolean> isSessionValid(String token);

    /**
     * Checks whether the given party has access to the specified product.
     *
     * @param partyId   the party identifier
     * @param productId the product identifier
     * @return a Mono emitting true if the party has access to the product
     */
    Mono<Boolean> hasAccessToProduct(UUID partyId, UUID productId);

    /**
     * Checks whether the given party has a specific permission on a product.
     *
     * @param partyId      the party identifier
     * @param productId    the product identifier
     * @param actionType   the action type (e.g., READ, WRITE, DELETE)
     * @param resourceType the resource type (e.g., BALANCE, TRANSACTION)
     * @return a Mono emitting true if the party has the permission
     */
    Mono<Boolean> hasPermission(UUID partyId, UUID productId, String actionType, String resourceType);

    /**
     * Invalidates the session associated with the given token.
     *
     * @param token the authentication token
     * @return a Mono completing when the session is invalidated
     */
    Mono<Void> invalidateSession(String token);
}
