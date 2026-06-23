package com.trung.taskmanager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity // Báo cho Hibernate biết đây là một thực thể cần map xuống DB
@Table(name = "users") // Chỉ định tên bảng dưới Database
@Getter
@Setter
@NoArgsConstructor // Lombok tự sinh Constructor rỗng (Bắt buộc phải có cho JPA)
@AllArgsConstructor // Lombok tự sinh Constructor full tham số
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tương đương AUTO_INCREMENT
    private Long id;

    @Column(nullable = false, unique = true) // Ràng buộc: Không được null, không được trùng
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;
}