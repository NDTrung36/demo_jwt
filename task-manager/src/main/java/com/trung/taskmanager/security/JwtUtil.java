package com.trung.taskmanager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component // Để Spring quản lý và cho phép tiêm vào các class khác khi cần
public class JwtUtil {

    // Chuỗi bí mật dùng để ký tên (Bắt buộc phải dài trên 32 ký tự)
    private final String SECRET_STRING = "Tr0ngCaiKhoLoCaiKhonCuaTrungIT2026!!!";

    // Thời gian sống của Token: 1 giờ (tính bằng miligiây)
    private final long EXPIRATION_TIME = 3600000;

    // Chuyển chuỗi String thành SecretKey chuẩn thuật toán HMAC-SHA256
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * HÀM 1: TẠO TOKEN (IN VÉ)
     * Nhận vào username và role để đóng gói vào Payload
     */
    public String generateToken(long userId, String username, String role) {
        var now = new Date();
        var expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)// Đặt Sub (chủ thể) là username
                .claim("role", role)                // Thêm dữ liệu tùy chỉnh (ví dụ: ROLE_USER)
                .issuedAt(now)                      // Thời gian phát hành
                .expiration(expiryDate)             // Thời gian hết hạn
                .signWith(getSigningKey())          // Ký tên bằng Khóa bí mật
                .compact();                         // Nén lại thành chuỗi chuỗi chữ và số ngăn cách bởi 2 dấu chấm
    }

    /**
     * HÀM 2: GIẢI MÃ VÀ TRÍCH XUẤT DỮ LIỆU (ĐỌC VÉ)
     * Thò tay vào Payload để lấy ra toàn bộ thông tin (Claims)
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())        // Đưa khóa bí mật vào để máy quét đối chiếu chữ ký
                .build()
                .parseSignedClaims(token)           // Nếu chữ ký giả mạo hoặc hết hạn, hàm này sẽ ném lỗi sập tại đây
                .getPayload();                         // Nếu hợp lệ, trả về phần ruột (Payload)
    }

    /**
     * HÀM 3: LẤY USERNAME TỪ TOKEN
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * HÀM 4: LẤY ROLE TỪ TOKEN
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // Bổ sung HÀM 4.5: Trích xuất userId từ Token
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    /**
     * HÀM 5: KIỂM TRA TOKEN CÒN HẠN KHÔNG
     */
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * HÀM 6: XÁC MINH TOKEN HỢP LỆ
     * Token được coi là đúng nếu cấu trúc chuẩn, chữ ký xịn và chưa hết hạn
     */
    public boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            return false; // Bất kỳ lỗi gì xảy ra (sai chữ ký, token rác) đều coi là không hợp lệ
        }
    }
}