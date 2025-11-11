package com.beautybooking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO para crear franjas horarias (solo ADMIN).
 *
 * Permite al administrador abrir slots de tiempo para reservas.
 * Ejemplo: Crear franjas de "Corte de pelo" para el próximo mes.
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FranjaRequest {

    /**
     * ID del servicio al que pertenece esta franja.
     */
    @NotNull(message = "El ID del servicio es obligatorio")
    private Long servicioId;

    /**
     * Fecha de la franja.
     */
    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    /**
     * Hora de inicio.
     * Debe estar entre 07:00 y 22:00 según reglas de negocio.
     */
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    /**
     * Número de plazas disponibles.
     * Si no se especifica, toma el aforoMaximo del servicio.
     */
    private Integer plazasDisponibles;
}