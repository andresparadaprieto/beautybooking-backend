package com.beautybooking.repository;

import com.beautybooking.model.Reserva;
import com.beautybooking.model.Usuario;
import com.beautybooking.model.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Repositorio para la entidad Reserva.
 *
 * Gestiona las reservas/citas realizadas por los usuarios.
 * Incluye queries especializadas para prevenir solapamientos,
 * consultar reservas por usuario, fecha, estado, etc.
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    /**
     * Busca todas las reservas de un usuario.
     * Útil para mostrar "Mis Reservas" en el frontend.
     *
     * @param usuarioId ID del usuario
     * @return Lista de reservas del usuario
     */
    List<Reserva> findByUsuarioId(Long usuarioId);

    /**
     * Busca reservas de un usuario ordenadas por fecha descendente.
     * Muestra las reservas más recientes primero.
     *
     * @param usuarioId ID del usuario
     * @return Lista ordenada de reservas
     */
    List<Reserva> findByUsuarioIdOrderByFechaDescHoraInicioDesc(Long usuarioId);

    /**
     * Busca reservas por estado.
     * Útil para filtrar reservas PENDIENTES, CONFIRMADAS, etc.
     *
     * @param estado Estado de la reserva
     * @return Lista de reservas con ese estado
     */
    List<Reserva> findByEstado(EstadoReserva estado);

    /**
     * Busca reservas por fecha específica.
     * Útil para ver todas las citas de un día.
     *
     * @param fecha Fecha a consultar
     * @return Lista de reservas en esa fecha
     */
    List<Reserva> findByFecha(LocalDate fecha);

    /**
     * Busca reservas en un rango de fechas.
     * Útil para reportes semanales o mensuales.
     *
     * @param fechaInicio Fecha inicio del rango
     * @param fechaFin Fecha fin del rango
     * @return Lista de reservas en ese rango
     */
    List<Reserva> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Busca reservas activas de un usuario (PENDIENTE o CONFIRMADA).
     * Excluye reservas CANCELADAS y COMPLETADAS.
     *
     * @param usuarioId ID del usuario
     * @param estados Lista de estados a buscar
     * @return Lista de reservas activas
     */
    @Query("SELECT r FROM Reserva r WHERE r.usuario.id = :usuarioId AND r.estado IN :estados")
    List<Reserva> findByUsuarioIdAndEstadoIn(
            @Param("usuarioId") Long usuarioId,
            @Param("estados") List<EstadoReserva> estados
    );

    /**
     * Verifica si existe una reserva que solapa con el horario dado.
     *
     * Previene que un usuario tenga dos reservas al mismo tiempo.
     *
     * Lógica de solapamiento:
     * - Nueva reserva de 10:00 a 11:00
     * - Solapa con existente si:
     *   * Empieza durante otra reserva (10:30 a 11:30)
     *   * Termina durante otra reserva (09:30 a 10:30)
     *   * Engloba otra reserva (09:00 a 12:00)
     *
     * @param usuario Usuario que intenta reservar
     * @param fecha Fecha de la nueva reserva
     * @param horaInicio Hora de inicio
     * @param horaFin Hora de fin
     * @param estados Estados a considerar (normalmente PENDIENTE y CONFIRMADA)
     * @return true si hay solapamiento, false si no
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reserva r " +
            "WHERE r.usuario = :usuario " +
            "AND r.fecha = :fecha " +
            "AND r.estado IN :estados " +
            "AND ((r.horaInicio < :horaFin AND r.horaFin > :horaInicio))")
    boolean existsSolapamiento(
            @Param("usuario") Usuario usuario,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("estados") List<EstadoReserva> estados
    );

    /**
     * Cuenta reservas de un servicio específico.
     * Útil para estadísticas de popularidad de servicios.
     *
     * @param servicioId ID del servicio
     * @return Número de reservas de ese servicio
     */
    long countByServicioId(Long servicioId);

    /**
     * Busca reservas de hoy con estados específicos.
     * Útil para el dashboard del admin: "Citas de hoy".
     *
     * @param fecha Fecha (normalmente LocalDate.now())
     * @param estados Estados a incluir
     * @return Lista de reservas de hoy
     */
    @Query("SELECT r FROM Reserva r WHERE r.fecha = :fecha AND r.estado IN :estados " +
            "ORDER BY r.horaInicio ASC")
    List<Reserva> findReservasDelDia(
            @Param("fecha") LocalDate fecha,
            @Param("estados") List<EstadoReserva> estados
    );

    /**
     * Busca reservas de un usuario en una franja específica.
     * Evita duplicados: un usuario no puede reservar la misma franja dos veces.
     *
     * @param usuarioId ID del usuario
     * @param franjaId ID de la franja
     * @return true si ya tiene reserva en esa franja, false si no
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reserva r " +
            "WHERE r.usuario.id = :usuarioId AND r.franja.id = :franjaId " +
            "AND r.estado IN ('PENDIENTE', 'CONFIRMADA')")
    boolean existsByUsuarioIdAndFranjaId(
            @Param("usuarioId") Long usuarioId,
            @Param("franjaId") Long franjaId
    );
}