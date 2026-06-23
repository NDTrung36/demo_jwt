package com.trung.taskmanager.controller;

import com.trung.taskmanager.security.UserPrincipal;
import com.trung.taskmanager.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Record nhận dữ liệu từ Postman
    public record ChangePasswordRequest(String oldPassword, String newPassword) {}

    /**
     * API TỰ ĐỔI MẬT KHẨU CỦA CHÍNH MÌNH: PUT /api/users/me/password
     */
    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(
            @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        // @AuthenticationPrincipal tự động lôi thông tin User đang đăng nhập từ Token ra

        try {
            String result = userService.changePassword(
                    principal.id(),
                    request.oldPassword(),
                    request.newPassword()
            );
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}