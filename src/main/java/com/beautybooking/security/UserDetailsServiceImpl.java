package com.beautybooking.security;

import com.beautybooking.model.Usuario;
import com.beautybooking.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Implementación de UserDetailsService de Spring Security.
 *
 * Este servicio es usado por Spring Security para cargar datos del usuario
 * durante el proceso de autenticación.
 *
 * Responsabilidades:
 * - Buscar usuario por email (username)
 * - Verificar que el usuario existe y está activo
 * - Convertir Usuario entity a UserDetails de Spring Security
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Carga un usuario por su email (username).
     *
     * Este método es invocado por Spring Security durante:
     * 1. Login (autenticación con credenciales)
     * 2. Validación de token JWT (verificar permisos)
     *
     * @param email Email del usuario
     * @return UserDetails con información del usuario y sus autoridades
     * @throws UsernameNotFoundException si el usuario no existe o está inactivo
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // Buscar usuario por email y verificar que esté activo
        Usuario usuario = usuarioRepository.findByEmailAndActivo(email, true)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado o inactivo: " + email
                ));

        // Convertir rol del usuario a autoridad de Spring Security
        // Ejemplo: RolUsuario.ADMIN → autoridad "ADMIN"
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(usuario.getRol().name());

        // Crear UserDetails con email, password y autoridades
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPasswordHash())
                .authorities(Collections.singletonList(authority))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!usuario.getActivo())
                .build();
    }
}