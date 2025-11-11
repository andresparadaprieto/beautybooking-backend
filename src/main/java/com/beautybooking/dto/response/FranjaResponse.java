package com.beautybooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO de respuesta para franjas horarias.
 *
 * Muestra disponibilidad de slots para un servicio.
 * El frontend usa esta info para mostrar el calendario de reservas.
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FranjaResponse {

    private Long id;
    private Long servicioId;
    private String servicioNombre;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer plazasTotales;
    private Integer plazasDisponibles;
    private Boolean disponible; // Calculado: plazasDisponibles > 0
}