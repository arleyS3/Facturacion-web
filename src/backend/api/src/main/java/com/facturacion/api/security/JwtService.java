package com.facturacion.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private Key accessKey;
    private Key refreshKey;

    @PostConstruct
    public void init() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "jwt.secret debe tener al menos 32 caracteres (256 bits). Actual: " +
                    (secret == null ? "null" : secret.length() + " chars"));
        }
        // Deriva dos keys distintas del mismo secret para evitar que
        // un token de un tipo pueda firmarse con la key del otro.
        this.accessKey = Keys.hmacShaKeyFor((secret + ":access").getBytes());
        this.refreshKey = Keys.hmacShaKeyFor((secret + ":refresh").getBytes());
    }

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(accessKey)
                .compact();
    }

    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7))
                .signWith(refreshKey)
                .compact();
    }

    /**
     * Extrae todos los claims de un ACCESS TOKEN.
     * Lanza excepción si el token está firmado con otra key (ej. refresh).
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(accessKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extrae el subject (email) de un REFRESH TOKEN.
     */
    public String extractEmailFromRefresh(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(refreshKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
