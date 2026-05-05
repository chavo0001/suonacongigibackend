package it.generation.suonacongigi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * ARCHITETTURA: Cryptographic Token Factory.
 * Questa utility trasforma l'identità Java in una stringa URL-safe firmata digitalmente.
 * Rilevante: Usiamo l'algoritmo HS256. La chiave segreta non deve mai lasciare il server.
 */
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    // MECCANICA: Generazione della chiave HMAC via Reflection/StandardCharsets.
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Trasforma un UserDetails (Identity) in un JWT (Passport).
     * Inseriamo il ruolo come "Claim" per permettere ai Proxy AOP di leggerlo velocemente.
     */
    public String generateToken(UserDetails userDetails) {
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("ROLE_USER");

        return Jwts.builder()
                .subject(userDetails.getUsername()) 
                .claim("role", role)
                .issuedAt(new Date()) 
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey()) 
                .compact();
    }

    // MECCANICA: Parsing e Validazione. 
    // Se la firma è manomessa, parseSignedClaims lancia una JwtException immediatamente.
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = extractAllClaims(token);
            String username = claims.getSubject();
            boolean isExpired = claims.getExpiration().before(new Date());
            
            return (username.equals(userDetails.getUsername()) && !isExpired);
        } catch (JwtException | IllegalArgumentException e) {
            // Certificazione Fallita: il token è corrotto o scaduto.
            return false;
        }
    }
}