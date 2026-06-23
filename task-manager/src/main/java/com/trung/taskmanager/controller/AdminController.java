package com.trung.taskmanager.controller;

import com.trung.taskmanager.model.Task;
import com.trung.taskmanager.model.User;
import com.trung.taskmanager.repository.TaskRepository;
import com.trung.taskmanager.service.UserService;
import jakarta.persistence.EntityManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final EntityManager em;
    private final TaskRepository taskRepository;
    private final UserService userService;

    public AdminController(EntityManager em, TaskRepository taskRepository, UserService userService) {
        this.em = em;
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    // 1. Xem danh sách toàn bộ User
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = em.createQuery("SELECT u FROM User u", User.class).getResultList();
        // Giấu password hash trước khi trả về Postman
        users.forEach(u -> u.setPassword("******"));
        return ResponseEntity.ok(users);
    }

    // 2. Xem danh sách toàn bộ Task
    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = em.createQuery("SELECT t FROM Task t", Task.class).getResultList();
        return ResponseEntity.ok(tasks);
    }

    // 3. Quyền tối cao: Update Task của bất kỳ ai
    @PutMapping("/tasks/{id}")
    public ResponseEntity<String> forceUpdateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        var existingTaskOpt = taskRepository.findById(id);
        if (existingTaskOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Không tìm thấy công việc này.");
        }

        Task existingTask = existingTaskOpt.get();
        // Ghi đè dữ liệu mà không cần kiểm tra userId (Quyền Admin)
        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus());

        taskRepository.update(existingTask);
        return ResponseEntity.ok("Admin đã cập nhật thành công Task ID: " + id);
    }
    // Record nhận dữ liệu từ Admin (Chỉ cần password mới)
    public record ResetPasswordRequest(String newPassword) {}

    /**
     * 4. QUYỀN TỐI CAO: Reset mật khẩu user bất kỳ: PUT /api/admin/users/{id}/reset-password
     */
    @PutMapping("/users/{id}/reset-password")
    public ResponseEntity<String> resetUserPassword(
            @PathVariable Long id,
            @RequestBody ResetPasswordRequest request) {
        try {
            // Chỉ cần gọi service, không cần quan tâm Admin này là ai
            String result = userService.resetPassword(id, request.newPassword());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}