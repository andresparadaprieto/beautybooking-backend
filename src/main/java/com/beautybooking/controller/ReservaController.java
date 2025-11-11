package com.beautybooking.controller;

import com.beautybooking.dto.request.ReservaRequest;
import com.beautybooking.dto.response.ApiResponse;
import com.beautybooking.dto.response.ReservaResponse;
import com.beautybooking.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller de reservas.
 *
 * Endpoints protegidos para gestionar reservas de usuarios autenticados.
 *
 * Rutas:
 * - POST /reservas - Crear nueva reserva
 * - GET /reservas/mis - Ver mis reservas
 * - GET /reservas/{id} - Ver detalle de reserva
 * - DELETE /reservas/{id} - Cancelar reserva
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    /**
     * Crea una nueva reserva.
     *
     * Endpoint: POST /reservas
     * Acceso: Usuario autenticado (CLIENTE o ADMIN)
     *
     * El email del usuario se extrae del token JWT (Authentication).
     *
     * @param request Datos de la reserva (franjaId, notas)
     * @param authentication Usuario autenticado
     * @return ReservaResponse con datos de la reserva creada
     */
    @PostMapping
    public ResponseEntity<ReservaResponse> createReserva(
            @Valid @RequestBody ReservaRequest request,
            Authentication authentication) {

        String usuarioEmail = authentication.getName();
        ReservaResponse reserva = reservaService.createReserva(usuarioEmail, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(reserva);
    }

    /**
     * Obtiene todas las reservas del usuario autenticado.
     *
     * Endpoint: GET /reservas/mis
     * Acceso: Usuario autenticado
     *
     * @param authentication Usuario autenticado
     * @return Lista de ReservaResponse del usuario
     */
    @GetMapping("/mis")
    public ResponseEntity<List<ReservaResponse>> getMisReservas(Authentication authentication) {
        String usuarioEmail = authentication.getName();
        List<ReservaResponse> reservas = reservaService.getMisReservas(usuarioEmail);
        return ResponseEntity.ok(reservas);
    }

    /**
     * Obtiene detalle de una reserva específica.
     * Verifica que la reserva pertenezca al usuario autenticado.
     *
     * Endpoint: GET /reservas/{id}
     * Acceso: Usuario autenticado (propietario de la reserva)
     *
     * @param id ID de la reserva
     * @param authentication Usuario autenticado
     * @return ReservaResponse
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> getReservaById(
            @PathVariable Long id,
            Authentication authentication) {

        String usuarioEmail = authentication.getName();
        ReservaResponse reserva = reservaService.getReservaById(id, usuarioEmail);
        return ResponseEntity.ok(reserva);
    }

    /**
     * Cancela una reserva.
     * Libera la plaza en la franja horaria.
     *
     * Endpoint: DELETE /reservas/{id}
     * Acceso: Usuario autenticado (propietario de la reserva)
     *
     * @param id ID de la reserva
     * @param authentication Usuario autenticado
     * @return ApiResponse confirmando la cancelación
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> cancelarReserva(
            @PathVariable Long id,
            Authentication authentication) {

        String usuarioEmail = authentication.getName();
        ApiResponse response = reservaService.cancelarReserva(id, usuarioEmail);
        return ResponseEntity.ok(response);
    }

}