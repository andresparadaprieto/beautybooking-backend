package com.beautybooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO de respuesta para información de usuario.
 *
 * NO incluye el passwordHash por seguridad.
 * Se usa en listados de usuarios y perfil de usuario.
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {

    private Long id;
    private String nombre;
    private String email;
    private String rol;
    private String telefono;
    private Boolean activo;
    private Instant creadoEn;
}