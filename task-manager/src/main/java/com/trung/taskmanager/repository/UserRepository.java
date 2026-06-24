package com.trung.taskmanager.repository;

import com.trung.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Chỉ cần khai báo tên hàm, Spring JPA sẽ tự động viết câu lệnh SQL!
    Optional<User> findByUsername(String username);
}