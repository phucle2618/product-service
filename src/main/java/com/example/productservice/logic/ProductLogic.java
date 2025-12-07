package com.example.productservice.logic;

import jakarta.transaction.Transactional;

import com.example.productservice.dto.CategoryDto;
import com.example.productservice.dto.CreateProductRequest;
import com.example.productservice.dto.PagedResponse;
import com.example.productservice.dto.ProductDto;
import com.example.productservice.repository.CategoryRepository;
import com.example.productservice.dto.UpdateProductRequest;
import com.example.productservice.repository.ProductRepository;
import java.sql.SQLException;

import org.springframework.stereotype.Service;

@Service
public class ProductLogic {
   private final ProductRepository productRepository;

   private final CategoryRepository categoryRepository;

   public ProductLogic(
      ProductRepository productRepository,
      CategoryRepository categoryRepository
   ) {
      this.productRepository = productRepository;
      this.categoryRepository = categoryRepository;

   }

   public PagedResponse<ProductDto> getProducts(String category, String keyword, int page, int size) {
      try {
         return productRepository.findProducts(category, keyword, page, size);
      } catch (SQLException e) {
         throw new RuntimeException("Failed to query products", e);
      }
   }

   public ProductDto getProductById(Long id) {
      try {
         return productRepository.findProduct(id);
      } catch (SQLException e) {
         throw new RuntimeException("Failed to query product", e);
      }
   }
   public CategoryDto createCategory(String name, String slug) {
      try {
         return categoryRepository.createCategory(name, slug);
      } catch (SQLException e) {
         throw new RuntimeException("Failed to create category", e);
      }
   }

   public ProductDto createProduct(CreateProductRequest req) {
      try {
         return productRepository.createProduct(
            req.getSku(),
            req.getName(),
            req.getDescription(),
            req.getPrice(),
            req.getStatus(),
            req.getImages(),
            req.getCategoryIds()
         );
      } catch (SQLException e) {
         throw new RuntimeException("Failed to create product", e);
      }
   }

   public CategoryDto updateCategory(Long id, String name, String slug) {
      try {
         return categoryRepository.updateCategory(id, name, slug);
      } catch (SQLException e) {
         throw new RuntimeException("Failed to update category", e);
      }
   }

   public ProductDto updateProduct(UpdateProductRequest req) {
      try {
         return productRepository.updateProduct(
            req.getId(),
            req.getSku(),
            req.getName(),
            req.getDescription(),
            req.getPrice(),
            req.getStatus(),
            req.getImages(),
            req.getCategoryIds()
         );
      } catch (SQLException e) {
         throw new RuntimeException("Failed to update product", e);
      }
   }
}
