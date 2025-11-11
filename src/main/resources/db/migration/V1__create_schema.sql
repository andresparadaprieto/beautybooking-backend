-- ========================================
-- BEAUTYBOOKING - SCHEMA PRINCIPAL
-- ========================================
-- Proyecto: BeautyBooking App
-- Descripción: Sistema de gestión de reservas para centros de estética
-- Autor: Andres Eduardo Parada Prieto
-- Versión: 1.0
-- Fecha: 2025
-- ========================================

-- ========================================
-- TABLA: usuarios
-- ========================================
CREATE TABLE usuarios (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          nombre VARCHAR(100) NOT NULL,
                          email VARCHAR(150) NOT NULL UNIQUE,
                          password_hash VARCHAR(255) NOT NULL,
                          rol VARCHAR(20) NOT NULL,
                          telefono VARCHAR(20),
                          activo BOOLEAN NOT NULL DEFAULT TRUE,
                          creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          INDEX idx_usuarios_email (email),
                          INDEX idx_usuarios_rol (rol),
                          INDEX idx_usuarios_activo (activo)
);

-- ========================================
-- TABLA: servicios
-- ========================================
CREATE TABLE servicios (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           nombre VARCHAR(150) NOT NULL,
                           descripcion TEXT,
                           duracion_minutos INT NOT NULL,
                           precio DECIMAL(7,2) NOT NULL,
                           aforo_maximo INT NOT NULL DEFAULT 1,
                           activo BOOLEAN NOT NULL DEFAULT TRUE,
                           creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CHECK (duracion_minutos > 0),
                           CHECK (precio > 0),
                           CHECK (aforo_maximo > 0),

                           INDEX idx_servicios_activo (activo),
                           INDEX idx_servicios_nombre (nombre)
);

-- ========================================
-- TABLA: franjas_horarias
-- ========================================
CREATE TABLE franjas_horarias (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  servicio_id BIGINT NOT NULL,
                                  fecha DATE NOT NULL,
                                  hora_inicio TIME NOT NULL,
                                  hora_fin TIME NOT NULL,
                                  plazas_totales INT NOT NULL,
                                  plazas_disponibles INT NOT NULL,

                                  FOREIGN KEY (servicio_id) REFERENCES servicios(id) ON DELETE CASCADE,

                                  UNIQUE KEY uk_servicio_fecha_hora (servicio_id, fecha, hora_inicio),

                                  CHECK (plazas_totales > 0),
                                  CHECK (plazas_disponibles >= 0),
                                  CHECK (plazas_disponibles <= plazas_totales),
                                  CHECK (hora_inicio < hora_fin),

                                  INDEX idx_franjas_servicio_fecha (servicio_id, fecha),
                                  INDEX idx_franjas_fecha (fecha),
                                  INDEX idx_franjas_disponibilidad (plazas_disponibles)
);

-- ========================================
-- TABLA: reservas
-- ========================================
CREATE TABLE reservas (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          usuario_id BIGINT NOT NULL,
                          servicio_id BIGINT NOT NULL,
                          franja_id BIGINT,
                          fecha DATE NOT NULL,
                          hora_inicio TIME NOT NULL,
                          hora_fin TIME NOT NULL,
                          estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
                          precio_final DECIMAL(7,2),
                          notas TEXT,
                          creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
                          FOREIGN KEY (servicio_id) REFERENCES servicios(id) ON DELETE RESTRICT,
                          FOREIGN KEY (franja_id) REFERENCES franjas_horarias(id) ON DELETE SET NULL,

                          CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'COMPLETADA', 'CANCELADA')),
                          CHECK (hora_inicio < hora_fin),
                          CHECK (precio_final IS NULL OR precio_final > 0),

                          INDEX idx_reservas_usuario_fecha (usuario_id, fecha),
                          INDEX idx_reservas_fecha_hora (fecha, hora_inicio),
                          INDEX idx_reservas_estado (estado),
                          INDEX idx_reservas_franja (franja_id),
                          INDEX idx_reservas_servicio (servicio_id)
);