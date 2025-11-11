package com.beautybooking.controller;

import com.beautybooking.dto.response.FranjaResponse;
import com.beautybooking.service.FranjaHorariaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller de franjas horarias.
 *
 * Endpoints públicos para consultar disponibilidad de horarios.
 *
 * Rutas:
 * - GET /franjas/disponibles?servicioId=X&fecha=YYYY-MM-DD
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/franjas")
@RequiredArgsConstructor
public class FranjaController {

    private final FranjaHorariaService franjaService;

    /**
     * Obtiene franjas disponibles para un servicio en una fecha.
     * Solo muestra franjas con plazas disponibles.
     *
     * Endpoint: GET /franjas/disponibles?servicioId=1&fecha=2025-01-15
     * Acceso: Público
     *
     * @param servicioId ID del servicio
     * @param fecha Fecha a consultar (formato: YYYY-MM-DD)
     * @return Lista de FranjaResponse disponibles
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<FranjaResponse>> getFranjasDisponibles(
            @RequestParam Long servicioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        List<FranjaResponse> franjas = franjaService.getFranjasDisponibles(servicioId, fecha);
        return ResponseEntity.ok(franjas);
    }

    /**
     * Obtiene todas las franjas (con y sin disponibilidad).
     *
     * Endpoint: GET /franjas?servicioId=1&fecha=2025-01-15
     * Acceso: Público
     *
     * @param servicioId ID del servicio
     * @param fecha Fecha a consultar
     * @return Lista de todas las FranjaResponse
     */
    @GetMapping
    public ResponseEntity<List<FranjaResponse>> getFranjas(
            @RequestParam Long servicioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        List<FranjaResponse> franjas = franjaService.getFranjasPorServicioYFecha(servicioId, fecha);
        return ResponseEntity.ok(franjas);
    }
}