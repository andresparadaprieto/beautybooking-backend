package com.beautybooking.exception;

/**
 * Excepción personalizada para recursos no encontrados.
 *
 * Se lanza cuando se busca una entidad por ID y no existe en la base de datos.
 * Ejemplo: buscar servicio con ID=999 que no existe.
 *
 * HTTP Status: 404 NOT FOUND
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     *
     * @param message Mensaje descriptivo del error
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje y causa.
     *
     * @param message Mensaje descriptivo
     * @param cause Causa original del error
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}