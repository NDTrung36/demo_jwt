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

    public String createTask(Task task, UserPrincipal principal) {
        task.setUserId(principal.id());

        if (task.getStatus() == null || task.getStatus().isBlank()) {
            task.setStatus("PENDING");
        }

        int result = taskRepository.save(task);
        if (result > 0) {
            return "Tạo công việc thành công!";
        }
        return "Tạo công việc thất bại.";
    }

    public List<Task> getTasksForUser(UserPrincipal principal) {
        return taskRepository.findByUserId(principal.id());
    }

    public String updateTask(Long taskId, Task updatedTask, UserPrincipal principal) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("NOT_FOUND"));

        if (!existingTask.getUserId().equals(principal.id())) {
            throw new RuntimeException("FORBIDDEN");
        }

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus());

        int result = taskRepository.update(existingTask);
        if (result > 0) {
            return "Cập nhật công việc thành công!";
        }
        return "Cập nhật công việc thất bại.";
    }

    public String deleteTask(Long taskId, UserPrincipal principal) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("NOT_FOUND"));

        if (!existingTask.getUserId().equals(principal.id())) {
            throw new RuntimeException("FORBIDDEN");
        }

        int result = taskRepository.delete(taskId);
        if (result > 0) {
            return "Xóa công việc thành công!";
        }
        return "Xóa công việc thất bại.";
    }
}
