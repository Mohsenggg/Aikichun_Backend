package com.aikichun.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private SecretKey secretKey;

    @Value("${aikichun.jwt.secret}")
    private String jwtSecret;

    @Value("${aikichun.jwt.expiration-hours:12}")
    private long expirationHours;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(UUID userId, String email) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationHours * 60 * 60 * 1000))
                .signWith(secretKey)
                .compact();
    }

    public java.time.Instant getExpirationTime(String token) {
        Date expiration = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration();
        return expiration.toInstant();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getSubject());
    }

    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}
