package com.beautybooking.exception;

/**
 * Excepción personalizada para errores de lógica de negocio.
 *
 * Se lanza cuando se viola una regla de negocio.
 * Ejemplos:
 * - Intentar reservar una franja sin plazas disponibles
 * - Reservar fuera del horario permitido (07:00-22:00)
 * - Usuario con reserva solapada
 *
 * HTTP Status: 400 BAD REQUEST o 409 CONFLICT
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
public class BusinessException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     *
     * @param message Mensaje descriptivo del error de negocio
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje y causa.
     *
     * @param message Mensaje descriptivo
     * @param cause Causa original del error
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}