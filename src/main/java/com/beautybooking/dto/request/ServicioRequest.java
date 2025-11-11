package com.beautybooking.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para crear/editar servicios (solo ADMIN).
 *
 * Permite al administrador dar de alta nuevos tratamientos
 * o modificar los existentes.
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicioRequest {

    /**
     * Nombre del servicio.
     */
    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
    private String nombre;

    /**
     * Descripción detallada del servicio.
     */
    @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
    private String descripcion;

    /**
     * Duración en minutos.
     * Ejemplo: 60 para una hora, 30 para media hora.
     */
    @NotNull(message = "La duración es obligatoria")
    @Min(value = 5, message = "La duración mínima es 5 minutos")
    @Max(value = 480, message = "La duración máxima es 480 minutos (8 horas)")
    private Integer duracionMinutos;

    /**
     * Precio del servicio en euros.
     */
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que 0")
    @DecimalMax(value = "9999.99", message = "El precio no puede superar 9999.99€")
    private BigDecimal precio;

    /**
     * Aforo máximo (número de clientes simultáneos).
     * Por defecto 1 para servicios individuales.
     */
    @NotNull(message = "El aforo máximo es obligatorio")
    @Min(value = 1, message = "El aforo mínimo es 1")
    @Max(value = 50, message = "El aforo máximo es 50")
    private Integer aforoMaximo;

    /**
     * Indica si el servicio está activo.
     * Servicios inactivos no aparecen en el catálogo.
     */
    private Boolean activo = true;
}