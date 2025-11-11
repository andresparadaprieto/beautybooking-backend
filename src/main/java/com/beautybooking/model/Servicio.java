package com.beautybooking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Servicio - Representa los tratamientos y servicios ofrecidos por el centro de estética.
 *
 * Mapea la tabla 'servicios' en la base de datos.
 * Contiene información sobre cada tipo de servicio: nombre, duración, precio y aforo máximo.
 *
 * Relaciones:
 * - Un servicio puede tener muchas franjas horarias (relación OneToMany con FranjaHoraria)
 * - Un servicio puede estar en muchas reservas (relación OneToMany con Reserva)
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "servicios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Servicio {

    /**
     * Identificador único del servicio.
     * Generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del servicio.
     * Ejemplo: "Corte y peinado", "Manicura", "Tratamiento facial"
     * Campo obligatorio, máximo 150 caracteres.
     */
    @Column(nullable = false, length = 150)
    private String nombre;

    /**
     * Descripción detallada del servicio.
     * Permite texto largo (TEXT) para explicar en qué consiste el tratamiento.
     */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Duración del servicio en minutos.
     * Ejemplo: 60 minutos para un corte de pelo.
     * Campo obligatorio.
     */
    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    /**
     * Precio del servicio en euros.
     * Usa BigDecimal para precisión en cálculos monetarios.
     * Ejemplo: 25.50 €
     * Precisión: 7 dígitos totales, 2 decimales
     * Campo obligatorio.
     */
    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal precio;

    /**
     * Número máximo de clientes que pueden recibir este servicio simultáneamente.
     * Ejemplo: 1 para servicios individuales, >1 para clases grupales.
     * Por defecto: 1
     * Campo obligatorio.
     */
    @Column(name = "aforo_maximo", nullable = false)
    private Integer aforoMaximo = 1;

    /**
     * Indica si el servicio está activo y disponible para reservar.
     * Servicios inactivos no aparecen en el catálogo.
     * Por defecto: true
     */
    @Column(nullable = false)
    private Boolean activo = true;

    /**
     * Fecha y hora de creación del servicio.
     * Se establece automáticamente al crear el registro.
     */
    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    /**
     * Lista de franjas horarias disponibles para este servicio.
     * Relación OneToMany con FranjaHoraria.
     * CascadeType.ALL: eliminar servicio elimina sus franjas.
     */
    @OneToMany(mappedBy = "servicio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FranjaHoraria> franjas = new ArrayList<>();

    /**
     * Lista de reservas asociadas a este servicio.
     * Relación OneToMany con Reserva.
     */
    @OneToMany(mappedBy = "servicio")
    private List<Reserva> reservas = new ArrayList<>();

    /**
     * Constructor de conveniencia para crear servicios.
     * Útil en tests y DataLoader.
     */
    public Servicio(String nombre, String descripcion, Integer duracionMinutos, BigDecimal precio, Integer aforoMaximo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.duracionMinutos = duracionMinutos;
        this.precio = precio;
        this.aforoMaximo = aforoMaximo;
        this.activo = true;
    }
}