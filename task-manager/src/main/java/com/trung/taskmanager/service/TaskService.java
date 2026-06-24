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
        taskRepository.save(task);
        return "Tạo công việc thành công!";
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

        taskRepository.save(existingTask);
        return "Cập nhật công việc thành công!";
    }

    public String deleteTask(Long taskId, UserPrincipal principal) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("NOT_FOUND"));

        if (!existingTask.getUserId().equals(principal.id())) {
            throw new RuntimeException("FORBIDDEN");
        }

        taskRepository.deleteById(taskId);
        return "Xóa công việc thành công!";
    }
}