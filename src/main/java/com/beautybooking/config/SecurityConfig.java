package com.beautybooking.config;

import com.beautybooking.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad de Spring Security.
 *
 * Define:
 * - Rutas públicas vs protegidas
 * - Configuración JWT (stateless)
 * - CORS habilitado
 * - Password encoder (BCrypt)
 * - Authentication provider
 *
 * Arquitectura:
 * - Sin sesiones (stateless) - cada petición valida el JWT
 * - CORS configurado en CorsConfig separado
 * - Filtro JWT antes del filtro de autenticación estándar
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Configura la cadena de filtros de seguridad.
     *
     * Define qué rutas son públicas y cuáles requieren autenticación.
     *
     * @param http HttpSecurity configurator
     * @return SecurityFilterChain configurada
     * @throws Exception Si hay error en la configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF (no necesario en API stateless con JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Configurar autorización de peticiones
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas - no requieren autenticación
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/servicios/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/franjas/**").permitAll()

                        // H2 Console en desarrollo
                        .requestMatchers("/h2-console/**").permitAll()

                        // Actuator health
                        .requestMatchers("/actuator/health").permitAll()

                        // Rutas de administración - solo ADMIN
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")

                        // Todas las demás rutas requieren autenticación
                        .anyRequest().authenticated()
                )

                // Sin sesiones - API stateless con JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configurar authentication provider
                .authenticationProvider(authenticationProvider())

                // Añadir filtro JWT antes del filtro de autenticación estándar
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // Habilitar frames para H2 Console (solo en dev)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                );

        return http.build();
    }

    /**
     * Bean de PasswordEncoder usando BCrypt.
     *
     * BCrypt es un algoritmo de hashing seguro para contraseñas.
     * - Lento intencionalmente para dificultar ataques de fuerza bruta
     * - Incluye salt automático
     * - Resistente a rainbow tables
     *
     * @return BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura el proveedor de autenticación.
     *
     * Une UserDetailsService y PasswordEncoder para validar credenciales.
     *
     * @return DaoAuthenticationProvider configurado
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Bean de AuthenticationManager.
     *
     * Usado para autenticar usuarios en el login.
     *
     * @param config AuthenticationConfiguration
     * @return AuthenticationManager
     * @throws Exception Si hay error al obtener el manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}