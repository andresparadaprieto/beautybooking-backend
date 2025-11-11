package com.beautybooking.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para petición de creación de reserva.
 *
 * El cliente envía el ID de la franja horaria que desea reservar.
 * El sistema valida disponibilidad y reglas de negocio antes de crear.
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaRequest {

    /**
     * ID de la franja horaria a reservar.
     * Debe existir y tener plazas disponibles.
     */
    @NotNull(message = "El ID de la franja es obligatorio")
    private Long franjaId;

    /**
     * Notas adicionales del cliente (opcional).
     * Ejemplo: "Prefiero la estilista María", "Alergia al tinte X"
     */
    @Size(max = 500, message = "Las notas no pueden superar 500 caracteres")
    private String notas;

    /**
     * Email del usuario para quien se crea la reserva (OPCIONAL).
     *
     * Si es null o vacío, se usa el email del usuario autenticado (del token JWT).
     *
     * Ejemplo de uso ADMIN: "maria.garcia@example.com"
     */
    @Email(message = "Debe ser un email válido")
    private String usuarioEmail;
}
