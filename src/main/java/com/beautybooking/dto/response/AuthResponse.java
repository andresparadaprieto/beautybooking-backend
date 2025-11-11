package com.beautybooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para autenticación (login/register).
 *
 * Devuelve el token JWT y datos básicos del usuario.
 * El frontend almacenará el token y lo enviará en cada petición
 * mediante header: Authorization: Bearer {token}
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /**
     * Token JWT firmado.
     * Válido por 24 horas (configurable en application.properties).
     */
    private String token;

    /**
     * Tipo de token (siempre "Bearer").
     */
    private String type = "Bearer";

    /**
     * ID del usuario autenticado.
     */
    private Long id;

    /**
     * Email del usuario.
     */
    private String email;

    /**
     * Nombre del usuario.
     */
    private String nombre;

    /**
     * Rol del usuario (CLIENTE o ADMIN).
     */
    private String rol;

    /**
     * Constructor sin el campo 'type' (se establece por defecto).
     */
    public AuthResponse(String token, Long id, String email, String nombre, String rol) {
        this.token = token;
        this.type = "Bearer";
        this.id = id;
        this.email = email;
        this.nombre = nombre;
        this.rol = rol;
    }
}