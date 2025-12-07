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

    public ProductDto findProduct(Long id) throws SQLException {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.id, p.sku, p.name, p.description, p.price, p.status, p.created_at, p.updated_at, ");
        sql.append("GROUP_CONCAT(DISTINCT i.url) AS images, GROUP_CONCAT(DISTINCT c.name) AS categories ");
        sql.append("FROM products p ");
        sql.append("LEFT JOIN images i ON i.product_id = p.id ");
        sql.append("LEFT JOIN product_category pc ON pc.product_id = p.id ");
        sql.append("LEFT JOIN categories c ON c.id = pc.category_id ");
        sql.append("WHERE p.id = ? ");
        sql.append(" GROUP BY p.id ");
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql.toString())) {

            pst.setLong(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
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

                    return new ProductDto(id, sku, name, description, price, status, images, categories, createdAt, updatedAt);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query product", e);
        }
    }

    public ProductDto createProduct(String sku, String name, String description, java.math.BigDecimal price, String status, java.util.List<String> images, java.util.List<Long> categoryIds) throws SQLException {
        String insertSql = "INSERT INTO products (sku, name, description, price, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            try {
                conn.setAutoCommit(false);
                try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, sku);
                    ps.setString(2, name);
                    ps.setString(3, description);
                    ps.setBigDecimal(4, price);
                    ps.setString(5, status == null ? "ACTIVE" : status);
                    int affected = ps.executeUpdate();
                    if (affected == 0) throw new SQLException("Creating product failed, no rows affected.");
                    Long productId;
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) productId = rs.getLong(1);
                        else throw new SQLException("Creating product failed, no ID obtained.");
                    }

                    // insert images
                    if (images != null && !images.isEmpty()) {
                        String imgSql = "INSERT INTO images (product_id, url) VALUES (?, ?)";
                        try (PreparedStatement ips = conn.prepareStatement(imgSql)) {
                            for (String url : images) {
                                ips.setLong(1, productId);
                                ips.setString(2, url);
                                ips.addBatch();
                            }
                            ips.executeBatch();
                        }
                    }

                    // insert product_category relations
                    if (categoryIds != null && !categoryIds.isEmpty()) {
                        String pcSql = "INSERT IGNORE INTO product_category (product_id, category_id) VALUES (?, ?)";
                        try (PreparedStatement pcs = conn.prepareStatement(pcSql)) {
                            for (Long cid : categoryIds) {
                                pcs.setLong(1, productId);
                                pcs.setLong(2, cid);
                                pcs.addBatch();
                            }
                            pcs.executeBatch();
                        }
                    }

                    conn.commit();
                    return findProduct(productId);
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public ProductDto updateProduct(Long id, String sku, String name, String description, java.math.BigDecimal price, String status, java.util.List<String> images, java.util.List<Long> categoryIds) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            try {
                conn.setAutoCommit(false);

                // Build dynamic update for products table
                StringBuilder upd = new StringBuilder("UPDATE products SET ");
                java.util.List<Object> params = new java.util.ArrayList<>();
                if (sku != null) { upd.append("sku = ?, "); params.add(sku); }
                if (name != null) { upd.append("name = ?, "); params.add(name); }
                if (description != null) { upd.append("description = ?, "); params.add(description); }
                if (price != null) { upd.append("price = ?, "); params.add(price); }
                if (status != null) { upd.append("status = ?, "); params.add(status); }

                if (!params.isEmpty()) {
                    // remove trailing comma
                    int len = upd.length();
                    upd.delete(len - 2, len);
                    upd.append(" WHERE id = ?");
                    try (PreparedStatement ps = conn.prepareStatement(upd.toString())) {
                        int idx = 1;
                        for (Object p : params) ps.setObject(idx++, p);
                        ps.setLong(idx, id);
                        ps.executeUpdate();
                    }
                }

                // images: if provided, replace existing images
                if (images != null) {
                    try (PreparedStatement del = conn.prepareStatement("DELETE FROM images WHERE product_id = ?")) {
                        del.setLong(1, id);
                        del.executeUpdate();
                    }
                    if (!images.isEmpty()) {
                        String imgSql = "INSERT INTO images (product_id, url) VALUES (?, ?)";
                        try (PreparedStatement ips = conn.prepareStatement(imgSql)) {
                            for (String url : images) {
                                ips.setLong(1, id);
                                ips.setString(2, url);
                                ips.addBatch();
                            }
                            ips.executeBatch();
                        }
                    }
                }

                // categories: if provided, replace relations
                if (categoryIds != null) {
                    try (PreparedStatement del = conn.prepareStatement("DELETE FROM product_category WHERE product_id = ?")) {
                        del.setLong(1, id);
                        del.executeUpdate();
                    }
                    if (!categoryIds.isEmpty()) {
                        String pcSql = "INSERT IGNORE INTO product_category (product_id, category_id) VALUES (?, ?)";
                        try (PreparedStatement pcs = conn.prepareStatement(pcSql)) {
                            for (Long cid : categoryIds) {
                                pcs.setLong(1, id);
                                pcs.setLong(2, cid);
                                pcs.addBatch();
                            }
                            pcs.executeBatch();
                        }
                    }
                }

                conn.commit();
                return findProduct(id);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
