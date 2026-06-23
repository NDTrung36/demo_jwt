package com.trung.taskmanager.service;

import com.trung.taskmanager.model.RefreshToken;
import com.trung.taskmanager.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    // Hạn sử dụng: 7 ngày (tính bằng milliseconds)
    private final long REFRESH_EXPIRATION = 604800000L;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // 1. IN THẺ MỚI
    public RefreshToken createRefreshToken(Long userId) {
        // Xóa thẻ cũ đi (Chính sách 1 tài khoản chỉ dùng trên 1 thiết bị tại 1 thời điểm)
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(userId);
        existingToken.ifPresent(refreshTokenRepository::delete);

        // Tạo thẻ mới
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(UUID.randomUUID().toString()); // Sinh mã ngẫu nhiên: ví dụ "f47ac10b-58cc-..."
        refreshToken.setExpiryDate(Instant.now().plusMillis(REFRESH_EXPIRATION));

        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    // 2. KIỂM TRA HẠN SỬ DỤNG
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token); // Hết hạn thì xóa luôn khỏi DB
            throw new RuntimeException("Refresh token đã hết hạn. Vui lòng đăng nhập lại!");
        }
        return token;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
}