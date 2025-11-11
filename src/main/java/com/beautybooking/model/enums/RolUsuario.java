package com.beautybooking.model.enums;


/**
 * Enumeración de roles de usuario en el sistema BeautyBooking.
 *
 * Define los tipos de usuario y sus permisos:
 * - CLIENTE: Usuario estándar que puede hacer reservas
 * - ADMIN: Administrador con permisos completos de gestión
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
public enum RolUsuario {
    /**
     * Cliente estándar del sistema.
     * Permisos: realizar reservas, ver su historial, cancelar sus propias reservas
     */
    CLIENTE,

    /**
     * Administrador del sistema.
     * Permisos: gestión completa de servicios, franjas horarias, ver todas las reservas,
     * generar reportes y gestionar usuarios
     */
    ADMIN
}