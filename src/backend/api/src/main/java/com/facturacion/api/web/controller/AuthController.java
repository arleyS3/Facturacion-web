package com.facturacion.api.web.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.facturacion.api.web.dto.AuthResponse;
import com.facturacion.api.web.dto.CreateUserRequest;
import com.facturacion.api.web.dto.LoginRequest;
import com.facturacion.api.web.dto.MeResponse;
import com.facturacion.api.web.dto.RefreshRequest;
import com.facturacion.api.web.dto.RegisterRequest;
import com.facturacion.api.web.dto.UserResponse;
import com.facturacion.api.web.models.UserEntity;
import com.facturacion.api.web.repositories.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        boolean secure = shouldUseSecureCookies(request);
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(secure ? "None" : "Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void setRefreshCookie(HttpServletResponse response, String value, HttpServletRequest request) {
        boolean secure = shouldUseSecureCookies(request);
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .path("/api/v1/auth/refresh")
                .maxAge(REFRESH_MAX_AGE)
                .sameSite(secure ? "None" : "Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response, HttpServletRequest request) {
        boolean secure = shouldUseSecureCookies(request);
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .path("/api/v1/auth/refresh")
                .maxAge(0)
                .sameSite(secure ? "None" : "Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private boolean shouldUseSecureCookies(HttpServletRequest request) {
        String host = request.getHeader("Host");
        return host == null || !(host.startsWith("localhost") || host.startsWith("127.0.0.1"));
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
    public ResponseEntity<?> me(@AuthenticationPrincipal String email) {
        if (email == null) return ResponseEntity.status(401).build();
        var user = userRepository.findByEmail(email);
        if (user.isEmpty()) return ResponseEntity.status(404).build();
        String role = user.get().getRole();
        if (role == null || role.isBlank()) {
            role = "USER";
        }
        return ResponseEntity.ok(new MeResponse(user.get().getEmail(), role));
    }

    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRole(@PathVariable UUID id, @RequestBody Map<String, String> body,
                                         @AuthenticationPrincipal String email) {
        var target = userRepository.findById(id);
        if (target.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Usuario no encontrado"));
        }
        var requester = userRepository.findByEmail(email);
        if (requester.isPresent() && requester.get().getId().equals(id)) {
            return ResponseEntity.badRequest().body(Map.of("error", "No puedes autodegradarte"));
        }
        String newRole = body.get("role");
        if (newRole == null || (!newRole.equals("ADMIN") && !newRole.equals("USER"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rol invalido"));
        }
        target.get().setRole(newRole);
        userRepository.save(target.get());
        return ResponseEntity.ok(Map.of("message", "Rol actualizado a " + newRole));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> listUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(u -> UserResponse.builder()
                        .id(u.getId())
                        .email(u.getEmail())
                        .role(u.getRole() != null ? u.getRole() : "USER")
                        .createdAt(u.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank() ||
            request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email y contraseña son requeridos"));
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El usuario ya existe"));
        }
        String role = (request.getRole() != null && (request.getRole().equals("ADMIN") || request.getRole().equals("USER")))
                ? request.getRole()
                : "USER";

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build());
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id, @AuthenticationPrincipal String email) {
        var target = userRepository.findById(id);
        if (target.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Usuario no encontrado"));
        }
        var requester = userRepository.findByEmail(email);
        if (requester.isPresent() && requester.get().getId().equals(id)) {
            return ResponseEntity.badRequest().body(Map.of("error", "No puedes eliminar tu propia cuenta"));
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Usuario eliminado correctamente"));
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
