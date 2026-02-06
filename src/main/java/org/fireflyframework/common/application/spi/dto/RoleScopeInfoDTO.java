package org.fireflyframework.common.application.spi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO representing a role scope (permission) within a role.
 *
 * @author Firefly Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleScopeInfoDTO {

    private UUID scopeId;
    private String actionType;
    private String resourceType;
    private boolean isActive;
}
