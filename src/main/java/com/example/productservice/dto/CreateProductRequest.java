package com.example.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
   @NotBlank
   @Size(max = 64)
   private String sku;

   @NotBlank
   @Size(max = 255)
   private String name;

   private String description;

   @NotNull
   @PositiveOrZero
   private BigDecimal price;

   private String status = "ACTIVE";

   private List<String> images;

   private List<Long> categoryIds;
}
