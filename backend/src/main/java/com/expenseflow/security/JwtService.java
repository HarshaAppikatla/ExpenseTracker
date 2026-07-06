package com.expenseflow.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import com.expenseflow.config.SecurityProperties;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey key;
    private final long jwtExpirationMs;

    public JwtService(SecurityProperties securityProperties) {
        String secret = securityProperties.getJwt().getSecret();
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException e) {
            // Fallback if not Base64 encoded
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        // Convert minutes to milliseconds
        this.jwtExpirationMs = (long) securityProperties.getJwt().getAccessTokenExpiryMinutes() * 60 * 1000;
    }

    public String generateAccessToken(UserPrincipal userPrincipal) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userPrincipal.getId());
        claims.put("fullName", userPrincipal.getFullName());
        claims.put("roles", userPrincipal.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toList());

        return generateToken(claims, userPrincipal.getUsername(), jwtExpirationMs);
    }

    public String generateToken(Map<String, Object> extraClaims, String subject, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public Date getIssuedAt(String token) {
        return getClaim(token, Claims::getIssuedAt);
    }

    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT token validation failed: {}", e.getMessage());
        }
        return false;
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }
}
