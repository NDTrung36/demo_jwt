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

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = em.createQuery("SELECT u FROM User u", User.class).getResultList();
        users.forEach(u -> u.setPassword("******"));
        return ResponseEntity.ok(users);
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = em.createQuery("SELECT t FROM Task t", Task.class).getResultList();
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<String> forceUpdateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        var existingTaskOpt = taskRepository.findById(id);
        if (existingTaskOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Không tìm thấy công việc này.");
        }

        Task existingTask = existingTaskOpt.get();
        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus());

        taskRepository.update(existingTask);
        return ResponseEntity.ok("Admin đã cập nhật thành công Task ID: " + id);
    }
    public record ResetPasswordRequest(String newPassword) {}

    @PutMapping("/users/{id}/reset-password")
    public ResponseEntity<String> resetUserPassword(
            @PathVariable Long id,
            @RequestBody ResetPasswordRequest request) {
        try {
            String result = userService.resetPassword(id, request.newPassword());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
