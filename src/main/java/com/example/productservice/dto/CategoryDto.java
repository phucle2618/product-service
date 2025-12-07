package com.example.productservice.dto;

public record CategoryDto(
    Long id,
    String name,
    String slug,
    String createdAt,
    String updatedAt
) {}
