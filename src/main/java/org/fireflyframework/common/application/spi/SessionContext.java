package org.fireflyframework.common.application.spi;

import org.fireflyframework.common.application.spi.dto.ContractInfoDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a user session context.
 * <p>
 * Carries session metadata, user roles, scopes, contracts, and any domain-specific context.
 * Platform-specific implementations can extend this class to add additional fields.
 * </p>
 *
 * @author Firefly Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionContext {

    private String sessionId;
    private UUID partyId;
    private String userId;
    private String tenantId;
    private List<String> roles;
    private List<String> scopes;
    private Map<String, Object> attributes;
    private List<ContractInfoDTO> activeContracts;
    private LocalDateTime createdAt;
    private SessionStatus status;

    /**
     * Session lifecycle status.
     */
    public enum SessionStatus {
        ACTIVE, EXPIRED, INVALIDATED
    }
}
