package org.tokomoapp.tokomoappbackend.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.tokomoapp.tokomoappbackend.model.ApiResponse;
import org.tokomoapp.tokomoappbackend.model.User;
import org.tokomoapp.tokomoappbackend.service.UserService;
import org.tokomoapp.tokomoappbackend.util.JwtUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, ObjectMapper objectMapper, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                // 验证token并获取用户信息
                if (!jwtUtil.validateToken(token)) {
                    handleAuthenticationError(response, "Invalid token");
                    return;
                }
                
                try {
                    Long userId = jwtUtil.extractUserId(token);
                    User user = userService.getUserById(userId);
                    
                    String username = user.getUsername();
                    Boolean isAdmin = user.getIsAdmin();
                    Boolean isVip = user.isVIP();

                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority(isVip ? "ROLE_VIP" : "ROLE_NOTVIP"));
                    authorities.add(new SimpleGrantedAuthority(isAdmin ? "ROLE_ADMIN" : "ROLE_USER"));

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        username,
                        authorities
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (Exception e) {
                    handleAuthenticationError(response, "User not found or token expired");
                    return;
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            handleAuthenticationError(response, e.getMessage());
        }
    }

    private void handleAuthenticationError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        ApiResponse errorResponse = new ApiResponse("error", message);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
} 