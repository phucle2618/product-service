package com.example.productservice.repository;

import com.example.productservice.dto.CategoryDto;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

@Repository
public class CategoryRepository {

    private final DataSource dataSource;

    public CategoryRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public CategoryDto createCategory(String name, String slug) throws SQLException {
        String sql = "INSERT INTO categories (name, slug) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, slug);
            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("Creating category failed, no rows affected.");
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    // fetch created_at/updated_at
                    return findById(id);
                } else {
                    throw new SQLException("Creating category failed, no ID obtained.");
                }
            }
        }
    }

    public CategoryDto findById(Long id) throws SQLException {
        String sql = "SELECT id, name, slug, created_at, updated_at FROM categories WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String createdAt = rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toString();
                    String updatedAt = rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toString();
                    return new CategoryDto(id, rs.getString("name"), rs.getString("slug"), createdAt, updatedAt);
                }
            }
        }
        return null;
    }

    public CategoryDto updateCategory(Long id, String name, String slug) throws SQLException {
        String sql = "UPDATE categories SET name = ?, slug = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, slug);
            ps.setLong(3, id);
            int affected = ps.executeUpdate();
            if (affected == 0) return null;
            return findById(id);
        }
    }
}
