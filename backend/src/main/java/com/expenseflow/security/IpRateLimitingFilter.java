package com.expenseflow.security;

import com.expenseflow.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filter responsible for enforcing IP-based Rate Limiting on authentication endpoints.
 * Development Strategy: Uses Bucket4j with an in-memory ConcurrentHashMap cache.
 * Production Migration Plan: Migrate the bucket resolver to connect to a Redis-backed Distributed Rate Limiter.
 */
@Component
public class IpRateLimitingFilter implements Filter {

    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public IpRateLimitingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        String endpointType = getEndpointType(path, method);
        if (endpointType != null) {
            String ip = getClientIp(httpRequest);
            String clientKey = ip;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
                clientKey = principal.getId();
            }
            String cacheKey = clientKey + ":" + ip + ":" + endpointType;

            Bucket bucket = ipBuckets.computeIfAbsent(cacheKey, k -> createNewBucket(endpointType));

            if (!bucket.tryConsume(1)) {
                // Add CORS headers so the browser can read this response.
                // Without these, the browser's CORS policy blocks the response body
                // and Axios reports it as a network error ("Unable to establish a connection")
                // instead of a proper 429 Too Many Requests error.
                String origin = httpRequest.getHeader("Origin");
                if (origin != null) {
                    httpResponse.setHeader("Access-Control-Allow-Origin", origin);
                    httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
                    httpResponse.setHeader("Vary", "Origin");
                }
                httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);

                ApiResponse<?> apiResponse = ApiResponse.error("Too many requests. Please try again later.", "RATE_001");
                httpResponse.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String getEndpointType(String path, String method) {
        if ("POST".equalsIgnoreCase(method)) {
            if (path.endsWith("/api/v1/auth/login")) return "LOGIN";
            if (path.endsWith("/api/v1/auth/register")) return "REGISTER";
            if (path.endsWith("/api/v1/auth/forgot-password")) return "FORGOT_PASSWORD";
            if (path.endsWith("/api/v1/auth/refresh")) return "REFRESH";
            if (path.endsWith("/api/v1/auth/verify-email") || path.endsWith("/api/v1/auth/resend-verification")) return "VERIFY_EMAIL";
            if (path.endsWith("/api/v1/auth/check-email")) return "CHECK_EMAIL";
            if (path.endsWith("/api/v1/groups/join")) return "GROUP_JOIN";
            if (path.endsWith("/api/v1/groups") || path.endsWith("/api/v1/groups/")) return "GROUP_CREATE";
        }
        return null;
    }

    private Bucket createNewBucket(String endpointType) {
        Bandwidth limit;
        switch (endpointType) {
            case "REGISTER" -> 
                limit = Bandwidth.classic(3, Refill.intervally(3, Duration.ofMinutes(1)));
            case "LOGIN" -> 
                limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
            case "REFRESH" -> 
                limit = Bandwidth.classic(30, Refill.intervally(30, Duration.ofMinutes(1)));
            case "FORGOT_PASSWORD" -> 
                limit = Bandwidth.classic(3, Refill.intervally(3, Duration.ofHours(1)));
            case "VERIFY_EMAIL" -> 
                limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofHours(1)));
            case "CHECK_EMAIL" -> 
                limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofHours(1)));
            case "GROUP_JOIN" -> 
                limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
            case "GROUP_CREATE" -> 
                limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
            default -> 
                limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        }
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
