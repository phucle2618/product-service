package com.example.productservice.service;

import com.example.productservice.dto.PagedResponse;
import com.example.productservice.dto.ProductDto;
import com.example.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
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
