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

    public record TokenResponse(String accessToken, String refreshToken) {}

    public String register(User user, String secretCode) {
        var existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) return "Username đã tồn tại, vui lòng chọn tên khác!";

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("VIP_ADMIN_2026".equals(secretCode) ? "ROLE_ADMIN" : "ROLE_USER");

        userRepository.save(user);
        return "Đăng ký tài khoản thành công!";
    }

    public TokenResponse login(String username, String password) {
        var userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
                String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername(), user.getRole());
                return new TokenResponse(accessToken, refreshToken);
            }
        }
        throw new RuntimeException("Sai tài khoản hoặc mật khẩu!");
    }

    public String changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản!"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "Đổi mật khẩu thành công!";
    }

    public String resetPassword(Long targetUserId, String newPassword) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản đích!"));

        targetUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(targetUser);
        return "Admin đã reset mật khẩu thành công cho User ID: " + targetUserId;
    }

    public TokenResponse refreshToken(String requestRefreshToken) {
        try {
            String username = jwtUtil.extractUsername(requestRefreshToken);

            if (jwtUtil.validateToken(requestRefreshToken, username)) {
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

                String newAccessToken = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
                String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername(), user.getRole());

                return new TokenResponse(newAccessToken, newRefreshToken);
            } else {
                throw new RuntimeException("Refresh token không hợp lệ!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Refresh token đã hết hạn hoặc bị lỗi! Vui lòng đăng nhập lại.");
        }
    }
}