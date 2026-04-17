package com.facturacion.api.security;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.facturacion.api.web.models.BlacklistedToken;
import com.facturacion.api.web.repositories.BlacklistedTokenRepository;

@Service
public class TokenBlacklistService {
    
    private final BlacklistedTokenRepository repository;

    public TokenBlacklistService(BlacklistedTokenRepository repository) {
        this.repository = repository;
    }

    public void addToBlacklist(String token) {

        BlacklistedToken entity = BlacklistedToken.builder()
                .token(token)
                .expiredAt(LocalDateTime.now().plusHours(1)) 
                .build();

        repository.save(entity);
    }

    public boolean isBlacklisted(String token) {
        return repository.findByToken(token).isPresent();
    }
}
