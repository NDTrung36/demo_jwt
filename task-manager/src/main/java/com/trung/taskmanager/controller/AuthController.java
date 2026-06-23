package com.trung.taskmanager.controller;

import com.trung.taskmanager.model.User;
import com.trung.taskmanager.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    public record AuthRequest(String username, String password, String secretCode) {}

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(request.password());

        String result = userService.register(user, request.secretCode());

        if (result.contains("thành công")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    // ... code cũ của AuthController ...

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            // Giờ đây hàm login trả về 1 object chứa cả 2 thẻ
            UserService.TokenResponse tokens = userService.login(request.username(), request.password());
            return ResponseEntity.ok(tokens);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // Record hứng dữ liệu khi xin cấp lại vé
    public record RefreshTokenRequest(String refreshToken) {}

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            UserService.TokenResponse tokens = userService.refreshToken(request.refreshToken());
            return ResponseEntity.ok(tokens);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage()); // 403 Forbidden
        }
    }
}