package com.beautybooking.controller;

import com.beautybooking.dto.request.LoginRequest;
import com.beautybooking.dto.request.RegisterRequest;
import com.beautybooking.dto.response.AuthResponse;
import com.beautybooking.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
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
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}