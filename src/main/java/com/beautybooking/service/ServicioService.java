package com.beautybooking.service;

import com.beautybooking.dto.request.ServicioRequest;
import com.beautybooking.dto.response.ApiResponse;
import com.beautybooking.dto.response.ServicioResponse;
import com.beautybooking.exception.ResourceNotFoundException;
import com.beautybooking.model.Servicio;
import com.beautybooking.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de servicios/tratamientos.
 *
 * Responsabilidades:
 * - Crear, editar y eliminar servicios (ADMIN)
 * - Listar servicios disponibles (público)
 * - Buscar servicios por criterios
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;

    /**
     * Obtiene todos los servicios activos.
     * Usado en el catálogo público.
     *
     * @return Lista de ServicioResponse activos
     */
    @Transactional(readOnly = true)
    public List<ServicioResponse> getServiciosActivos() {
        return servicioRepository.findByActivoOrderByNombreAsc(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los servicios (activos e inactivos).
     * Solo para ADMIN.
     *
     * @return Lista de todos los ServicioResponse
     */
    @Transactional(readOnly = true)
    public List<ServicioResponse> getAllServicios() {
        return servicioRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un servicio por su ID.
     *
     * @param id ID del servicio
     * @return ServicioResponse
     * @throws ResourceNotFoundException si el servicio no existe
     */
    @Transactional(readOnly = true)
    public ServicioResponse getServicioById(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + id));

        return mapToResponse(servicio);
    }

    /**
     * Crea un nuevo servicio (solo ADMIN).
     *
     * @param request Datos del servicio
     * @return ServicioResponse del servicio creado
     */
    @Transactional
    public ServicioResponse createServicio(ServicioRequest request) {
        Servicio servicio = new Servicio();
        servicio.setNombre(request.getNombre());
        servicio.setDescripcion(request.getDescripcion());
        servicio.setDuracionMinutos(request.getDuracionMinutos());
        servicio.setPrecio(request.getPrecio());
        servicio.setAforoMaximo(request.getAforoMaximo());
        servicio.setActivo(request.getActivo() != null ? request.getActivo() : true);

        Servicio savedServicio = servicioRepository.save(servicio);
        return mapToResponse(savedServicio);
    }

    /**
     * Actualiza un servicio existente (solo ADMIN).
     *
     * @param id ID del servicio a actualizar
     * @param request Nuevos datos del servicio
     * @return ServicioResponse actualizado
     * @throws ResourceNotFoundException si el servicio no existe
     */
    @Transactional
    public ServicioResponse updateServicio(Long id, ServicioRequest request) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + id));

        servicio.setNombre(request.getNombre());
        servicio.setDescripcion(request.getDescripcion());
        servicio.setDuracionMinutos(request.getDuracionMinutos());
        servicio.setPrecio(request.getPrecio());
        servicio.setAforoMaximo(request.getAforoMaximo());
        servicio.setActivo(request.getActivo());

        Servicio updatedServicio = servicioRepository.save(servicio);
        return mapToResponse(updatedServicio);
    }

    public ServicioResponse toggleServicioActivo(Long id, Boolean activo) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        servicio.setActivo(activo);
        Servicio updated = servicioRepository.save(servicio);
        return mapToResponse(updated);
    }

    /**
     * Elimina un servicio (solo ADMIN).
     * En realidad solo lo desactiva (soft delete).
     *
     * @param id ID del servicio a eliminar
     * @return ApiResponse confirmando la eliminación
     * @throws ResourceNotFoundException si el servicio no existe
     */
    @Transactional
    public ApiResponse deleteServicio(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + id));

        // Soft delete: solo desactivar
        servicio.setActivo(false);
        servicioRepository.save(servicio);

        return ApiResponse.success("Servicio eliminado correctamente");
    }

    /**
     * Busca servicios por nombre (búsqueda parcial).
     *
     * @param nombre Palabra clave a buscar
     * @return Lista de ServicioResponse que coinciden
     */
    @Transactional(readOnly = true)
    public List<ServicioResponse> buscarServiciosPorNombre(String nombre) {
        return servicioRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convierte entidad Servicio a DTO ServicioResponse.
     *
     * @param servicio Entidad Servicio
     * @return ServicioResponse
     */
    private ServicioResponse mapToResponse(Servicio servicio) {
        return new ServicioResponse(
                servicio.getId(),
                servicio.getNombre(),
                servicio.getDescripcion(),
                servicio.getDuracionMinutos(),
                servicio.getPrecio(),
                servicio.getAforoMaximo(),
                servicio.getActivo(),
                servicio.getCreadoEn()
        );
    }
}