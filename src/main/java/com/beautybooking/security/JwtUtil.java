package com.beautybooking.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utilidad para generación y validación de tokens JWT.
 *
 * JWT (JSON Web Token) es el mecanismo de autenticación usado en la API.
 *
 * Flujo:
 * 1. Usuario hace login → se genera token JWT
 * 2. Frontend guarda token (localStorage o sessionStorage)
 * 3. En cada petición, frontend envía: Authorization: Bearer {token}
 * 4. Backend valida token y extrae email del usuario
 * 5. Backend carga usuario desde BD y autoriza la petición
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Component
public class JwtUtil {

    /**
     * Clave secreta para firmar tokens.
     * Se inyecta desde application.properties (jwt.secret).
     * DEBE ser diferente en producción y mantenerse segura.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Tiempo de expiración del token en milisegundos.
     * Por defecto: 86400000 ms = 24 horas.
     */
    @Value("${jwt.expiration-ms}")
    private Long expirationMs;

    /**
     * Genera un token JWT para un usuario.
     *
     * @param userDetails Información del usuario autenticado
     * @return Token JWT firmado
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Crea el token JWT con claims y subject (email del usuario).
     *
     * Estructura del token:
     * - Header: algoritmo (HS256) y tipo (JWT)
     * - Payload: subject (email), issued-at, expiration
     * - Signature: firma con la clave secreta
     *
     * @param claims Información adicional a incluir en el token
     * @param subject Email del usuario
     * @return Token JWT
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Obtiene la clave de firma a partir del secret.
     * Usa HMAC-SHA256 para firmar el token.
     *
     * @return SecretKey para firmar/verificar tokens
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae el email (subject) del token JWT.
     *
     * @param token Token JWT
     * @return Email del usuario
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración del token.
     *
     * @param token Token JWT
     * @return Fecha de expiración
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim específico del token.
     *
     * @param token Token JWT
     * @param claimsResolver Función para extraer el claim deseado
     * @param <T> Tipo del claim
     * @return Valor del claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims del token.
     * Valida la firma del token en el proceso.
     *
     * @param token Token JWT
     * @return Claims del token
     * @throws JwtException si el token es inválido
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Verifica si el token ha expirado.
     *
     * @param token Token JWT
     * @return true si expiró, false si aún es válido
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Valida un token JWT.
     *
     * Verifica que:
     * 1. El email en el token coincida con el usuario actual
     * 2. El token no haya expirado
     *
     * @param token Token JWT
     * @param userDetails Usuario cargado desde BD
     * @return true si el token es válido, false si no
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Valida un token JWT sin verificar contra un usuario específico.
     * Útil para validaciones rápidas.
     *
     * @param token Token JWT
     * @return true si el token es válido (no expirado y firma correcta)
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}