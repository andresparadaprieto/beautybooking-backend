package com.beautybooking.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para petición de registro de nuevo usuario.
 *
 * Recibe datos necesarios para crear una cuenta.
 * Incluye validaciones de formato y longitud.
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /**
     * Nombre completo del usuario.
     * Mínimo 2 caracteres, máximo 100.
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    /**
     * Email del usuario (será su username).
     * Debe ser único en el sistema.
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    @Size(max = 150, message = "El email no puede superar 150 caracteres")
    private String email;

    /**
     * Contraseña del usuario.
     * Mínimo 6 caracteres para seguridad básica.
     * Se hasheará con BCrypt antes de almacenar.
     */
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    private String password;

    /**
     * Teléfono del usuario (opcional).
     * Formato flexible para números españoles e internacionales.
     */
    @Pattern(regexp = "^[+]?[0-9]{9,15}$",
            message = "El teléfono debe tener entre 9 y 15 dígitos")
    private String telefono;
}