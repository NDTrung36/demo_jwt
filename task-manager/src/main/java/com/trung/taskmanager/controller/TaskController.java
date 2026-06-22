package com.trung.taskmanager.controller;

import com.trung.taskmanager.model.Task;
import com.trung.taskmanager.security.UserPrincipal;
import com.trung.taskmanager.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks") // Tất cả các API trong đây sẽ có tiền tố /api/tasks
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * API TẠO TASK MỚI: POST /api/tasks
     */
    @PostMapping
    public ResponseEntity<String> createTask(@RequestBody Task task) {
        // Hướng 1A: Lấy thủ công thông tin UserPrincipal từ SecurityContextHolder
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        // Gọi Service xử lý tạo Task
        String result = taskService.createTask(task, principal);

        if (result.contains("thành công")) {
            return ResponseEntity.ok(result); // Trả về HTTP 200 OK
        }
        return ResponseEntity.badRequest().body(result); // Trả về HTTP 400
    }

    /**
     * API LẤY DANH SÁCH TASK CỦA USER ĐANG ĐĂNG NHẬP: GET /api/tasks
     */
    @GetMapping
    public ResponseEntity<List<Task>> getTasks() {
        // Hướng 1A: Lấy thủ công thông tin UserPrincipal từ SecurityContextHolder
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        // Gọi Service lấy danh sách Task
        List<Task> tasks = taskService.getTasksForUser(principal);
        return ResponseEntity.ok(tasks); // Trả về HTTP 200 kèm danh sách Task dạng JSON
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateTask(@PathVariable Long id, @RequestBody Task task) {
        // Lấy thông tin User theo hướng 1A
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        try {
            String result = taskService.updateTask(id, task, principal);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            // Xử lý ném lỗi tương ứng dựa trên thông báo từ Service
            if ("NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(404).body("Không tìm thấy công việc này.");
            } else if ("FORBIDDEN".equals(e.getMessage())) {
                return ResponseEntity.status(403).body("Bạn không có quyền chỉnh sửa công việc này!");
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API XÓA TASK: DELETE /api/tasks/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        // Lấy thông tin User theo hướng 1A
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        try {
            String result = taskService.deleteTask(id, principal);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            // Xử lý ném lỗi tương ứng
            if ("NOT_FOUND".equals(e.getMessage())) {
                return ResponseEntity.status(404).body("Không tìm thấy công việc này.");
            } else if ("FORBIDDEN".equals(e.getMessage())) {
                return ResponseEntity.status(403).body("Bạn không có quyền xóa công việc này!");
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}