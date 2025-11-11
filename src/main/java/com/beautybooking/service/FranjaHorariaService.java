package com.beautybooking.service;

import com.beautybooking.dto.request.FranjaRequest;
import com.beautybooking.dto.response.ApiResponse;
import com.beautybooking.dto.response.FranjaResponse;
import com.beautybooking.exception.BusinessException;
import com.beautybooking.exception.ResourceNotFoundException;
import com.beautybooking.model.FranjaHoraria;
import com.beautybooking.model.Servicio;
import com.beautybooking.repository.FranjaHorariaRepository;
import com.beautybooking.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de franjas horarias.
 *
 * Responsabilidades:
 * - Crear franjas horarias (ADMIN)
 * - Consultar disponibilidad (público)
 * - Validar horarios permitidos (07:00-22:00)
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Service
@RequiredArgsConstructor
public class FranjaHorariaService {

    private final FranjaHorariaRepository franjaRepository;
    private final ServicioRepository servicioRepository;

    // Horarios permitidos según reglas de negocio
    private static final LocalTime HORA_APERTURA = LocalTime.of(7, 0);
    private static final LocalTime HORA_CIERRE = LocalTime.of(22, 0);

    /**
     * Obtiene franjas disponibles para un servicio en una fecha.
     * Solo muestra franjas con plazas disponibles > 0.
     *
     * @param servicioId ID del servicio
     * @param fecha Fecha a consultar
     * @return Lista de FranjaResponse disponibles
     */
    @Transactional(readOnly = true)
    public List<FranjaResponse> getFranjasDisponibles(Long servicioId, LocalDate fecha) {
        return franjaRepository.findDisponiblesByServicioIdAndFecha(servicioId, fecha).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las franjas de un servicio en una fecha.
     * Incluye franjas sin disponibilidad.
     *
     * @param servicioId ID del servicio
     * @param fecha Fecha a consultar
     * @return Lista de todas las FranjaResponse
     */
    @Transactional(readOnly = true)
    public List<FranjaResponse> getFranjasPorServicioYFecha(Long servicioId, LocalDate fecha) {
        return franjaRepository.findByServicioIdAndFecha(servicioId, fecha).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Crea una nueva franja horaria (solo ADMIN).
     *
     * Validaciones:
     * - Servicio debe existir
     * - Horario debe estar entre 07:00 y 22:00
     * - No puede duplicar franjas (mismo servicio, fecha y hora)
     *
     * @param request Datos de la franja
     * @return FranjaResponse creada
     * @throws ResourceNotFoundException si el servicio no existe
     * @throws BusinessException si viola reglas de negocio
     */
    @Transactional
    public FranjaResponse createFranja(FranjaRequest request) {

        // Validar que el servicio existe
        Servicio servicio = servicioRepository.findById(request.getServicioId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Servicio no encontrado con ID: " + request.getServicioId()
                ));

        // Validar horario permitido (07:00 - 22:00)
        if (request.getHoraInicio().isBefore(HORA_APERTURA) ||
                request.getHoraInicio().isAfter(HORA_CIERRE)) {
            throw new BusinessException(
                    "El horario debe estar entre las 07:00 y las 22:00. " +
                            "Hora solicitada: " + request.getHoraInicio()
            );
        }

        // Calcular hora de fin basada en la duración del servicio
        LocalTime horaFin = request.getHoraInicio().plusMinutes(servicio.getDuracionMinutos());

        // Validar que la hora de fin no supere el horario de cierre
        if (horaFin.isAfter(HORA_CIERRE)) {
            throw new BusinessException(
                    "La franja terminaría a las " + horaFin + ", después del horario de cierre (22:00)"
            );
        }

        // Validar que no exista franja duplicada
        if (franjaRepository.existsByServicioIdAndFechaAndHoraInicio(
                request.getServicioId(), request.getFecha(), request.getHoraInicio())) {
            throw new BusinessException(
                    "Ya existe una franja para este servicio en la fecha y hora indicadas"
            );
        }

        // Crear franja horaria
        FranjaHoraria franja = new FranjaHoraria();
        franja.setServicio(servicio);
        franja.setFecha(request.getFecha());
        franja.setHoraInicio(request.getHoraInicio());
        franja.setHoraFin(horaFin);
        franja.setPlazasTotales(servicio.getAforoMaximo());

        // Permitir especificar plazas diferentes al aforo del servicio
        if (request.getPlazasDisponibles() != null) {
            franja.setPlazasDisponibles(request.getPlazasDisponibles());
        } else {
            franja.setPlazasDisponibles(servicio.getAforoMaximo());
        }

        FranjaHoraria savedFranja = franjaRepository.save(franja);
        return mapToResponse(savedFranja);
    }

    /**
     * Elimina una franja horaria (solo ADMIN).
     * Solo permite eliminar franjas sin reservas.
     *
     * @param id ID de la franja
     * @return ApiResponse confirmando la eliminación
     * @throws ResourceNotFoundException si la franja no existe
     * @throws BusinessException si la franja tiene reservas
     */
    @Transactional
    public ApiResponse deleteFranja(Long id) {
        FranjaHoraria franja = franjaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Franja no encontrada con ID: " + id));

        // Verificar que no tenga reservas
        if (franja.getPlazasDisponibles() < franja.getPlazasTotales()) {
            throw new BusinessException(
                    "No se puede eliminar una franja con reservas activas"
            );
        }

        franjaRepository.delete(franja);
        return ApiResponse.success("Franja horaria eliminada correctamente");
    }


    /**
     * Obtiene todas las franjas de un servicio (para gestión admin).
     *
     * @param servicioId ID del servicio
     * @return Lista de FranjaResponse (todas, no solo disponibles)
     */
    @Transactional(readOnly = true)
    public List<FranjaResponse> getFranjasByServicio(Long servicioId) {
        // Verificar que el servicio existe
        if (!servicioRepository.existsById(servicioId)) {
            throw new ResourceNotFoundException("Servicio no encontrado con ID: " + servicioId);
        }

        return franjaRepository.findByServicioIdOrderByFechaAscHoraInicioAsc(servicioId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una franja por ID (para vista detalle/edición).
     *
     * @param id ID de la franja
     * @return FranjaResponse
     * @throws ResourceNotFoundException si la franja no existe
     */
    @Transactional(readOnly = true)
    public FranjaResponse getFranjaById(Long id) {
        FranjaHoraria franja = franjaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Franja no encontrada con ID: " + id));
        return mapToResponse(franja);
    }

    /**
     * Obtiene todas las franjas en un rango de fechas (para calendario admin).
     *
     * @param desde Fecha inicial
     * @param hasta Fecha final
     * @return Lista de FranjaResponse
     */
    @Transactional(readOnly = true)
    public List<FranjaResponse> getFranjasByRangoFechas(LocalDate desde, LocalDate hasta) {
        if (desde.isAfter(hasta)) {
            throw new BusinessException("La fecha inicial debe ser anterior a la fecha final");
        }

        return franjaRepository.findByFechaBetweenOrderByFechaAscHoraInicioAsc(desde, hasta).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza una franja horaria existente (solo ADMIN).
     *
     * Validaciones:
     * - No permite cambiar fecha/hora si tiene reservas activas
     * - Permite ajustar plazas disponibles
     * - Valida horarios permitidos
     *
     * @param id ID de la franja
     * @param request Nuevos datos
     * @return FranjaResponse actualizada
     * @throws ResourceNotFoundException si la franja no existe
     * @throws BusinessException si viola reglas de negocio
     */
    @Transactional
    public FranjaResponse updateFranja(Long id, FranjaRequest request) {
        FranjaHoraria franja = franjaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Franja no encontrada con ID: " + id));

        // Verificar si tiene reservas activas
        int plazasOcupadas = franja.getPlazasTotales() - franja.getPlazasDisponibles();
        boolean tieneReservas = plazasOcupadas > 0;

        // Si tiene reservas, solo permitir cambiar plazas (no fecha/hora/servicio)
        if (tieneReservas) {
            if (!franja.getFecha().equals(request.getFecha()) ||
                    !franja.getHoraInicio().equals(request.getHoraInicio()) ||
                    !franja.getServicio().getId().equals(request.getServicioId())) {
                throw new BusinessException(
                        "No se puede cambiar el servicio, fecha u hora de una franja con reservas activas (" +
                                plazasOcupadas + " reservas). Solo puedes ajustar las plazas."
                );
            }
        }

        // Validar horario permitido
        if (request.getHoraInicio().isBefore(HORA_APERTURA) ||
                request.getHoraInicio().isAfter(HORA_CIERRE)) {
            throw new BusinessException(
                    "El horario debe estar entre las 07:00 y las 22:00. " +
                            "Hora solicitada: " + request.getHoraInicio()
            );
        }

        // Obtener servicio (puede ser el mismo o uno nuevo)
        Servicio servicio = servicioRepository.findById(request.getServicioId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Servicio no encontrado con ID: " + request.getServicioId()
                ));

        // Calcular hora de fin
        LocalTime horaFin = request.getHoraInicio().plusMinutes(servicio.getDuracionMinutos());

        if (horaFin.isAfter(HORA_CIERRE)) {
            throw new BusinessException(
                    "La franja terminaría a las " + horaFin + ", después del horario de cierre (22:00)"
            );
        }

        // Validar que no se duplique (excluir la misma franja)
        if (!tieneReservas && franjaRepository.existsByServicioIdAndFechaAndHoraInicioAndIdNot(
                request.getServicioId(), request.getFecha(), request.getHoraInicio(), id)) {
            throw new BusinessException(
                    "Ya existe otra franja para este servicio en la fecha y hora indicadas"
            );
        }

        // Actualizar campos
        franja.setServicio(servicio);
        franja.setFecha(request.getFecha());
        franja.setHoraInicio(request.getHoraInicio());
        franja.setHoraFin(horaFin);

        // Actualizar plazas con validación
        if (request.getPlazasDisponibles() != null) {
            if (request.getPlazasDisponibles() < plazasOcupadas) {
                throw new BusinessException(
                        "No se pueden establecer menos plazas disponibles que las ya reservadas. " +
                                "Plazas ocupadas: " + plazasOcupadas + ", intentando establecer: " + request.getPlazasDisponibles()
                );
            }
            franja.setPlazasTotales(request.getPlazasDisponibles());
            franja.setPlazasDisponibles(request.getPlazasDisponibles() - plazasOcupadas);
        } else {
            // Si no se especifican plazas, usar el aforo del servicio
            franja.setPlazasTotales(servicio.getAforoMaximo());
            franja.setPlazasDisponibles(servicio.getAforoMaximo() - plazasOcupadas);
        }

        FranjaHoraria updatedFranja = franjaRepository.save(franja);
        return mapToResponse(updatedFranja);
    }

    /**
     * Convierte entidad FranjaHoraria a DTO FranjaResponse.
     *
     * @param franja Entidad FranjaHoraria
     * @return FranjaResponse
     */
    private FranjaResponse mapToResponse(FranjaHoraria franja) {
        return new FranjaResponse(
                franja.getId(),
                franja.getServicio().getId(),
                franja.getServicio().getNombre(),
                franja.getFecha(),
                franja.getHoraInicio(),
                franja.getHoraFin(),
                franja.getPlazasTotales(),
                franja.getPlazasDisponibles(),
                franja.tieneDisponibilidad()
        );
    }
}