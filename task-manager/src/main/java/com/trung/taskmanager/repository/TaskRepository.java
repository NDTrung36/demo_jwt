package com.trung.taskmanager.repository;

import com.trung.taskmanager.model.Task;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class TaskRepository {

    @PersistenceContext
    private EntityManager em;

    public int save(Task task) {
        try {
            em.persist(task);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public List<Task> findByUserId(Long userId) {
        var hql = "SELECT t FROM Task t WHERE t.userId = :userId";
        return em.createQuery(hql, Task.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public Optional<Task> findById(Long taskId) {
        Task task = em.find(Task.class, taskId);
        return Optional.ofNullable(task);
    }

    public int update(Task task) {
        try {
            em.merge(task);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public int delete(Long taskId) {
        try {
            Task task = em.find(Task.class, taskId);
            if (task != null) {
                em.remove(task);
                return 1;
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
