package com.trung.taskmanager.controller;

import com.trung.taskmanager.model.Task;
import com.trung.taskmanager.security.UserPrincipal;
import com.trung.taskmanager.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    //POST /api/tasks
    @PostMapping
    public ResponseEntity<String> createTask(@RequestBody Task task) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        String result = taskService.createTask(task, principal);

        if (result.contains("thành công")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @GetMapping
    public ResponseEntity<List<Task>> getTasks() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        List<Task> tasks = taskService.getTasksForUser(principal);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateTask(@PathVariable Long id, @RequestBody Task task) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        try {
            String result = taskService.updateTask(id, task, principal);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            if ("NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(404).body("Không tìm thấy công việc này.");
            } else if ("FORBIDDEN".equals(e.getMessage())) {
                return ResponseEntity.status(403).body("Bạn không có quyền chỉnh sửa công việc này!");
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE /api/tasks/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        try {
            String result = taskService.deleteTask(id, principal);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            if ("NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(404).body("Không tìm thấy công việc này.");
            } else if ("FORBIDDEN".equals(e.getMessage())) {
                return ResponseEntity.status(403).body("Bạn không có quyền xóa công việc này!");
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}