package org.fireflyframework.common.application.spi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO representing a contract within a user session.
 *
 * @author Firefly Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractInfoDTO {

    private UUID contractId;
    private String contractNumber;
    private RoleInfoDTO roleInContract;
    private ProductInfoDTO product;
    private boolean isActive;
}
