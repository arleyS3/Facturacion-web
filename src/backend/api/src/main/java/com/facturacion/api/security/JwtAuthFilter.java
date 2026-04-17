package com.facturacion.api.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthFilter(JwtService jwtService, TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Obtener header Authorization
        final String authHeader = request.getHeader("Authorization");

        // 2. Validar que exista y tenga Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer token
        String token = authHeader.substring(7).trim();

        boolean isBlacklisted = tokenBlacklistService.isBlacklisted(token);

        System.out.println("TOKEN RECIBIDO: " + token);
        System.out.println("¿Está en blacklist?: " + isBlacklisted);

        if (isBlacklisted) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token invalido");
            return;
        }

        /*
         * f (tokenBlacklistService.isBlacklisted(token.trim())){
         * response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalido");
         * return;
         * }
         */

        try {
            // 4. Obtener email desde el token y el rol
            String email = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);

            // 5. Validar y autenticar
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role)));

                // 6. Registrar usuario en Spring
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token invalido");
            return;
        }

        // 7. Continuar con la petición
        filterChain.doFilter(request, response);
    }
}