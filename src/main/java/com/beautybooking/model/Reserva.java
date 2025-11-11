package com.beautybooking.model;

import com.beautybooking.model.enums.EstadoReserva;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entidad Reserva - Representa una cita/reserva realizada por un usuario.
 *
 * Mapea la tabla 'reservas' en la base de datos.
 * Es la entidad central del sistema, vincula usuario, servicio y franja horaria.
 *
 * Ciclo de vida típico:
 * 1. Cliente crea reserva → estado PENDIENTE
 * 2. Sistema/Admin confirma → estado CONFIRMADA
 * 3. Servicio prestado → estado COMPLETADA
 * 4. En cualquier momento puede pasar a CANCELADA
 *
 * Relaciones:
 * - Pertenece a un Usuario (ManyToOne)
 * - Pertenece a un Servicio (ManyToOne)
 * - Pertenece a una FranjaHoraria (ManyToOne)
 *
 * Reglas de negocio:
 * - Un usuario no puede tener dos reservas solapadas en el tiempo
 * - Al crear reserva, se decrementa plazasDisponibles de la franja
 * - Al cancelar reserva, se incrementa plazasDisponibles de la franja
 * - Solo se pueden reservar franjas entre las 07:00 y 22:00
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "reservas",
        indexes = {
                @Index(name = "idx_usuario_fecha", columnList = "usuario_id, fecha"),
                @Index(name = "idx_franja", columnList = "franja_id"),
                @Index(name = "idx_estado", columnList = "estado"),
                @Index(name = "idx_fecha_hora", columnList = "fecha, hora_inicio")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    /**
     * Identificador único de la reserva.
     * Generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario que realizó la reserva.
     * Relación ManyToOne - un usuario puede tener muchas reservas.
     * Campo obligatorio (optional = false).
     * ON DELETE RESTRICT: no se puede eliminar un usuario con reservas activas.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Servicio reservado.
     * Relación ManyToOne - un servicio puede estar en muchas reservas.
     * Se desnormaliza de la franja para consultas más eficientes.
     * Campo obligatorio (optional = false).
     * ON DELETE RESTRICT: no se puede eliminar un servicio con reservas activas.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    /**
     * Franja horaria específica de la reserva.
     * Relación ManyToOne - una franja puede tener muchas reservas.
     * Campo obligatorio para controlar el aforo.
     * ON DELETE SET NULL: si se elimina la franja, la reserva mantiene fecha/hora.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "franja_id")
    private FranjaHoraria franja;

    /**
     * Fecha de la reserva (desnormalizada de franja).
     * Se duplica para facilitar consultas y reportes.
     * Formato: YYYY-MM-DD
     * Campo obligatorio.
     */
    @Column(nullable = false)
    private LocalDate fecha;

    /**
     * Hora de inicio de la reserva (desnormalizada de franja).
     * Formato: HH:mm:ss
     * Campo obligatorio.
     */
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    /**
     * Hora de fin de la reserva (desnormalizada de franja).
     * Formato: HH:mm:ss
     * Campo obligatorio.
     */
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    /**
     * Estado actual de la reserva.
     * Valores posibles: PENDIENTE, CONFIRMADA, COMPLETADA, CANCELADA
     * Por defecto: PENDIENTE
     * Campo obligatorio.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    /**
     * Precio final cobrado por esta reserva.
     * Puede diferir del precio del servicio si hay descuentos/promociones.
     * Por defecto toma el precio del servicio al momento de la reserva.
     * Precisión: 7 dígitos totales, 2 decimales.
     */
    @Column(name = "precio_final", precision = 7, scale = 2)
    private BigDecimal precioFinal;

    /**
     * Notas adicionales de la reserva (opcional).
     * Permite al cliente añadir comentarios o al admin añadir observaciones.
     */
    @Column(columnDefinition = "TEXT")
    private String notas;

    /**
     * Fecha y hora de creación de la reserva.
     * Se establece automáticamente al crear el registro.
     * Útil para auditoría y estadísticas.
     */
    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    /**
     * Verifica si la reserva puede ser cancelada.
     * Solo se pueden cancelar reservas en estado PENDIENTE o CONFIRMADA.
     *
     * @return true si se puede cancelar, false en caso contrario
     */
    public boolean esCancelable() {
        return this.estado == EstadoReserva.PENDIENTE ||
                this.estado == EstadoReserva.CONFIRMADA;
    }

    /**
     * Cancela la reserva cambiando su estado a CANCELADA.
     * También libera la plaza en la franja horaria asociada.
     *
     * @throws IllegalStateException si la reserva no es cancelable
     */
    public void cancelar() {
        if (!esCancelable()) {
            throw new IllegalStateException(
                    "No se puede cancelar una reserva en estado " + this.estado
            );
        }
        this.estado = EstadoReserva.CANCELADA;

        // Liberar plaza en la franja si existe
        if (this.franja != null) {
            this.franja.incrementarPlazas();
        }
    }

    /**
     * Confirma la reserva cambiando su estado a CONFIRMADA.
     *
     * @throws IllegalStateException si la reserva no está PENDIENTE
     */
    public void confirmar() {
        if (this.estado != EstadoReserva.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo se pueden confirmar reservas en estado PENDIENTE"
            );
        }
        this.estado = EstadoReserva.CONFIRMADA;
    }

    /**
     * Marca la reserva como completada.
     * Se invoca cuando el servicio ha sido prestado.
     *
     * @throws IllegalStateException si la reserva no está CONFIRMADA
     */
    public void completar() {
        if (this.estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException(
                    "Solo se pueden completar reservas en estado CONFIRMADA"
            );
        }
        this.estado = EstadoReserva.COMPLETADA;
    }
}