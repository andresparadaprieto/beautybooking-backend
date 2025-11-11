package com.beautybooking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad FranjaHoraria - Representa los slots de tiempo disponibles para cada servicio.
 *
 * Mapea la tabla 'franjas_horarias' en la base de datos.
 * Define bloques de tiempo específicos en los que se puede reservar un servicio,
 * controlando el aforo (número de plazas disponibles).
 *
 * Ejemplo: "Corte de pelo el 15/01/2025 de 10:00 a 11:00 con 2 plazas disponibles"
 *
 * Relaciones:
 * - Pertenece a un Servicio (ManyToOne con Servicio)
 * - Puede tener muchas Reservas (OneToMany con Reserva)
 *
 * Reglas de negocio críticas:
 * - No puede haber dos franjas iguales (mismo servicio, fecha y hora_inicio)
 * - plazasDisponibles nunca puede ser negativo
 * - plazasDisponibles <= plazasTotales
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "franjas_horarias",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_servicio_fecha_hora",
                columnNames = {"servicio_id", "fecha", "hora_inicio"}
        ),
        indexes = {
                @Index(name = "idx_fecha", columnList = "fecha"),
                @Index(name = "idx_servicio_fecha", columnList = "servicio_id, fecha")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FranjaHoraria {

    /**
     * Identificador único de la franja horaria.
     * Generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Servicio al que pertenece esta franja horaria.
     * Relación ManyToOne - muchas franjas pueden pertenecer al mismo servicio.
     * Campo obligatorio (optional = false).
     * Si se elimina el servicio, se eliminan sus franjas (ON DELETE CASCADE en SQL).
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    /**
     * Fecha en la que está disponible esta franja.
     * Formato: YYYY-MM-DD
     * Campo obligatorio.
     */
    @Column(nullable = false)
    private LocalDate fecha;

    /**
     * Hora de inicio de la franja.
     * Formato: HH:mm:ss
     * Ejemplo: 10:00:00
     * Campo obligatorio.
     */
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    /**
     * Hora de fin de la franja.
     * Se calcula automáticamente: horaInicio + duracionMinutos del servicio
     * Formato: HH:mm:ss
     * Ejemplo: 11:00:00
     * Campo obligatorio.
     */
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    /**
     * Número total de plazas/cupos disponibles para esta franja.
     * Normalmente coincide con el aforoMaximo del servicio.
     * Ejemplo: 1 para servicios individuales, 10 para clases grupales.
     * Campo obligatorio.
     */
    @Column(name = "plazas_totales", nullable = false)
    private Integer plazasTotales;

    /**
     * Número de plazas aún disponibles para reservar.
     * Se decrementa al crear una reserva y se incrementa al cancelar.
     *
     * Validaciones:
     * - plazasDisponibles >= 0 (nunca negativo)
     * - plazasDisponibles <= plazasTotales
     *
     * Campo obligatorio.
     */
    @Column(name = "plazas_disponibles", nullable = false)
    private Integer plazasDisponibles;

    /**
     * Lista de reservas realizadas en esta franja horaria.
     * Relación OneToMany con Reserva.
     */
    @OneToMany(mappedBy = "franja")
    private List<Reserva> reservas = new ArrayList<>();

    /**
     * Constructor de conveniencia para crear franjas horarias.
     * Calcula automáticamente la hora_fin sumando la duración del servicio.
     *
     * @param servicio Servicio asociado
     * @param fecha Fecha de la franja
     * @param horaInicio Hora de inicio
     */
    public FranjaHoraria(Servicio servicio, LocalDate fecha, LocalTime horaInicio) {
        this.servicio = servicio;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaInicio.plusMinutes(servicio.getDuracionMinutos());
        this.plazasTotales = servicio.getAforoMaximo();
        this.plazasDisponibles = servicio.getAforoMaximo();
    }

    /**
     * Verifica si hay plazas disponibles en esta franja.
     *
     * @return true si plazasDisponibles > 0, false en caso contrario
     */
    public boolean tieneDisponibilidad() {
        return this.plazasDisponibles != null && this.plazasDisponibles > 0;
    }

    /**
     * Decrementa el número de plazas disponibles.
     * Se invoca al crear una nueva reserva.
     *
     * @throws IllegalStateException si no hay plazas disponibles
     */
    public void decrementarPlazas() {
        if (!tieneDisponibilidad()) {
            throw new IllegalStateException("No hay plazas disponibles en esta franja");
        }
        this.plazasDisponibles--;
    }

    /**
     * Incrementa el número de plazas disponibles.
     * Se invoca al cancelar una reserva.
     *
     * @throws IllegalStateException si ya están todas las plazas disponibles
     */
    public void incrementarPlazas() {
        if (this.plazasDisponibles >= this.plazasTotales) {
            throw new IllegalStateException("No se pueden incrementar más plazas");
        }
        this.plazasDisponibles++;
    }
}