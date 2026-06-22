package com.trung.taskmanager.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // Tiêm máy quét vé JwtUtil vào trạm kiểm soát
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Thò tay vào HTTP Header để lấy chuỗi Authorization
        final String authHeader = request.getHeader("Authorization");

        // 2. Kiểm tra: Nếu không có Header này, hoặc không bắt đầu bằng chữ "Bearer " -> Bỏ qua, không quét
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Cho đi tiếp (lát nữa SecurityConfig sẽ tự động đấm văng ra vì không có thẻ)
            return;
        }

        // 3. Cắt bỏ 7 ký tự đầu ("Bearer ") để lấy phần token nguyên chất
        final String jwt = authHeader.substring(7);

        try {
            // 4. Đưa vào máy quét để bóc tách tên đăng nhập
            final String username = jwtUtil.extractUsername(jwt);

            // 5. Kiểm tra: Có tên trên thẻ VÀ hiện tại luồng này chưa được cấp quyền (chưa ai đăng nhập)
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. Máy quét soi chữ ký và kiểm tra hạn sử dụng của vé
                if (jwtUtil.validateToken(jwt, username)) {

                    // Lấy quyền (role) ra từ vé để cấp cho Spring Security
                    String role = jwtUtil.extractRole(jwt);
                    Long userId = jwtUtil.extractUserId(jwt);

                    UserPrincipal principal = new UserPrincipal(userId, username);

                    // 7. Cấp "Thẻ thông hành nội bộ" cho Spring Security
                    var authToken = new UsernamePasswordAuthenticationToken(
                            principal,
                            null, // Không cần mật khẩu nữa vì đã quét bằng JWT
                            Collections.singletonList(new SimpleGrantedAuthority(role))
                    );

                    // 8. Gắn thẻ thông hành này vào SecurityContext (Bộ nhớ cục bộ của luồng hiện tại)
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Nếu token hết hạn hoặc bị làm giả, hàm extractUsername/validateToken sẽ ném lỗi.
            // Ta bắt lỗi ở đây và im lặng không làm gì cả. Request này sẽ không được cấp thẻ thông hành.
        }

        // 9. Tiếp tục đẩy Request đi sang các trạm tiếp theo hoặc Controller
        filterChain.doFilter(request, response);
    }
}