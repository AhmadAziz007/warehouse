package com.warehouse.dto;

import com.warehouse.model.StockMovement;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockUpdateDTO {
    @NotNull(message = "Variant ID is required")
    private Long variantId;

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    private String reason;
    private String reference;
}