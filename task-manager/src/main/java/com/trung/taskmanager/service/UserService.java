package com.trung.taskmanager.service;

import com.trung.taskmanager.model.RefreshToken;
import com.trung.taskmanager.model.User;
import com.trung.taskmanager.repository.UserRepository;
import com.trung.taskmanager.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    public record TokenResponse(String accessToken, String refreshToken) {}

    public String register(User user, String secretCode) {
        var existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            return "Username đã tồn tại, vui lòng chọn tên khác!";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if ("VIP_ADMIN_2026".equals(secretCode)) {
            user.setRole("ROLE_ADMIN");
        } else {
            user.setRole("ROLE_USER");
        }

        int result = userRepository.save(user);
        if (result > 0) {
            return "Đăng ký tài khoản thành công!";
        }
        return "Đăng ký thất bại, lỗi hệ thống.";
    }

    public TokenResponse login(String username, String password) {
        var userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                // 1. In vé đi cổng (Access Token)
                String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
                // 2. In vé dự phòng (Refresh Token)
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

                return new TokenResponse(accessToken, refreshToken.getToken());
            }
        }
        throw new RuntimeException("Sai tài khoản hoặc mật khẩu!");
    }
    /**
     * Logic cho USER: Tự đổi mật khẩu (Bắt buộc phải check pass cũ)
     */
    public String changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản!"));

        // 1. Kiểm tra mật khẩu cũ có khớp không?
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác!");
        }

        // 2. Băm mật khẩu mới và lưu lại
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.update(user);

        return "Đổi mật khẩu thành công!";
    }

    /**
     * Logic cho ADMIN: Reset mật khẩu của người khác (Quyền lực tuyệt đối)
     */
    public String resetPassword(Long targetUserId, String newPassword) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản đích!"));

        // Admin ra lệnh là đổi, không cần hỏi mật khẩu cũ
        targetUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.update(targetUser);

        return "Admin đã reset mật khẩu thành công cho User ID: " + targetUserId;
    }
    // THÊM HÀM MỚI: XỬ LÝ KHI CLIENT CẦM REFRESH TOKEN LÊN XIN ACCESS TOKEN MỚI
    public TokenResponse refreshToken(String requestRefreshToken) {
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration) // Kiểm tra hạn
                .map(refreshToken -> {
                    // Nếu thẻ còn xịn, chui vào DB lấy thông tin User để in Access Token mới
                    User user = userRepository.findById(refreshToken.getUserId())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

                    String newAccessToken = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
                    return new TokenResponse(newAccessToken, refreshToken.getToken());
                })
                .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại trong hệ thống!"));
    }
}