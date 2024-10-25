package com.devprogen.infrastructure.config.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${devprogen.jwt.secretkey}")
    private String secretKey;

    @Value("${devprogen.jwt.expirationtime}")
    private long expirationTime;

    // Convert the string secret key to a Key object
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(String username, Collection<?> roles) {
        Map<String, Object> claims = new HashMap<>();
        if(roles != null) claims.put("roles", roles);
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // Use expiration time from properties
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Use Key object for signing
                .compact();
    }

    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Collection<?> extractRoles(String token) {
        return (Collection<?>) extractAllClaims(token).get("roles"); // Extract roles from claims
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder() // Use parserBuilder instead of parser()
                .setSigningKey(getSigningKey()) // Use Key object for verification
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}
