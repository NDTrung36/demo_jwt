package com.trung.taskmanager.service;

import com.trung.taskmanager.model.Task;
import com.trung.taskmanager.repository.TaskRepository;
import com.trung.taskmanager.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Tạo Task mới dựa trên thông tin User đang đăng nhập
     */
    public String createTask(Task task, UserPrincipal principal) {
        // Gán userId trực tiếp từ token đã được bóc tách trong Context, không cần truy vấn DB
        task.setUserId(principal.id());

        // Đảm bảo trạng thái mặc định nếu client không truyền lên
        if (task.getStatus() == null || task.getStatus().isBlank()) {
            task.setStatus("PENDING");
        }

        int result = taskRepository.save(task);
        if (result > 0) {
            return "Tạo công việc thành công!";
        }
        return "Tạo công việc thất bại.";
    }

    /**
     * Lấy danh sách Task của riêng User hiện tại
     */
    public List<Task> getTasksForUser(UserPrincipal principal) {
        return taskRepository.findByUserId(principal.id());
    }

    public String updateTask(Long taskId, Task updatedTask, UserPrincipal principal) {
        // Bước 1: Kiểm tra Task có tồn tại hay không
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("NOT_FOUND"));

        // Bước 2: Kiểm tra User hiện tại có phải là chủ sở hữu Task không
        if (!existingTask.getUserId().equals(principal.id())) {
            throw new RuntimeException("FORBIDDEN");
        }

        // Bước 3: Tiến hành ghi đè dữ liệu mới
        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus());

        int result = taskRepository.update(existingTask);
        if (result > 0) {
            return "Cập nhật công việc thành công!";
        }
        return "Cập nhật công việc thất bại.";
    }

    /**
     * Xóa Task (Có kiểm tra quyền sở hữu)
     */
    public String deleteTask(Long taskId, UserPrincipal principal) {
        // Bước 1: Kiểm tra Task có tồn tại hay không
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("NOT_FOUND"));

        // Bước 2: Kiểm tra quyền sở hữu
        if (!existingTask.getUserId().equals(principal.id())) {
            throw new RuntimeException("FORBIDDEN");
        }

        // Bước 3: Tiến hành xóa
        int result = taskRepository.delete(taskId);
        if (result > 0) {
            return "Xóa công việc thành công!";
        }
        return "Xóa công việc thất bại.";
    }
}