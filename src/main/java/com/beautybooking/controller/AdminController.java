package com.beautybooking.controller;

import com.beautybooking.dto.request.FranjaRequest;
import com.beautybooking.dto.request.ReservaRequest;
import com.beautybooking.dto.request.ServicioRequest;
import com.beautybooking.dto.response.*;
import com.beautybooking.exception.BusinessException;
import com.beautybooking.exception.ResourceNotFoundException;
import com.beautybooking.service.FranjaHorariaService;
import com.beautybooking.service.ReservaService;
import com.beautybooking.service.ServicioService;
import com.beautybooking.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller de administración.
 *
 * Endpoints protegidos solo para usuarios con rol ADMIN.
 *
 * Responsabilidades:
 * - Gestión CRUD de servicios
 * - Gestión de franjas horarias
 * - Visualización de todas las reservas
 * - Confirmación/completado de reservas
 * - Gestión de usuarios
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ServicioService servicioService;
    private final FranjaHorariaService franjaService;
    private final ReservaService reservaService;
    private final UsuarioService usuarioService;

    // ==================== GESTIÓN DE SERVICIOS ====================

    /**
     * Crea un nuevo servicio.
     *
     * Endpoint: POST /admin/servicios
     * Acceso: Solo ADMIN
     *
     * @param request Datos del servicio
     * @return ServicioResponse del servicio creado
     */
    @PostMapping("/servicios")
    public ResponseEntity<ServicioResponse> createServicio(
            @Valid @RequestBody ServicioRequest request) {
        ServicioResponse servicio = servicioService.createServicio(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio);
    }

    /**
     * Obtiene todos los servicios (activos e inactivos).
     *
     * Endpoint: GET /admin/servicios
     * Acceso: Solo ADMIN
     *
     * @return Lista de todos los ServicioResponse
     */
    @GetMapping("/servicios")
    public ResponseEntity<List<ServicioResponse>> getAllServicios() {
        List<ServicioResponse> servicios = servicioService.getAllServicios();
        return ResponseEntity.ok(servicios);
    }

    /**
     * Actualiza un servicio existente.
     *
     * Endpoint: PUT /admin/servicios/{id}
     * Acceso: Solo ADMIN
     *
     * @param id ID del servicio
     * @param request Nuevos datos del servicio
     * @return ServicioResponse actualizado
     */
    @PutMapping("/servicios/{id}")
    public ResponseEntity<ServicioResponse> updateServicio(
            @PathVariable Long id,
            @Valid @RequestBody ServicioRequest request) {
        ServicioResponse servicio = servicioService.updateServicio(id, request);
        return ResponseEntity.ok(servicio);
    }

     /**
     * Activa o desactiva un servicio.
     * Endpoint: PATCH /admin/servicios/{id}/activo
     */

    @PatchMapping("/servicios/{id}/activo")
    public ResponseEntity<ServicioResponse> toggleServicioActivo(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request) {

        Boolean activo = request.get("activo");
        if (activo == null) {
            throw new IllegalArgumentException("El campo 'activo' es requerido");
        }

        ServicioResponse servicio = servicioService.toggleServicioActivo(id, activo);
        return ResponseEntity.ok(servicio);
    }

    /**
     * Elimina (desactiva) un servicio.

    /**
     * Elimina (desactiva) un servicio.
     *
     * Endpoint: DELETE /admin/servicios/{id}
     * Acceso: Solo ADMIN
     *
     * @param id ID del servicio
     * @return ApiResponse confirmando la eliminación
     */
    @DeleteMapping("/servicios/{id}")
    public ResponseEntity<ApiResponse> deleteServicio(@PathVariable Long id) {
        ApiResponse response = servicioService.deleteServicio(id);
        return ResponseEntity.ok(response);
    }



    // ==================== GESTIÓN DE FRANJAS ====================

    /**
     * Crea una nueva franja horaria.
     *
     * Endpoint: POST /admin/franjas
     * Acceso: Solo ADMIN
     *
     * @param request Datos de la franja (servicioId, fecha, horaInicio)
     * @return FranjaResponse creada
     */
    @PostMapping("/franjas")
    public ResponseEntity<FranjaResponse> createFranja(
            @Valid @RequestBody FranjaRequest request) {
        FranjaResponse franja = franjaService.createFranja(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(franja);
    }

    /**
     * Elimina una franja horaria.
     * Solo permite eliminar franjas sin reservas.
     *
     * Endpoint: DELETE /admin/franjas/{id}
     * Acceso: Solo ADMIN
     *
     * @param id ID de la franja
     * @return ApiResponse confirmando la eliminación
     */
    @DeleteMapping("/franjas/{id}")
    public ResponseEntity<ApiResponse> deleteFranja(@PathVariable Long id) {
        ApiResponse response = franjaService.deleteFranja(id);
        return ResponseEntity.ok(response);
    }


    /**
     * Lista todas las franjas de un servicio específico.
     *
     * Endpoint: GET /admin/franjas/servicio/{servicioId}
     * Acceso: Solo ADMIN
     *
     * @param servicioId ID del servicio
     * @return Lista de FranjaResponse (todas, no solo disponibles)
     */
    @GetMapping("/franjas/servicio/{servicioId}")
    public ResponseEntity<List<FranjaResponse>> getFranjasByServicio(
            @PathVariable Long servicioId) {
        List<FranjaResponse> franjas = franjaService.getFranjasByServicio(servicioId);
        return ResponseEntity.ok(franjas);
    }

    /**
     * Obtiene una franja específica por ID.
     *
     * Endpoint: GET /admin/franjas/{id}
     * Acceso: Solo ADMIN
     *
     * @param id ID de la franja
     * @return FranjaResponse
     */
    @GetMapping("/franjas/{id}")
    public ResponseEntity<FranjaResponse> getFranjaById(@PathVariable Long id) {
        FranjaResponse franja = franjaService.getFranjaById(id);
        return ResponseEntity.ok(franja);
    }

    /**
     * Actualiza una franja horaria existente.
     *
     * Endpoint: PUT /admin/franjas/{id}
     * Acceso: Solo ADMIN
     *
     * @param id ID de la franja
     * @param request Nuevos datos de la franja
     * @return FranjaResponse actualizada
     */
    @PutMapping("/franjas/{id}")
    public ResponseEntity<FranjaResponse> updateFranja(
            @PathVariable Long id,
            @Valid @RequestBody FranjaRequest request) {
        FranjaResponse franja = franjaService.updateFranja(id, request);
        return ResponseEntity.ok(franja);
    }

    /**
     * Lista todas las franjas entre dos fechas (para calendario admin).
     *
     * Endpoint: GET /admin/franjas?desde=2024-01-01&hasta=2024-01-31
     * Acceso: Solo ADMIN
     *
     * @param desde Fecha inicial
     * @param hasta Fecha final
     * @return Lista de FranjaResponse en el rango
     */
    @GetMapping("/franjas")
    public ResponseEntity<List<FranjaResponse>> getFranjasByRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        List<FranjaResponse> franjas = franjaService.getFranjasByRangoFechas(desde, hasta);
        return ResponseEntity.ok(franjas);
    }

    // ==================== GESTIÓN DE RESERVAS ====================

    /**
     * Obtiene todas las reservas del sistema.
     *
     * Endpoint: GET /admin/reservas
     * Acceso: Solo ADMIN
     *
     * @return Lista de todas las ReservaResponse
     */
    @GetMapping("/reservas")
    public ResponseEntity<List<ReservaResponse>> getAllReservas() {
        List<ReservaResponse> reservas = reservaService.getAllReservas();
        return ResponseEntity.ok(reservas);
    }

    /**
     * Obtiene reservas del día actual.
     * Útil para dashboard: "Citas de hoy".
     *
     * Endpoint: GET /admin/reservas/hoy
     * Acceso: Solo ADMIN
     *
     * @return Lista de ReservaResponse de hoy
     */
    @GetMapping("/reservas/hoy")
    public ResponseEntity<List<ReservaResponse>> getReservasDeHoy() {
        List<ReservaResponse> reservas = reservaService.getReservasDeHoy();
        return ResponseEntity.ok(reservas);
    }

    /**
     * Confirma una reserva (PENDIENTE → CONFIRMADA).
     *
     * Endpoint: PATCH /admin/reservas/{id}/confirmar
     * Acceso: Solo ADMIN
     *
     * @param id ID de la reserva
     * @return ReservaResponse actualizada
     */
    @PatchMapping("/reservas/{id}/confirmar")
    public ResponseEntity<ReservaResponse> confirmarReserva(@PathVariable Long id) {
        ReservaResponse reserva = reservaService.confirmarReserva(id);
        return ResponseEntity.ok(reserva);
    }

    /**
     * Marca una reserva como completada (CONFIRMADA → COMPLETADA).
     *
     * Endpoint: PATCH /admin/reservas/{id}/completar
     * Acceso: Solo ADMIN
     *
     * @param id ID de la reserva
     * @return ReservaResponse actualizada
     */
    @PatchMapping("/reservas/{id}/completar")
    public ResponseEntity<ReservaResponse> completarReserva(@PathVariable Long id) {
        ReservaResponse reserva = reservaService.completarReserva(id);
        return ResponseEntity.ok(reserva);
    }

    /**
     * Cancela una reserva (cualquier usuario, sin validación de propietario).
     * Permite al administrador cancelar reservas de cualquier cliente.
     *
     * Endpoint: DELETE /admin/reservas/{id}/cancelar
     * Acceso: Solo ADMIN
     *
     * @param id ID de la reserva
     * @return ApiResponse confirmando la cancelación
     */
    @DeleteMapping("/reservas/{id}/cancelar")
    public ResponseEntity<ApiResponse> cancelarReservaAdmin(@PathVariable Long id) {
        ApiResponse response = reservaService.cancelarReservaAdmin(id);
        return ResponseEntity.ok(response);
    }


    /**
     * Actualiza una reserva existente
     *
     * Endpoint: PUT /admin/reservas/{id}/editar
     * Acceso: Solo ADMIN
     *
     * @param id ID de la reserva
     * @param request Nuevos datos de la reserva
     * @return ReservaResponse actualizada
     */
    @PutMapping("/reservas/{id}/editar")
    public ResponseEntity<ReservaResponse> editarReserva(
            @PathVariable Long id,
            @Valid @RequestBody  ReservaRequest request) {
        ReservaResponse reserva = reservaService.editarReserva(id, request);
        return ResponseEntity.ok(reserva);
    }


    /**
     * Crea una reserva manual para un cliente específico.
     * Si el cliente no existe, lo crea automáticamente.
     *
     * Endpoint: POST /admin/reservas/manual
     * Acceso: Solo ADMIN
     *
     * @param requestBody Datos de la reserva (franjaId, usuarioEmail, usuarioNombre, notas)
     * @return ReservaResponse con datos de la reserva creada
     * @throws IllegalArgumentException si faltan campos obligatorios
     * @throws ResourceNotFoundException si la franja no existe
     * @throws BusinessException si viola alguna regla de negocio
     */


    @PostMapping("/reservas/manual")
    public ResponseEntity<ReservaResponse> crearReservaManual(
            @Valid @RequestBody Map<String, Object> requestBody) {

        // Extraer datos del body
        String usuarioEmail = (String) requestBody.get("usuarioEmail");
        String usuarioNombre = (String) requestBody.get("usuarioNombre");
        Long franjaId = requestBody.get("franjaId") != null
                ? Long.valueOf(requestBody.get("franjaId").toString())
                : null;
        String notas = (String) requestBody.get("notas");

        // Validaciones
        if (usuarioEmail == null || usuarioEmail.isBlank()) {
            throw new IllegalArgumentException("El campo 'usuarioEmail' es obligatorio");
        }

        if (franjaId == null) {
            throw new IllegalArgumentException("El campo 'franjaId' es obligatorio");
        }

        // Crear ReservaRequest
        ReservaRequest request = new ReservaRequest();
        request.setFranjaId(franjaId);
        request.setNotas(notas);

        // Llamar al servicio con auto-registro
        ReservaResponse reserva = reservaService.createReservaManualConAutoRegistro(
                usuarioEmail,
                usuarioNombre,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(reserva);
    }

    // ==================== GESTIÓN DE USUARIOS ====================

    /**
     * Lista todos los usuarios del sistema.
     *
     * Endpoint: GET /admin/usuarios
     * Acceso: Solo ADMIN
     *
     * @return Lista de UsuarioResponse
     */
    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponse>> getAllUsuarios() {
        List<UsuarioResponse> usuarios = usuarioService.getAllUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Obtiene un usuario por ID.
     *
     * Endpoint: GET /admin/usuarios/{id}
     * Acceso: Solo ADMIN
     *
     * @param id ID del usuario
     * @return UsuarioResponse
     */
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioResponse> getUsuarioById(@PathVariable Long id) {
        UsuarioResponse usuario = usuarioService.getUsuarioById(id);
        return ResponseEntity.ok(usuario);
    }
}