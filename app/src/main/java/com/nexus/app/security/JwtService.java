package com.nexus.app.security;

import com.nexus.identity.domain.User;
import com.nexus.identity.domain.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    public JwtService(@Value("${nexus.jwt.secret:change-this-development-secret-to-a-32-byte-minimum}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    public String issue(User user) {
        Instant now = Instant.now();
        String role = user.type() == UserType.ADMIN ? "ROLE_ADMIN" : "ROLE_USER";
        return Jwts.builder().subject(user.id().toString()).claim("username", user.username()).claim("user_type", user.type().name())
                .claim("roles", role).issuer("nexus").issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(3600)))
                .id(UUID.randomUUID().toString()).signWith(key).compact();
    }
    public UUID subject(String token) { return UUID.fromString(claims(token).getSubject()); }
    public List<String> roles(String token) { return List.of(claims(token).get("roles", String.class)); }
    private Claims claims(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); }
}