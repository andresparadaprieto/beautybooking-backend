package com.beautybooking.repository;

import com.beautybooking.model.FranjaHoraria;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad FranjaHoraria.
 *
 * Gestiona franjas horarias disponibles para reservas con método con bloqueo pesimista para evitar race conditions
 * al decrementar plazas en reservas concurrentes.
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Repository
public interface FranjaHorariaRepository extends JpaRepository<FranjaHoraria, Long> {

    /**
     * Busca franjas horarias para un servicio en una fecha específica.
     *
     * @param servicioId ID del servicio
     * @param fecha Fecha a consultar
     * @return Lista de franjas para ese servicio y fecha
     */
    @Query("SELECT f FROM FranjaHoraria f WHERE f.servicio.id = :servicioId AND f.fecha = :fecha")
    List<FranjaHoraria> findByServicioIdAndFecha(
            @Param("servicioId") Long servicioId,
            @Param("fecha") LocalDate fecha
    );

    /**
     * Busca franjas disponibles (con plazas > 0) para un servicio en una fecha.
     * Solo devuelve franjas donde aún se puede reservar.
     *
     * @param servicioId ID del servicio
     * @param fecha Fecha a consultar
     * @return Lista de franjas con disponibilidad
     */
    @Query("SELECT f FROM FranjaHoraria f WHERE f.servicio.id = :servicioId " +
            "AND f.fecha = :fecha AND f.plazasDisponibles > 0")
    List<FranjaHoraria> findDisponiblesByServicioIdAndFecha(
            @Param("servicioId") Long servicioId,
            @Param("fecha") LocalDate fecha
    );

    /**
     * Busca franjas en un rango de fechas para un servicio.
     * Útil para mostrar disponibilidad semanal o mensual.
     *
     * @param servicioId ID del servicio
     * @param fechaInicio Fecha inicio del rango
     * @param fechaFin Fecha fin del rango
     * @return Lista de franjas en ese rango
     */
    @Query("SELECT f FROM FranjaHoraria f WHERE f.servicio.id = :servicioId " +
            "AND f.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<FranjaHoraria> findByServicioIdAndFechaBetween(
            @Param("servicioId") Long servicioId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    /**
     * Busca una franja específica con BLOQUEO PESIMISTA (PESSIMISTIC_WRITE).
     *
     * Este método bloquea el registro en la base de datos hasta que termine
     * la transacción, evitando que dos usuarios reserven la última plaza
     * simultáneamente (race condition).
     *
     * Se usa en ReservaService al crear una reserva.
     *
     * Equivale a: SELECT ... FOR UPDATE en SQL
     *
     * @param id ID de la franja
     * @return Optional<FranjaHoraria> bloqueada para escritura
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM FranjaHoraria f WHERE f.id = :id")
    Optional<FranjaHoraria> findByIdWithLock(@Param("id") Long id);

    /**
     * Verifica si existe una franja con los mismos parámetros.
     * Evita duplicados al crear franjas.
     *
     * @param servicioId ID del servicio
     * @param fecha Fecha
     * @param horaInicio Hora de inicio
     * @return true si ya existe, false si no
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FranjaHoraria f " +
            "WHERE f.servicio.id = :servicioId AND f.fecha = :fecha AND f.horaInicio = :horaInicio")
    boolean existsByServicioIdAndFechaAndHoraInicio(
            @Param("servicioId") Long servicioId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio
    );


    /**
     * Lista todas las franjas de un servicio ordenadas por fecha y hora.
     * Para panel de admin - gestión de franjas por servicio.
     *
     * @param servicioId ID del servicio
     * @return Lista de franjas ordenadas cronológicamente
     */
    List<FranjaHoraria> findByServicioIdOrderByFechaAscHoraInicioAsc(Long servicioId);

    /**
     * Lista todas las franjas en un rango de fechas ordenadas.
     * Para vista calendario admin - ver todas las franjas del sistema.
     *
     * @param desde Fecha inicial del rango
     * @param hasta Fecha final del rango
     * @return Lista de franjas en el rango ordenadas cronológicamente
     */
    List<FranjaHoraria> findByFechaBetweenOrderByFechaAscHoraInicioAsc(LocalDate desde, LocalDate hasta);

    /**
     * Verifica si existe una franja duplicada excluyendo la franja actual.
     * Usado al ACTUALIZAR una franja para evitar duplicados con otras franjas.
     *
     * @param servicioId ID del servicio
     * @param fecha Fecha
     * @param horaInicio Hora de inicio
     * @param id ID de la franja actual (a excluir de la búsqueda)
     * @return true si existe otra franja con esos datos, false si no
     */
    boolean existsByServicioIdAndFechaAndHoraInicioAndIdNot(
            Long servicioId, LocalDate fecha, LocalTime horaInicio, Long id
    );

    /**
     * Elimina franjas antiguas (antes de una fecha).
     * Útil para limpiar franjas pasadas periódicamente.
     *
     * @param fecha Fecha límite (eliminar anteriores a esta)
     */
    void deleteByFechaBefore(LocalDate fecha);
}