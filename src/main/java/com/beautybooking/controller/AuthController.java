package com.beautybooking.controller;

import com.beautybooking.dto.request.LoginRequest;
import com.beautybooking.dto.request.RegisterRequest;
import com.beautybooking.dto.response.AuthResponse;
import com.beautybooking.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller de autenticación.
 *
 * Endpoints públicos para registro y login de usuarios.
 *
 * Rutas:
 * - POST /auth/register - Registro de nuevo usuario
 * - POST /auth/login - Login y obtención de token JWT
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.1
 * @since 2025
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * Endpoint: POST /auth/register
     * Acceso: Público
     *
     * @param request Datos del nuevo usuario (nombre, email, password, teléfono)
     * @return AuthResponse con token JWT y datos del usuario
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El email ya está registrado"));

        } catch (Exception e) {
            log.error("Error inesperado en registro de usuario", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor. Contacta al administrador."));
        }
    }

    /**
     * Autentica un usuario (login).
     *
     * Endpoint: POST /auth/login
     * Acceso: Público
     *
     * @param request Credenciales (email y password)
     * @return AuthResponse con token JWT y datos del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("Error inesperado en login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor. Contacta al administrador."));
        }
    }
}