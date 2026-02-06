package org.fireflyframework.common.application.spi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO representing a product within a contract.
 *
 * @author Firefly Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfoDTO {

    private UUID productId;
    private String productName;
}
