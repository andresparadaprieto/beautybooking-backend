package com.beautybooking.model.enums;

/**
 * Enumeración de estados posibles de una reserva en BeautyBooking.
 *
 * Ciclo de vida típico de una reserva:
 * PENDIENTE → CONFIRMADA → COMPLETADA
 *           ↓
 *        CANCELADA
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
public enum EstadoReserva {
    /**
     * Reserva creada pero pendiente de confirmación.
     * Estado inicial al crear una reserva.
     */
    PENDIENTE,

    /**
     * Reserva confirmada por el sistema o administrador.
     * El cliente puede presentarse en la fecha/hora indicada.
     */
    CONFIRMADA,

    /**
     * Reserva completada - el servicio fue prestado.
     * Estado final positivo.
     */
    COMPLETADA,

    /**
     * Reserva cancelada por el cliente o administrador.
     * Las plazas se liberan automáticamente.
     */
    CANCELADA
}