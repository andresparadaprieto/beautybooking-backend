package com.beautybooking.service;

import com.beautybooking.dto.response.UsuarioResponse;
import com.beautybooking.exception.ResourceNotFoundException;
import com.beautybooking.model.Usuario;
import com.beautybooking.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de usuarios.
 *
 * Responsabilidades:
 * - Obtener información de usuarios
 * - Listar usuarios (para admin)
 * - Actualizar perfil de usuario
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id ID del usuario
     * @return UsuarioResponse con datos del usuario
     * @throws ResourceNotFoundException si el usuario no existe
     */
    @Transactional(readOnly = true)
    public UsuarioResponse getUsuarioById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        return mapToResponse(usuario);
    }

    /**
     * Obtiene un usuario por su email.
     *
     * @param email Email del usuario
     * @return UsuarioResponse con datos del usuario
     * @throws ResourceNotFoundException si el usuario no existe
     */
    @Transactional(readOnly = true)
    public UsuarioResponse getUsuarioByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        return mapToResponse(usuario);
    }

    /**
     * Lista todos los usuarios del sistema (solo para ADMIN).
     *
     * @return Lista de UsuarioResponse
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> getAllUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista usuarios activos.
     *
     * @return Lista de UsuarioResponse de usuarios activos
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> getUsuariosActivos() {
        return usuarioRepository.findByActivo(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convierte entidad Usuario a DTO UsuarioResponse.
     * NO incluye el passwordHash por seguridad.
     *
     * @param usuario Entidad Usuario
     * @return UsuarioResponse
     */
    private UsuarioResponse mapToResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol().name(),
                usuario.getTelefono(),
                usuario.getActivo(),
                usuario.getCreadoEn()
        );
    }
}