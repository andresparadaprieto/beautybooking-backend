package com.beautybooking.controller;

import com.beautybooking.dto.response.ServicioResponse;
import com.beautybooking.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller de servicios/tratamientos.
 *
 * Endpoints públicos para consultar el catálogo de servicios.
 *
 * Rutas:
 * - GET /servicios - Listar servicios activos
 * - GET /servicios/{id} - Obtener servicio por ID
 * - GET /servicios/buscar?nombre=X - Buscar servicios por nombre
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    /**
     * Lista todos los servicios activos.
     *
     * Endpoint: GET /servicios
     * Acceso: Público
     *
     * @return Lista de ServicioResponse activos
     */
    @GetMapping
    public ResponseEntity<List<ServicioResponse>> getServiciosActivos() {
        List<ServicioResponse> servicios = servicioService.getServiciosActivos();
        return ResponseEntity.ok(servicios);
    }

    /**
     * Obtiene un servicio por su ID.
     *
     * Endpoint: GET /servicios/{id}
     * Acceso: Público
     *
     * @param id ID del servicio
     * @return ServicioResponse
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServicioResponse> getServicioById(@PathVariable Long id) {
        ServicioResponse servicio = servicioService.getServicioById(id);
        return ResponseEntity.ok(servicio);
    }

    /**
     * Busca servicios por nombre (búsqueda parcial).
     *
     * Endpoint: GET /servicios/buscar?nombre=corte
     * Acceso: Público
     *
     * @param nombre Palabra clave a buscar
     * @return Lista de ServicioResponse que coinciden
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ServicioResponse>> buscarServicios(
            @RequestParam String nombre) {
        List<ServicioResponse> servicios = servicioService.buscarServiciosPorNombre(nombre);
        return ResponseEntity.ok(servicios);
    }
}