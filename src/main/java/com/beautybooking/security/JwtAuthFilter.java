package com.beautybooking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticación JWT para Spring Security.
 *
 * Este filtro se ejecuta en CADA petición HTTP antes de llegar al controller.
 *
 * Flujo:
 * 1. Extrae el token del header "Authorization: Bearer {token}"
 * 2. Valida el token JWT
 * 3. Extrae el email del usuario del token
 * 4. Carga el usuario desde la base de datos
 * 5. Establece la autenticación en el SecurityContext
 * 6. Permite que la petición continúe al controller
 *
 * Si el token es inválido o no existe, la petición continúa pero sin autenticación
 * (rutas públicas funcionan, rutas protegidas devuelven 401).
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Procesa cada petición HTTP para validar el token JWT.
     *
     * @param request Petición HTTP
     * @param response Respuesta HTTP
     * @param filterChain Cadena de filtros de Spring Security
     * @throws ServletException Si ocurre un error en el filtro
     * @throws IOException Si ocurre un error de I/O
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Extraer el header Authorization
        final String authHeader = request.getHeader("Authorization");

        String jwt = null;
        String userEmail = null;

        // Verificar que el header existe y tiene el formato correcto
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Extraer el token (remover "Bearer " del inicio)
            jwt = authHeader.substring(7);

            try {
                // Extraer email del usuario del token
                userEmail = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // Token malformado o inválido - continuar sin autenticar
                logger.warn("Error al extraer usuario del token JWT: " + e.getMessage());
            }
        }

        // Si tenemos email y el usuario NO está ya autenticado en el contexto
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Cargar usuario desde la base de datos
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Validar que el token es válido para este usuario
            if (jwtUtil.validateToken(jwt, userDetails)) {

                // Crear objeto de autenticación de Spring Security
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Añadir detalles de la petición HTTP
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Establecer la autenticación en el SecurityContext
                // A partir de aquí, el usuario está autenticado para esta petición
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continuar con la cadena de filtros
        // La petición pasa al siguiente filtro o al controller
        filterChain.doFilter(request, response);
    }
}