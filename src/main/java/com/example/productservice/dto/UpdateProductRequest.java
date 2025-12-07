package com.example.productservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductRequest {

    @NotNull
    private Long id;

    @Size(max = 64)
    private String sku;

    @Size(max = 255)
    private String name;

    private String description;

    @PositiveOrZero
    private BigDecimal price;

    private String status;

    private List<String> images;

    private List<Long> categoryIds;
}
