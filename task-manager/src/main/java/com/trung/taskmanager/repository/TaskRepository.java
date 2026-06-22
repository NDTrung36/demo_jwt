package com.trung.taskmanager.repository;

import com.trung.taskmanager.model.Task;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TaskRepository {

    private final JdbcTemplate jdbcTemplate;

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Đã đổi tên thành taskRowMapper và sửa lại tên cột user_id
    private final RowMapper<Task> taskRowMapper = (rs, rowNum) -> {
        var task = new Task();
        task.setId(rs.getLong("id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setStatus(rs.getString("status"));
        task.setUserId(rs.getLong("user_id")); // Khớp với DB
        return task;
    };

    public int save(Task task) {
        // Sửa lại tên bảng (tasks) và tên cột (user_id)
        var sql = """
                  INSERT INTO tasks (title, description, status, user_id) 
                  VALUES (?, ?, ?, ?)
                  """;
        return jdbcTemplate.update(sql, task.getTitle(), task.getDescription(),
                task.getStatus(), task.getUserId());
    }

    // BỔ SUNG: Hàm lấy danh sách công việc của 1 User cụ thể
    public List<Task> findByUserId(Long userId) {
        var sql = """
                  SELECT id, title, description, status, user_id 
                  FROM tasks 
                  WHERE user_id = ?
                  """;
        // Dùng query() thay vì queryForObject() vì kết quả trả về có thể là nhiều dòng (List)
        return jdbcTemplate.query(sql, taskRowMapper, userId);
    }
    // 1. Tìm Task theo ID
    public Optional<Task> findById(Long taskId) {
        var sql = """
              SELECT id, title, description, status, user_id 
              FROM tasks 
              WHERE id = ?
              """;
        try {
            // Tái sử dụng taskRowMapper bạn đã định nghĩa sẵn ở trên
            Task task = jdbcTemplate.queryForObject(sql, taskRowMapper, taskId);
            return Optional.ofNullable(task);
        } catch (Exception e) {
            return Optional.empty(); // Không tìm thấy Task
        }
    }

    // 2. Cập nhật Task
    public int update(Task task) {
        var sql = """
              UPDATE tasks 
              SET title = ?, description = ?, status = ? 
              WHERE id = ?
              """;
        return jdbcTemplate.update(sql, task.getTitle(), task.getDescription(),
                task.getStatus(), task.getId());
    }

    // 3. Xóa Task
    public int delete(Long taskId) {
        var sql = """
              DELETE FROM tasks 
              WHERE id = ?
              """;
        return jdbcTemplate.update(sql, taskId);
    }
}