package com.snowball.security;

import com.snowball.entity.User;
import com.snowball.repository.UserRepository; // ✅ 引入 UserRepository (根据你的实际包名调整)
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository; // ✅ 注入 UserRepository

    // ✅ 修改构造器，注入 UserRepository
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            try {
                Long userId = jwtUtil.getUserIdFromToken(token);
                User user = userRepository.findById(userId).orElse(null);

                if (user != null) {
                    String roleName = "ROLE_" + user.getRole().name();
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null,
                                    List.of(new SimpleGrantedAuthority(roleName)));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
                // Token valid but user not found — just clear context, don't block.
                // Spring Security will enforce authentication for non-permitAll paths.
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                // Don't block — let Spring Security decide based on path rules.
                // permitAll() paths work; authenticated() paths get 401 from entry point.
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
