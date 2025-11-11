package com.beautybooking.config;

import com.beautybooking.model.FranjaHoraria;
import com.beautybooking.model.Servicio;
import com.beautybooking.model.Usuario;
import com.beautybooking.model.enums.RolUsuario;
import com.beautybooking.repository.FranjaHorariaRepository;
import com.beautybooking.repository.ServicioRepository;
import com.beautybooking.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cargador de datos iniciales para desarrollo y demostración, para desarrollo.
 *
 * Se ejecuta automáticamente al arrancar la aplicación.
 * Solo se ejecuta si NO existe el perfil "test" (para evitar interferir con tests).
 *
 * Responsabilidades:
 * - Crear usuario administrador por defecto
 * - Crear usuarios de prueba
 * - Crear servicios de ejemplo
 * - Crear franjas horarias para los próximos 7 días
 *
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Component
@Profile("!test") // No ejecutar en tests
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final ServicioRepository servicioRepository;
    private final FranjaHorariaRepository franjaRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Método que se ejecuta al iniciar la aplicación.
     *
     * @param args Argumentos de la aplicación
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("=== Iniciando carga de datos iniciales ===");

        // Solo cargar datos si la BD está vacía
        if (usuarioRepository.count() == 0) {
            log.info("Base de datos vacía. Cargando datos de ejemplo...");

            cargarUsuarios();
            cargarServicios();
            cargarFranjasHorarias();

            log.info("=== Carga de datos completada exitosamente ===");
        } else {
            log.info("La base de datos ya contiene datos. Omitiendo carga inicial.");
        }
    }

    /**
     * Carga usuarios de ejemplo en la base de datos.
     *
     * Crea:
     * - 1 administrador (admin@beautybooking.com / admin123)
     * - 2 clientes de prueba
     */
    private void cargarUsuarios() {
        log.info("Cargando usuarios de ejemplo...");

        // Usuario administrador
        Usuario admin = new Usuario();
        admin.setNombre("Administrador BeautyBooking");
        admin.setEmail("admin@beautybooking.com");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setRol(RolUsuario.ADMIN);
        admin.setTelefono("+34600000000");
        admin.setActivo(true);
        usuarioRepository.save(admin);
        log.info("✓ Usuario ADMIN creado: admin@beautybooking.com / admin123");

        // Cliente de prueba 1
        Usuario cliente1 = new Usuario();
        cliente1.setNombre("María García López");
        cliente1.setEmail("maria.garcia@example.com");
        cliente1.setPasswordHash(passwordEncoder.encode("password123"));
        cliente1.setRol(RolUsuario.CLIENTE);
        cliente1.setTelefono("+34611111111");
        cliente1.setActivo(true);
        usuarioRepository.save(cliente1);
        log.info("✓ Usuario CLIENTE creado: maria.garcia@example.com / password123");

        // Cliente de prueba 2
        Usuario cliente2 = new Usuario();
        cliente2.setNombre("Carlos Rodríguez Martín");
        cliente2.setEmail("carlos.rodriguez@example.com");
        cliente2.setPasswordHash(passwordEncoder.encode("password123"));
        cliente2.setRol(RolUsuario.CLIENTE);
        cliente2.setTelefono("+34622222222");
        cliente2.setActivo(true);
        usuarioRepository.save(cliente2);
        log.info("✓ Usuario CLIENTE creado: carlos.rodriguez@example.com / password123");
    }

    /**
     * Carga servicios de ejemplo en la base de datos.
     *
     * Crea servicios típicos de un centro de estética.
     */
    private void cargarServicios() {
        log.info("Cargando servicios de ejemplo...");

        List<Servicio> servicios = new ArrayList<>();

        // Servicio 1: Corte y Peinado
        Servicio servicio1 = new Servicio();
        servicio1.setNombre("Corte y Peinado");
        servicio1.setDescripcion("Corte de pelo profesional con lavado, peinado y acabado personalizado. " +
                "Incluye asesoramiento sobre el estilo más adecuado según tu tipo de cabello y rostro.");
        servicio1.setDuracionMinutos(60);
        servicio1.setPrecio(new BigDecimal("25.00"));
        servicio1.setAforoMaximo(1);
        servicio1.setActivo(true);
        servicios.add(servicio1);

        // Servicio 2: Manicura
        Servicio servicio2 = new Servicio();
        servicio2.setNombre("Manicura Completa");
        servicio2.setDescripcion("Manicura profesional que incluye limado, cutículas, exfoliación, " +
                "hidratación y esmaltado. Acabado perfecto para lucir unas manos impecables.");
        servicio2.setDuracionMinutos(45);
        servicio2.setPrecio(new BigDecimal("18.00"));
        servicio2.setAforoMaximo(2);
        servicio2.setActivo(true);
        servicios.add(servicio2);

        // Servicio 3: Tratamiento Facial
        Servicio servicio3 = new Servicio();
        servicio3.setNombre("Tratamiento Facial Hidratante");
        servicio3.setDescripcion("Tratamiento facial completo con limpieza profunda, exfoliación, " +
                "mascarilla hidratante y masaje facial relajante. Ideal para todo tipo de pieles.");
        servicio3.setDuracionMinutos(90);
        servicio3.setPrecio(new BigDecimal("45.00"));
        servicio3.setAforoMaximo(1);
        servicio3.setActivo(true);
        servicios.add(servicio3);

        // Servicio 4: Masaje Relajante
        Servicio servicio4 = new Servicio();
        servicio4.setNombre("Masaje Relajante");
        servicio4.setDescripcion("Masaje corporal completo de 60 minutos con técnicas de relajación. " +
                "Perfecto para aliviar tensiones musculares y estrés del día a día.");
        servicio4.setDuracionMinutos(60);
        servicio4.setPrecio(new BigDecimal("40.00"));
        servicio4.setAforoMaximo(1);
        servicio4.setActivo(true);
        servicios.add(servicio4);

        // Servicio 5: Depilación
        Servicio servicio5 = new Servicio();
        servicio5.setNombre("Depilación con Cera");
        servicio5.setDescripcion("Depilación profesional con cera caliente de alta calidad. " +
                "Resultados duraderos y piel suave. Consultar zonas disponibles.");
        servicio5.setDuracionMinutos(30);
        servicio5.setPrecio(new BigDecimal("15.00"));
        servicio5.setAforoMaximo(1);
        servicio5.setActivo(true);
        servicios.add(servicio5);

        // Servicio 6: Tinte de Pelo
        Servicio servicio6 = new Servicio();
        servicio6.setNombre("Tinte y Color");
        servicio6.setDescripcion("Servicio de coloración profesional con tintes de alta calidad. " +
                "Incluye prueba de alergia, aplicación del color, lavado y acabado.");
        servicio6.setDuracionMinutos(120);
        servicio6.setPrecio(new BigDecimal("55.00"));
        servicio6.setAforoMaximo(1);
        servicio6.setActivo(true);
        servicios.add(servicio6);

        servicioRepository.saveAll(servicios);
        log.info("✓ {} servicios creados correctamente", servicios.size());
    }

    /**
     * Carga franjas horarias para los próximos 7 días.
     *
     * Genera franjas de 9:00 a 20:00 para cada servicio activo.
     * Esto permite tener disponibilidad inmediata para hacer pruebas.
     */
    private void cargarFranjasHorarias() {
        log.info("Cargando franjas horarias para los próximos 7 días...");

        List<Servicio> servicios = servicioRepository.findByActivo(true);

        if (servicios.isEmpty()) {
            log.warn("No hay servicios activos. Omitiendo creación de franjas.");
            return;
        }

        int franjasCreadas = 0;
        LocalDate fechaInicio = LocalDate.now();

        // Crear franjas para los próximos 7 días
        for (int dia = 0; dia < 7; dia++) {
            LocalDate fecha = fechaInicio.plusDays(dia);

            for (Servicio servicio : servicios) {
                // Crear franjas de 9:00 a 20:00 (horario de ejemplo)
                // Ajustar según necesidades reales del negocio
                LocalTime horaInicio = LocalTime.of(9, 0);
                LocalTime horaFin = LocalTime.of(20, 0);

                while (horaInicio.plusMinutes(servicio.getDuracionMinutos()).isBefore(horaFin) ||
                        horaInicio.plusMinutes(servicio.getDuracionMinutos()).equals(horaFin)) {

                    // Verificar que la franja no exista ya
                    boolean existe = franjaRepository.existsByServicioIdAndFechaAndHoraInicio(
                            servicio.getId(), fecha, horaInicio
                    );

                    if (!existe) {
                        FranjaHoraria franja = new FranjaHoraria();
                        franja.setServicio(servicio);
                        franja.setFecha(fecha);
                        franja.setHoraInicio(horaInicio);
                        franja.setHoraFin(horaInicio.plusMinutes(servicio.getDuracionMinutos()));
                        franja.setPlazasTotales(servicio.getAforoMaximo());
                        franja.setPlazasDisponibles(servicio.getAforoMaximo());

                        franjaRepository.save(franja);
                        franjasCreadas++;
                    }

                    // Avanzar a la siguiente franja
                    horaInicio = horaInicio.plusMinutes(servicio.getDuracionMinutos());
                }
            }
        }

        log.info("✓ {} franjas horarias creadas para los próximos 7 días", franjasCreadas);
    }
}