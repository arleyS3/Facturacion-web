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
    private static final int COOKIE_MAX_AGE = 60 * 60; // 1 hora

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
        return "Usurio registrado correctamente";
    }

    private void setCookie(HttpServletResponse response, String value, int maxAge, HttpServletRequest request) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(request.isSecure()) // true en HTTPS, false en HTTP (dev)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        setCookie(httpResponse, accessToken, COOKIE_MAX_AGE, httpRequest);

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(accessToken) // también se devuelve en el body para session en memoria
                .refreshToken(refreshToken)
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestBody RefreshRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String email = jwtService.extractEmail(request.getRefreshToken());

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String newAccessToken = jwtService.generateToken(user.getEmail(), user.getRole());
        setCookie(httpResponse, newAccessToken, COOKIE_MAX_AGE, httpRequest);

        return ResponseEntity.ok(new AuthResponse(null, request.getRefreshToken()));
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
                    break;
                }
            }
        }

        setCookie(response, "", 0, request);

        return ResponseEntity.ok("logout exitoso");
    }
}
