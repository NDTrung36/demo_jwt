package com.trung.taskmanager.repository;

import com.trung.taskmanager.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public class UserRepository {

    @PersistenceContext
    private EntityManager em;

    public Optional<User> findByUsername(String username) {
        var hql = "SELECT u FROM User u WHERE u.username = :username";

        return em.createQuery(hql, User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    public int save(User user) {
        try {
            em.persist(user);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
    public Optional<User> findById(Long id) {
        User user = em.find(User.class, id);
        return Optional.ofNullable(user);
    }

    public int update(User user) {
        try {
            em.merge(user);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
}
