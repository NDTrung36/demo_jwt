package com.trung.taskmanager.controller;

import com.trung.taskmanager.model.User;
import com.trung.taskmanager.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // Khai báo tiền tố cho tất cả các API trong class này
public class AuthController {

    private final UserService userService;

    // Tiêm UserService vào Controller
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Dùng Java Record làm DTO hứng dữ liệu JSON từ Client.
     * Record tự động tạo constructor, getter, equals, hashCode (Immutable data).
     */
    public record AuthRequest(String username, String password) {}

    /**
     * API ĐĂNG KÝ: POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest request) {
        // Chuyển đổi dữ liệu từ Record DTO sang Model User
        User user = new User();
        user.setUsername(request.username()); // Gọi hàm username() của Record thay vì getUsername()
        user.setPassword(request.password());

        String result = userService.register(user);

        // Trả về HTTP Status Code tương ứng
        if (result.contains("thành công")) {
            return ResponseEntity.ok(result); // Trả về HTTP 200 OK
        }
        return ResponseEntity.badRequest().body(result); // Trả về HTTP 400 Bad Request
    }

    /**
     * API ĐĂNG NHẬP: POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest request) {
        try {
            // Đưa dữ liệu xuống Service, nếu đúng sẽ nhận về chuỗi JWT
            String token = userService.login(request.username(), request.password());
            return ResponseEntity.ok(token); // HTTP 200 OK kèm theo Token
        } catch (RuntimeException e) {
            // Bắt cái lỗi quăng ra từ Service nếu sai pass, trả về lỗi 401
            return ResponseEntity.status(401).body(e.getMessage()); // HTTP 401 Unauthorized
        }
    }
}