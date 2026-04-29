package com.example.snowball.security;

import com.example.snowball.entity.User;
import com.example.snowball.repository.UserRepository; // ✅ 引入 UserRepository (根据你的实际包名调整)
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

                // ✅ 核心新增：根据 ID 去数据库查出当前用户实体
                User user = userRepository.findById(userId).orElse(null);

                if (user != null) {
                    // ✅ 核心逻辑：将数据库里的枚举角色，转为 Spring Security 认识的格式
                    // 注意：Spring Security 强制要求角色前缀必须有 "ROLE_"
                    String roleName = "ROLE_" + user.getRole().name(); // 比如 "ROLE_SYS_ADMIN"

                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);

                    // ✅ 把带有角色的列表传进去（替换掉原来的 new ArrayList<>()）
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, List.of(authority));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // ✅ 核心修复：如果根据 Token 里的 ID 查不到用户（比如数据库被清空、用户被注销）
                    // 必须清除上下文并直接返回 401，绝对不能放行！
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 返回 401 状态码
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401, \"message\":\"Token 无效或用户不存在，请重新登录\"}");
                    return; // ✅ 关键：直接 return，终止请求，不再往下走 filterChain
                }
            } catch (Exception e) {
                // Token 无效或用户被删，不放行
                SecurityContextHolder.clearContext();
                e.printStackTrace();
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
