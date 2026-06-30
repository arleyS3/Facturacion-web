package com.facturacion.api.web.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.facturacion.api.web.dto.AuthResponse;
import com.facturacion.api.web.dto.LoginRequest;
import com.facturacion.api.web.dto.RefreshRequest;
import com.facturacion.api.web.dto.RegisterRequest;
import com.facturacion.api.web.models.UserEntity;
import com.facturacion.api.web.repositories.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.facturacion.api.security.JwtService;
import com.facturacion.api.security.TokenBlacklistService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String COOKIE_NAME = "access_token";
    private static final String REFRESH_COOKIE_NAME = "refresh_token";
    private static final int COOKIE_MAX_AGE = 60 * 60; // 1 hora
    private static final int REFRESH_MAX_AGE = 60 * 60 * 24 * 7; // 7 días

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return "El usuario ya existe";
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .password(hashedPassword)
                .role("USER")
                .build();

        userRepository.save(user);
        return "Usuario registrado correctamente";
    }

    private void setCookie(HttpServletResponse response, String value, int maxAge, HttpServletRequest request) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(request.isSecure())
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void setRefreshCookie(HttpServletResponse response, String value, HttpServletRequest request) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(request.isSecure())
                .path("/api/v1/auth/refresh")
                .maxAge(REFRESH_MAX_AGE)
                .sameSite(request.isSecure() ? "None" : "Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response, HttpServletRequest request) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(request.isSecure())
                .path("/api/v1/auth/refresh")
                .maxAge(0)
                .sameSite(request.isSecure() ? "None" : "Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        var userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales invalidas"));
        }

        UserEntity user = userOpt.get();

        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        setCookie(httpResponse, accessToken, COOKIE_MAX_AGE, httpRequest);
        setRefreshCookie(httpResponse, refreshToken, httpRequest);

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @RequestBody(required = false) RefreshRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        // 1. Obtener refresh token (cookie > body)
        String refreshToken = extractRefreshToken(httpRequest);
        if (refreshToken == null && request != null) {
            refreshToken = request.getRefreshToken();
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(400).body(Map.of("error", "Refresh token requerido"));
        }

        // 2. Validar el token ANTES de blacklistear
        String email;
        try {
            email = jwtService.extractEmailFromRefresh(refreshToken);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Refresh token invalido o expirado"));
        }

        // 3. Buscar usuario
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Usuario no encontrado"));
        }
        UserEntity user = userOpt.get();

        // 4. Generar nuevos tokens
        String newAccessToken = jwtService.generateToken(user.getEmail(), user.getRole());
        String newRefreshToken = jwtService.generateRefreshToken(user.getEmail());

        // 5. Setear cookies
        setCookie(httpResponse, newAccessToken, COOKIE_MAX_AGE, httpRequest);
        setRefreshCookie(httpResponse, newRefreshToken, httpRequest);

        // 6. Recién acá invalidar el anterior (rotación segura)
        blacklistService.addToBlacklist(refreshToken);

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build());
    }

    @GetMapping("/me")
    public ResponseEntity<String> me(@AuthenticationPrincipal String email) {
        if (email == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(email);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (COOKIE_NAME.equals(c.getName())) {
                    blacklistService.addToBlacklist(c.getValue());
                }
                if (REFRESH_COOKIE_NAME.equals(c.getName())) {
                    blacklistService.addToBlacklist(c.getValue());
                }
            }
        }

        setCookie(response, "", 0, request);
        clearRefreshCookie(response, request);

        return ResponseEntity.ok("logout exitoso");
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return java.util.Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
