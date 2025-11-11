package com.beautybooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO de respuesta para reservas.
 *
 * Incluye información completa de la reserva con datos desnormalizados
 * para evitar múltiples llamadas al backend.
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaResponse {

    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioEmail;
    private Long servicioId;
    private String servicioNombre;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String estado;
    private BigDecimal precioFinal;
    private String notas;
    private Instant creadoEn;
}