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

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.facturacion.api.security.JwtService;
import com.facturacion.api.security.TokenBlacklistService;;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {

        // 1. Validar que no exista el usuario
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return "El usuario ya existe";
        }

        // 2. Encriptar contraseña
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 3. Crear usuario
        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .password(hashedPassword)
                .role("USER")
                .build();

        // 4. Guardar en base de datos
        userRepository.save(user);

        return "Usurio registrado correctamente";
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {

        // 1. Buscar usuario por email
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Verificar contraseña
        boolean isValid = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!isValid) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        // 3. Login correcto (Genera el token)
        String accessTokenoken = jwtService.generateToken(user.getEmail(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessTokenoken)
                .refreshToken(refreshToken)
                .build();
    }

    @PostMapping("/refresh")
    public AuthResponse refreshToken(@RequestBody RefreshRequest request) {

        String email = jwtService.extractEmail(request.getRefreshToken());

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String newAccessToken = jwtService.generateToken(user.getEmail(), user.getRole());

        return new AuthResponse(newAccessToken, request.getRefreshToken());
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            blacklistService.addToBlacklist(token);
        }

        return "logout exitoso";
    }

}
