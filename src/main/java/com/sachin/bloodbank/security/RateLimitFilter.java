package com.sachin.bloodbank.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple, dependency-free per-IP rate limiter for the public,
 * unauthenticated POST endpoints that could otherwise be spammed
 * (donor registration, emergency requests). Fixed-window counter:
 * max N requests per IP per path per window, reset every window.
 *
 * This is intentionally basic (in-memory, single-instance) — good enough
 * for a single-server student/portfolio deployment. A production system
 * with multiple instances would need a shared store (Redis) instead.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private static final int MAX_REQUESTS_PER_WINDOW = 5;
    private static final long WINDOW_MILLIS = 10 * 60 * 1000; // 10 minutes

    private static final Set<String> LIMITED_PATHS = Set.of(
            "/register", "/request-blood", "/api/donors", "/api/requests"
    );

    private record Bucket(AtomicInteger count, long windowStart) { }

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        boolean limited = "POST".equalsIgnoreCase(request.getMethod())
                && LIMITED_PATHS.stream().anyMatch(path::equals);

        if (limited) {
            String key = clientIp(request) + ":" + path;
            long now = Instant.now().toEpochMilli();

            Bucket bucket = buckets.compute(key, (k, existing) -> {
                if (existing == null || now - existing.windowStart() > WINDOW_MILLIS) {
                    return new Bucket(new AtomicInteger(1), now);
                }
                existing.count().incrementAndGet();
                return existing;
            });

            if (bucket.count().get() > MAX_REQUESTS_PER_WINDOW) {
                response.setStatus(429); // Too Many Requests
                response.setContentType("text/plain");
                response.getWriter().write("Too many requests. Please wait a few minutes and try again.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
