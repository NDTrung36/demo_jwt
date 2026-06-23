package com.trung.taskmanager.service;

import com.trung.taskmanager.model.User;
import com.trung.taskmanager.repository.UserRepository;
import com.trung.taskmanager.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public String register(User user) {
        var existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            return "Username đã tồn tại, vui lòng chọn tên khác!";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");

        int result = userRepository.save(user);
        if (result > 0) {
            return "Đăng ký tài khoản thành công!";
        }
        return "Đăng ký thất bại, lỗi hệ thống.";
    }

    public String login(String username, String password) {
        var userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            }
        }

        throw new RuntimeException("Sai tài khoản hoặc mật khẩu!");
    }
}