package com.inventario.UsuariosService.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    
    private static final String SECRET = "InventarioSecretKeyForJWTTokenGenerationThatMustBeLongEnoughForHS512AlgorithmRequirements2025";
    private static final int JWT_TOKEN_VALIDITY = 5 * 60 * 60; // 5 horas

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // Generar token para usuario
    public String generateToken(String correo, String nombre, String rol) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("nombre", nombre);
        claims.put("rol", rol);
        return createToken(claims, correo);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Extraer correo del token
    public String extractCorreo(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extraer fecha de expiración
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extraer rol del token
    public String extractRol(String token) {
        return extractClaim(token, claims -> claims.get("rol", String.class));
    }

    // Extraer nombre del token
    public String extractNombre(String token) {
        return extractClaim(token, claims -> claims.get("nombre", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Verificar si el token ha expirado
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Validar token
    public Boolean validateToken(String token, String correo) {
        final String extractedCorreo = extractCorreo(token);
        return (extractedCorreo.equals(correo) && !isTokenExpired(token));
    }

    // Verificar si el usuario tiene rol ADMIN
    public Boolean isAdmin(String token) {
        String rol = extractRol(token);
        return "ADMIN".equals(rol);
    }
}
