package com.facturacion.api.security;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.facturacion.api.web.repositories.BlacklistedTokenRepository;

@Service
public class TokenCleanupService {
    
    private final BlacklistedTokenRepository repository;

    public TokenCleanupService(BlacklistedTokenRepository repository) {
        this.repository = repository;
    }

    // Limpieza automáticamente cada hora
    @Scheduled(fixedRate = 1000 * 60 * 60)
    public void cleanExpiredTokens() {
        repository.deleteByExpiredAtBefore(LocalDateTime.now());
        System.out.println("Tokens expirados eliminados");
    }
}
