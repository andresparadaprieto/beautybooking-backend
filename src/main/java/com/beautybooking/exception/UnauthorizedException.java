package com.beautybooking.exception;

/**
 * Excepción personalizada para errores de autenticación/autorización.
 *
 * Se lanza cuando:
 * - Credenciales inválidas en login
 * - Token JWT expirado o inválido
 * - Usuario intenta acceder a recurso sin permisos
 *
 * HTTP Status: 401 UNAUTHORIZED
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     *
     * @param message Mensaje descriptivo del error de autenticación
     */
    public UnauthorizedException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje y causa.
     *
     * @param message Mensaje descriptivo
     * @param cause Causa original del error
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}