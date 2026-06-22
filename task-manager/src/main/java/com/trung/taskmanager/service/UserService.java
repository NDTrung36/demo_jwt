package com.trung.taskmanager.service;

import com.trung.taskmanager.model.User;
import com.trung.taskmanager.repository.UserRepository;
import com.trung.taskmanager.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil; // BỔ SUNG: Tiêm máy in vé vào đây
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Cập nhật Constructor để Spring tiêm cả 2 thứ vào
    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Logic Đăng ký (Giữ nguyên như cũ)
     */
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

    /**
     * Logic Đăng nhập NÂNG CẤP (Trả về chuỗi Token thay vì Object User)
     */
    public String login(String username, String password) {
        // 1. Tìm user trong DB
        var userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 2. So sánh mật khẩu thô và mật khẩu băm
            if (passwordEncoder.matches(password, user.getPassword())) {
                // 3. ĐĂNG NHẬP THÀNH CÔNG -> Gọi máy in vé!
                return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            }
        }

        // Nếu sai tài khoản hoặc mật khẩu, ném ra một lỗi để chặn luồng chạy
        throw new RuntimeException("Sai tài khoản hoặc mật khẩu!");
    }
}