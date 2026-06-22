package com.trung.taskmanager.repository;

import com.trung.taskmanager.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    // Spring tự động tiêm (inject) JdbcTemplate vào đây
    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper: "Dịch" 1 dòng trong ResultSet (MySQL) thành 1 Object User trên vùng nhớ Heap
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        var user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        return user;
    };

    public Optional<User> findByUsername(String username) {
        // Sử dụng Text Blocks (""") của Modern Java để viết SQL dễ nhìn
        var sql = """
                  SELECT id, username, password, role 
                  FROM users 
                  WHERE username = ?
                  """;

        try {
            // Dấu '?' giúp chống lại bẫy phỏng vấn: Lỗ hổng SQL Injection
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, username);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty(); // Không tìm thấy
        }
    }

    public int save(User user) {
        var sql = """
                  INSERT INTO users (username, password, role) 
                  VALUES (?, ?, ?)
                  """;
        return jdbcTemplate.update(sql, user.getUsername(), user.getPassword(), user.getRole());
    }
}