package com.udea.skillbridge.seguridad.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.udea.skillbridge.seguridad.entity.UsuarioEntity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JwtService {
	
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.access-token-expiration}")
    private Long accessTokenExpiration;   // milisegundos
    
    // ── Generar access token ────────────────────────────────────────

    public String generateAccessToken(UsuarioEntity usuarioEnt) {
        Map<String, Object> claims = new HashMap<>();

        // Roles como lista de strings
        claims.put("roles", usuarioEnt.getRoles().stream()
                .map(r -> r.getNombre().name())
                .collect(Collectors.toList()));

        // Permisos como lista de strings
        claims.put("permissions", usuarioEnt.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .filter(a -> !a.startsWith("ROLE_"))  // solo permisos, no roles
                .collect(Collectors.toList()));

        claims.put("firstName", usuarioEnt.getNombre());
        claims.put("email",     usuarioEnt.getEmail());

        return Jwts.builder()
                .claims(claims)
                .subject(usuarioEnt.getId().toString())  // idUsuario como subject
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }
    
    // ── Extraer información del token ───────────────────────────────

    public Long extractUserId(String token) {
        return Long.parseLong(extractClaim(token, Claims::getSubject));
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }
    
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractClaim(token,
            claims -> claims.get("roles", List.class));
    }
    
    public boolean isTokenValid(String token, UsuarioEntity usuarioEnt) {
        try {
            Long userId = extractUserId(token);
            return userId.equals(usuarioEnt.getId()) && !isTokenExpired(token);
        } catch (JwtException ex) {
            log.warn("Token inválido: {}", ex.getMessage());
            return false;
        }
    } 
    
    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public Long getAccessTokenExpiration() {
        return accessTokenExpiration / 1000; // en segundos para la response
    }
    
    // ── Helpers privados ────────────────────────────────────────────

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
