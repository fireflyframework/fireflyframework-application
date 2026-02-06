package org.fireflyframework.common.application.spi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO representing a role within a contract.
 *
 * @author Firefly Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleInfoDTO {

    private UUID roleId;
    private String roleCode;
    private String name;
    private boolean isActive;
    private List<RoleScopeInfoDTO> scopes;
}
