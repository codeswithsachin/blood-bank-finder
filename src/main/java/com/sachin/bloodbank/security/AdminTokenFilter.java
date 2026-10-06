package com.sachin.bloodbank.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class AdminTokenFilter extends OncePerRequestFilter {
    private final String adminToken;

    public AdminTokenFilter(@Value("${app.admin-token}") String adminToken) {
        this.adminToken = adminToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/api/admin")) {
            String token = request.getHeader("X-ADMIN-TOKEN");
            if (token == null || !token.equals(adminToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid admin token\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
