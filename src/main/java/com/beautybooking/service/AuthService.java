package com.beautybooking.service;

import com.beautybooking.dto.request.LoginRequest;
import com.beautybooking.dto.request.RegisterRequest;
import com.beautybooking.dto.response.AuthResponse;
import com.beautybooking.exception.BusinessException;
import com.beautybooking.model.Usuario;
import com.beautybooking.model.enums.RolUsuario;
import com.beautybooking.repository.UsuarioRepository;
import com.beautybooking.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de autenticación y registro de usuarios.
 *
 * Responsabilidades:
 * - Registrar nuevos usuarios (clientes)
 * - Autenticar usuarios (login)
 * - Generar tokens JWT
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * Validaciones:
     * - Email no puede estar duplicado
     * - Password se hashea con BCrypt antes de guardar
     * - Rol por defecto: CLIENTE
     *
     * @param request Datos del nuevo usuario
     * @return AuthResponse con token JWT
     * @throws BusinessException si el email ya existe
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Validar que el email no exista
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El email ya está registrado en el sistema");
        }

        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(RolUsuario.CLIENTE);
        usuario.setTelefono(request.getTelefono());
        usuario.setActivo(true);

        // Guardar en base de datos
        Usuario savedUsuario = usuarioRepository.save(usuario);

        // Generar token JWT
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(savedUsuario.getEmail())
                .password(savedUsuario.getPasswordHash())
                .authorities(savedUsuario.getRol().name())
                .build();

        String token = jwtUtil.generateToken(userDetails);

        // Retornar respuesta con token
        return new AuthResponse(
                token,
                savedUsuario.getId(),
                savedUsuario.getEmail(),
                savedUsuario.getNombre(),
                savedUsuario.getRol().name()
        );
    }

    /**
     * Autentica un usuario (login).
     *
     * Proceso:
     * 1. Validar credenciales con AuthenticationManager
     * 2. Si son correctas, generar token JWT
     * 3. Retornar token y datos del usuario
     *
     * @param request Credenciales (email y password)
     * @return AuthResponse con token JWT
     * @throws BadCredentialsException si las credenciales son incorrectas
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        // Autenticar con Spring Security
        // Si las credenciales son incorrectas, lanza BadCredentialsException
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Si llegamos aquí, las credenciales son correctas
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Buscar usuario completo para obtener datos adicionales
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        // Generar token JWT
        String token = jwtUtil.generateToken(userDetails);

        // Retornar respuesta con token
        return new AuthResponse(
                token,
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getRol().name()
        );
    }
}