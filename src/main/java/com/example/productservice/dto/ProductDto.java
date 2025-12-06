package com.example.productservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductDto(
    Long id,
    String sku,
    String name,
    String description,
    BigDecimal price,
    String status,
    List<String> images,
    List<String> categories,
    String createdAt,
    String updatedAt
) {}
