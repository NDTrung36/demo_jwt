package com.trung.taskmanager.controller;

import com.trung.taskmanager.model.Task;
import com.trung.taskmanager.model.User;
import com.trung.taskmanager.repository.TaskRepository;
import com.trung.taskmanager.repository.UserRepository;
import com.trung.taskmanager.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final UserService userService;

    public AdminController(UserRepository userRepository, TaskRepository taskRepository, UserService userService) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        // Nhớ cấu hình @JsonIgnore cho thuộc tính password ở Model User nhé!
        return ResponseEntity.ok(users);
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskRepository.findAll());
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<String> forceUpdateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy công việc này."));

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus());

        taskRepository.save(existingTask);
        return ResponseEntity.ok("Admin đã cập nhật thành công Task ID: " + id);
    }

    public record ResetPasswordRequest(String newPassword) {}

    @PutMapping("/users/{id}/reset-password")
    public ResponseEntity<String> resetUserPassword(@PathVariable Long id, @RequestBody ResetPasswordRequest request) {
        try {
            String result = userService.resetPassword(id, request.newPassword());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}