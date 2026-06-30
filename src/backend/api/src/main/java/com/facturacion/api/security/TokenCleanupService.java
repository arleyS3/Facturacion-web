package com.facturacion.api.security;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.facturacion.api.web.repositories.BlacklistedTokenRepository;

@Service
public class TokenCleanupService {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupService.class);

    private final BlacklistedTokenRepository repository;

    public TokenCleanupService(BlacklistedTokenRepository repository) {
        this.repository = repository;
    }

    // Limpieza automáticamente cada hora
    @Scheduled(fixedRate = 1000 * 60 * 60)
    @Transactional
    public void cleanExpiredTokens() {
        repository.deleteByExpiredAtBefore(LocalDateTime.now());
        log.info("Tokens expirados eliminados");
    }
}
