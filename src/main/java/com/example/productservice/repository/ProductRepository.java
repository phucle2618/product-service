package com.example.productservice.repository;

import com.example.productservice.dto.ProductDto;
import com.example.productservice.dto.PagedResponse;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.math.BigDecimal;

@Repository
public class ProductRepository {

    private final DataSource dataSource;

    public ProductRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public PagedResponse<ProductDto> findProducts(String categorySlug, String keyword, int page, int size) throws SQLException {
        int offset = page * size;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.id, p.sku, p.name, p.description, p.price, p.status, p.created_at, p.updated_at, ");
        sql.append("GROUP_CONCAT(DISTINCT i.url) AS images, GROUP_CONCAT(DISTINCT c.name) AS categories ");
        sql.append("FROM products p ");
        sql.append("LEFT JOIN images i ON i.product_id = p.id ");
        sql.append("LEFT JOIN product_category pc ON pc.product_id = p.id ");
        sql.append("LEFT JOIN categories c ON c.id = pc.category_id ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (p.name LIKE ? OR p.description LIKE ? OR p.sku LIKE ?) ");
            String like = "%" + keyword + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (categorySlug != null && !categorySlug.isEmpty()) {
            sql.append(" AND c.slug = ? ");
            params.add(categorySlug);
        }

        sql.append(" GROUP BY p.id ORDER BY p.created_at DESC LIMIT ? OFFSET ?");

        // Query for data
        List<ProductDto> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            for (Object p : params) {
                pst.setObject(idx++, p);
            }
            pst.setInt(idx++, size);
            pst.setInt(idx, offset);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Long id = rs.getLong("id");
                    String sku = rs.getString("sku");
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    BigDecimal price = rs.getBigDecimal("price");
                    String status = rs.getString("status");
                    String imagesCsv = rs.getString("images");
                    String categoriesCsv = rs.getString("categories");
                    List<String> images = imagesCsv == null ? List.of() : Arrays.asList(imagesCsv.split(","));
                    List<String> categories = categoriesCsv == null ? List.of() : Arrays.asList(categoriesCsv.split(","));
                    Timestamp created = rs.getTimestamp("created_at");
                    Timestamp updated = rs.getTimestamp("updated_at");

                    String createdAt = created == null ? null : created.toString();
                    String updatedAt = updated == null ? null : updated.toString();

                    ProductDto dto = new ProductDto(id, sku, name, description, price, status, images, categories, createdAt, updatedAt);
                    results.add(dto);
                }
            }
        }

        // Query for total count
        StringBuilder countSql = new StringBuilder();
        countSql.append("SELECT COUNT(DISTINCT p.id) AS total FROM products p ");
        countSql.append("LEFT JOIN product_category pc ON pc.product_id = p.id ");
        countSql.append("LEFT JOIN categories c ON c.id = pc.category_id ");
        countSql.append("WHERE 1=1 ");
        if (keyword != null && !keyword.isEmpty()) {
            countSql.append(" AND (p.name LIKE ? OR p.description LIKE ? OR p.sku LIKE ?) ");
        }
        if (categorySlug != null && !categorySlug.isEmpty()) {
            countSql.append(" AND c.slug = ? ");
        }

        long total = 0L;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pst = conn.prepareStatement(countSql.toString())) {
            int idx = 1;
            if (keyword != null && !keyword.isEmpty()) {
                String like = "%" + keyword + "%";
                pst.setString(idx++, like);
                pst.setString(idx++, like);
                pst.setString(idx++, like);
            }
            if (categorySlug != null && !categorySlug.isEmpty()) {
                pst.setString(idx, categorySlug);
            }
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    total = rs.getLong("total");
                }
            }
        }

        return new PagedResponse<>(results, total, page, size);
    }
}
