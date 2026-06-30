package com.facturacion.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiter simple para {@code POST /api/v1/auth/login}.
 * <p>
 * Máximo 5 intentos por IP por minuto usando ventana fija.
 * Los contadores se resetean cada 60 segundos vía {@link Scheduled}.
 * Respeta {@code X-Forwarded-For} para IP real detrás de proxy.
 * </p>
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    static final int MAX_ATTEMPTS = 5;
    static final long RESET_INTERVAL_MS = 60_000;

    private final ConcurrentHashMap<String, AtomicInteger> attempts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        if (isLoginRequest(request)) {
            String ip = resolveClientIP(request);
            AtomicInteger counter = attempts.computeIfAbsent(ip, k -> new AtomicInteger(0));

            if (counter.incrementAndGet() > MAX_ATTEMPTS) {
                response.setStatus(429);
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("Demasiados intentos. Intente de nuevo en 1 minuto");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Scheduled(fixedRate = RESET_INTERVAL_MS)
    public void resetCounters() {
        attempts.clear();
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return "/api/v1/auth/login".equals(request.getRequestURI())
                && "POST".equalsIgnoreCase(request.getMethod());
    }

    private String resolveClientIP(HttpServletRequest request) {
        String xfwd = request.getHeader("X-Forwarded-For");
        if (xfwd != null && !xfwd.isBlank()) {
            return xfwd.split(",")[0].trim();
        }
        String ip = request.getRemoteAddr();
        return "0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip) ? "127.0.0.1" : ip;
    }
}
