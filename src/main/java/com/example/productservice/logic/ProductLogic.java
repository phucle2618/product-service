package com.example.productservice.logic;

import jakarta.transaction.Transactional;
import com.example.productservice.dto.PagedResponse;
import com.example.productservice.dto.ProductDto;
import com.example.productservice.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import org.springframework.stereotype.Service;

@Service
public class ProductLogic {
   private final ProductRepository repository;

   public ProductLogic(ProductRepository repository) {
      this.repository = repository;
   }

   public PagedResponse<ProductDto> getProducts(String category, String keyword, int page, int size) {
      try {
         return repository.findProducts(category, keyword, page, size);
      } catch (SQLException e) {
         throw new RuntimeException("Failed to query products", e);
      }
   }
}
