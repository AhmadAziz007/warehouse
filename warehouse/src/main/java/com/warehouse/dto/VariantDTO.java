package com.warehouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantDTO {
    private Long id;

    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotBlank(message = "SKU is required")
    private String sku;

    private String size;
    private String color;
    private String material;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @PositiveOrZero(message = "Stock quantity cannot be negative")
    @Builder.Default
    private Integer stockQuantity = 0;

    @NotNull
    @PositiveOrZero(message = "Minimum stock level cannot be negative")
    @Builder.Default
    private Integer minStockLevel = 0;

    // Calculated fields
    @JsonIgnore
    public Boolean getInStock() {
        return stockQuantity > 0;
    }

    @JsonIgnore
    public Boolean getNeedsRestock() {
        return stockQuantity <= minStockLevel;
    }

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}