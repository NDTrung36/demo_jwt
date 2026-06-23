package com.trung.taskmanager.repository;

import com.trung.taskmanager.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional // BẮT BUỘC: Đánh dấu class này cần mở một giao dịch (Transaction) với Database
public class UserRepository {

    // Tiêm "Cỗ máy" quản lý thực thể của Hibernate vào đây
    @PersistenceContext
    private EntityManager em;

    public Optional<User> findByUsername(String username) {
        // Đây là HQL (Hibernate Query Language) - Ta truy vấn trên Class User, không phải bảng 'users'
        var hql = "SELECT u FROM User u WHERE u.username = :username";

        return em.createQuery(hql, User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    public int save(User user) {
        try {
            // persist = Ra lệnh cho Hibernate theo dõi và tự sinh lệnh INSERT INTO...
            em.persist(user);
            return 1; // Thành công trả về 1
        } catch (Exception e) {
            return 0; // Thất bại trả về 0
        }
    }
    // 1. Tìm User theo ID
    public Optional<User> findById(Long id) {
        User user = em.find(User.class, id);
        return Optional.ofNullable(user);
    }

    // 2. Cập nhật User (Hibernate sẽ tự động so sánh và sinh lệnh UPDATE)
    public int update(User user) {
        try {
            em.merge(user);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
}