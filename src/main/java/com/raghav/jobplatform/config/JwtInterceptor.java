package com.raghav.jobplatform.config;

import com.raghav.jobplatform.user.entity.UserEntity;
import com.raghav.jobplatform.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtInterceptor(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Permit CORS preflight options requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();

        String token = extractToken(request);
        UserEntity user = null;

        if (token != null && jwtService.validateToken(token)) {
            String email = jwtService.extractEmail(token);
            if (email != null) {
                user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    request.setAttribute("currentUser", user);
                  	request.setAttribute("currentUserId", user.getId());
                }
            }
        }

        // If the path is public, allow access (with user optionally bound if they are logged in)
        if (isPublicRoute(path, method)) {
            return true;
        }

        // If protected route and user is not authenticated, return 401 Unauthorized
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized: Please log in first.\"}");
            return false;
        }

        return true;
    }

    private String extractToken(HttpServletRequest request) {
        // 1. Try to extract from Authorization Header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 2. Try to extract from HttpOnly Session Cookie named "jwt"
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private boolean isPublicRoute(String path, String method) {
        // Strip trailing slash if present for robustness
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if (path.startsWith("/api/auth")) {
            return true;
        }

        // Public Job search and retrieval (GET requests)
        if (path.equals("/api/jobs") && "GET".equalsIgnoreCase(method)) {
            return true;
        }
        if (path.equals("/api/jobs/search") && "GET".equalsIgnoreCase(method)) {
            return true;
        }
        if (path.matches("/api/jobs/\\d+") && "GET".equalsIgnoreCase(method)) {
            return true;
        }

        // Provider health and search
        if (path.equals("/api/providers/health") && "GET".equalsIgnoreCase(method)) {
            return true;
        }
        if (path.equals("/api/providers/search") && "GET".equalsIgnoreCase(method)) {
            return true;
        }

        // Swagger documentation paths
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/api-docs")) {
            return true;
        }

        return false;
    }
}
