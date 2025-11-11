package com.beautybooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO genérico para respuestas simples de la API.
 *
 * Usado para operaciones como crear, actualizar, eliminar
 * donde solo necesitamos confirmar éxito y un mensaje.
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {

    /**
     * Indica si la operación fue exitosa.
     */
    private Boolean success;

    /**
     * Mensaje descriptivo del resultado.
     */
    private String message;

    /**
     * Constructor de conveniencia para respuestas exitosas.
     */
    public static ApiResponse success(String message) {
        return new ApiResponse(true, message);
    }

    /**
     * Constructor de conveniencia para respuestas de error.
     */
    public static ApiResponse error(String message) {
        return new ApiResponse(false, message);
    }
}