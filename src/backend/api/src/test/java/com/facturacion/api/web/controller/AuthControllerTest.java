package com.facturacion.api.web.controller;

import com.facturacion.api.security.JwtService;
import com.facturacion.api.security.TokenBlacklistService;
import com.facturacion.api.web.dto.UpdateUserRequest;
import com.facturacion.api.web.dto.UserResponse;
import com.facturacion.api.web.models.UserEntity;
import com.facturacion.api.web.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistService blacklistService;

    @InjectMocks
    private AuthController authController;

    private UUID userId;
    private UserEntity existingUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        existingUser = UserEntity.builder()
                .id(userId)
                .email("usuario@test.com")
                .password("$2a$10$hashedpassword")
                .role("ADMIN")
                .build();
    }

    @Test
    @DisplayName("Debe retornar 400 cuando la contraseña actual no coincide al editar usuario como Admin")
    void testUpdateUser_IncorrectOldPassword_Returns400() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrong_password", existingUser.getPassword())).thenReturn(false);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("usuario@test.com");
        request.setOldPassword("wrong_password");
        request.setNewPassword("new_password_123");
        request.setRole("USER");

        ResponseEntity<?> response = authController.updateUser(userId, request, "admin@test.com");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("La contraseña actual es incorrecta", body.get("error"));
    }

    @Test
    @DisplayName("Debe retornar 400 cuando la contraseña actual no coincide al editar perfil")
    void testUpdateProfile_IncorrectOldPassword_Returns400() {
        when(userRepository.findByEmail("usuario@test.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrong_password", existingUser.getPassword())).thenReturn(false);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("usuario@test.com");
        request.setOldPassword("wrong_password");
        request.setNewPassword("new_password_123");

        ResponseEntity<?> response = authController.updateProfile(request, "usuario@test.com");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("La contraseña actual es incorrecta", body.get("error"));
    }

    @Test
    @DisplayName("Debe actualizar perfil exitosamente cuando la contraseña actual es correcta")
    void testUpdateProfile_Success() {
        when(userRepository.findByEmail("usuario@test.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("correct_password", existingUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("new_password_123")).thenReturn("$2a$10$newhashedpassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("nuevo_email@test.com");
        request.setOldPassword("correct_password");
        request.setNewPassword("new_password_123");

        ResponseEntity<?> response = authController.updateProfile(request, "usuario@test.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof UserResponse);
        UserResponse userResponse = (UserResponse) response.getBody();
        assertEquals("nuevo_email@test.com", userResponse.getEmail());
    }
}
