package com.trung.taskmanager.repository;

import com.trung.taskmanager.model.RefreshToken;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public class RefreshTokenRepository {

    @PersistenceContext
    private EntityManager em;

    public Optional<RefreshToken> findByToken(String token) {
        var hql = "SELECT r FROM RefreshToken r WHERE r.token = :token";
        return em.createQuery(hql, RefreshToken.class)
                .setParameter("token", token)
                .getResultStream()
                .findFirst();
    }

    public Optional<RefreshToken> findByUserId(Long userId) {
        var hql = "SELECT r FROM RefreshToken r WHERE r.userId = :userId";
        return em.createQuery(hql, RefreshToken.class)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst();
    }

    public void save(RefreshToken refreshToken) {
        em.persist(refreshToken);
    }

    public void delete(RefreshToken refreshToken) {
        // Lệnh xóa an toàn của JPA
        em.remove(em.contains(refreshToken) ? refreshToken : em.merge(refreshToken));
    }
}