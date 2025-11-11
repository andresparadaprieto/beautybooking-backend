package com.beautybooking.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para petición de login.
 *
 * Recibe credenciales del usuario (email y contraseña).
 * Incluye validaciones con Bean Validation (JSR-380).
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * Email del usuario (usado como username).
     * Debe ser un email válido y no estar vacío.
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    private String email;

    /**
     * Contraseña del usuario.
     * Se enviará en texto plano (HTTPS protege el tránsito).
     * Se valida contra el hash almacenado en BD.
     */
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
