package com.facturacion.api.web.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.facturacion.api.web.models.BlacklistedToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long>{
    
    Optional<BlacklistedToken> findByToken(String token);

    void deleteByExpiredAtBefore(LocalDateTime now);
}
