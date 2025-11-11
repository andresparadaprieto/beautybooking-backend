package com.beautybooking.service;

import com.beautybooking.dto.request.ReservaRequest;
import com.beautybooking.dto.response.ApiResponse;
import com.beautybooking.dto.response.ReservaResponse;
import com.beautybooking.exception.BusinessException;
import com.beautybooking.exception.ResourceNotFoundException;
import com.beautybooking.model.FranjaHoraria;
import com.beautybooking.model.Reserva;
import com.beautybooking.model.Usuario;
import com.beautybooking.model.enums.EstadoReserva;
import com.beautybooking.model.enums.RolUsuario;
import com.beautybooking.repository.FranjaHorariaRepository;
import com.beautybooking.repository.ReservaRepository;
import com.beautybooking.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de reservas.
 *
 * Responsabilidades:
 * - Crear y cancelar reservas
 * - Controlar aforo y disponibilidad
 * - Validar horarios permitidos (07:00-22:00)
 * - Gestionar estados de reservas
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final FranjaHorariaRepository franjaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Horarios permitidos según reglas de negocio
    private static final LocalTime HORA_APERTURA = LocalTime.of(7, 0);
    private static final LocalTime HORA_CIERRE = LocalTime.of(22, 0);

    /**
     * Crea una nueva reserva.
     *
     * Valida que el usuario existe, que hay plazas disponibles en la franja,
     * que el horario está permitido (07:00-22:00) y que no hay solapamientos.
     * Usa bloqueo pesimista en la franja para evitar problemas de concurrencia.
     *
     * @param usuarioEmail Email del usuario que reserva
     * @param request Datos de la reserva (franjaId, notas)
     * @return ReservaResponse con datos de la reserva creada
     * @throws ResourceNotFoundException si usuario o franja no existen
     * @throws BusinessException si viola alguna regla de negocio
     */
    @Transactional
    public ReservaResponse createReserva(String usuarioEmail, ReservaRequest request) {

        // 1. Buscar usuario que realiza la reserva
        Usuario usuario = usuarioRepository.findByEmail(usuarioEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado: " + usuarioEmail
                ));

        // 2. Obtener franja con BLOQUEO PESIMISTA
        // Esto evita que dos usuarios reserven la última plaza simultáneamente
        // La franja queda bloqueada hasta el final de la transacción
        FranjaHoraria franja = franjaRepository.findByIdWithLock(request.getFranjaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Franja horaria no encontrada con ID: " + request.getFranjaId()
                ));

        // 3. VALIDACIÓN: Horario permitido (07:00 - 22:00)
        if (franja.getHoraInicio().isBefore(HORA_APERTURA) ||
                franja.getHoraFin().isAfter(HORA_CIERRE)) {
            throw new BusinessException(
                    "Las reservas solo están permitidas entre las 07:00 y las 22:00. " +
                            "Horario de esta franja: " + franja.getHoraInicio() + " - " + franja.getHoraFin()
            );
        }

        // 4. VALIDACIÓN: Verificar disponibilidad
        if (!franja.tieneDisponibilidad()) {
            throw new BusinessException(
                    "No hay plazas disponibles para esta franja horaria. " +
                            "Servicio: " + franja.getServicio().getNombre() + ", " +
                            "Fecha: " + franja.getFecha() + ", " +
                            "Hora: " + franja.getHoraInicio()
            );
        }

        // 5. VALIDACIÓN: Evitar reservas duplicadas (mismo usuario, misma franja)
        if (reservaRepository.existsByUsuarioIdAndFranjaId(usuario.getId(), franja.getId())) {
            throw new BusinessException(
                    "Ya tienes una reserva activa para esta franja horaria"
            );
        }

        // 6. VALIDACIÓN: Evitar solapamientos (usuario no puede tener dos reservas al mismo tiempo)
        List<EstadoReserva> estadosActivos = Arrays.asList(
                EstadoReserva.PENDIENTE,
                EstadoReserva.CONFIRMADA
        );

        boolean tieneSolapamiento = reservaRepository.existsSolapamiento(
                usuario,
                franja.getFecha(),
                franja.getHoraInicio(),
                franja.getHoraFin(),
                estadosActivos
        );

        if (tieneSolapamiento) {
            throw new BusinessException(
                    "Ya tienes una reserva activa que solapa con este horario. " +
                            "No puedes tener dos reservas al mismo tiempo."
            );
        }

        // 7. OPERACIÓN CRÍTICA: Decrementar plazas disponibles
        // Se hace dentro de la transacción con la franja bloqueada
        franja.decrementarPlazas();
        franjaRepository.save(franja);

        // 8. Crear reserva
        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setServicio(franja.getServicio());
        reserva.setFranja(franja);
        reserva.setFecha(franja.getFecha());
        reserva.setHoraInicio(franja.getHoraInicio());
        reserva.setHoraFin(franja.getHoraFin());
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setPrecioFinal(franja.getServicio().getPrecio());
        reserva.setNotas(request.getNotas());

        // 9. Guardar reserva
        Reserva savedReserva = reservaRepository.save(reserva);

        // 10. Retornar respuesta
        return mapToResponse(savedReserva);
    }

    /**
     * Obtiene todas las reservas de un usuario.
     *
     * @param usuarioEmail Email del usuario
     * @return Lista de ReservaResponse del usuario
     */
    @Transactional(readOnly = true)
    public List<ReservaResponse> getMisReservas(String usuarioEmail) {
        Usuario usuario = usuarioRepository.findByEmail(usuarioEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usuarioEmail));

        return reservaRepository.findByUsuarioIdOrderByFechaDescHoraInicioDesc(usuario.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una reserva por su ID.
     * Verifica que la reserva pertenezca al usuario.
     *
     * @param id ID de la reserva
     * @param usuarioEmail Email del usuario
     * @return ReservaResponse
     * @throws ResourceNotFoundException si no existe
     * @throws BusinessException si no pertenece al usuario
     */
    @Transactional(readOnly = true)
    public ReservaResponse getReservaById(Long id, String usuarioEmail) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con ID: " + id));

        // Verificar que la reserva pertenece al usuario
        if (!reserva.getUsuario().getEmail().equals(usuarioEmail)) {
            throw new BusinessException("No tienes permiso para ver esta reserva");
        }

        return mapToResponse(reserva);
    }

    /**
     * Cancela una reserva.
     *
     * Verifica que la reserva pertenece al usuario y que se puede cancelar
     * (solo reservas pendientes o confirmadas). Al cancelar, libera la plaza
     * en la franja horaria correspondiente.
     *
     * @param id ID de la reserva
     * @param usuarioEmail Email del usuario
     * @return ApiResponse confirmando la cancelación
     * @throws ResourceNotFoundException si no existe
     * @throws BusinessException si no es cancelable o no pertenece al usuario
     */
    @Transactional
    public ApiResponse cancelarReserva(Long id, String usuarioEmail) {

        // Buscar reserva
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con ID: " + id));

        // Verificar que pertenece al usuario
        if (!reserva.getUsuario().getEmail().equals(usuarioEmail)) {
            throw new BusinessException("No tienes permiso para cancelar esta reserva");
        }

        // Verificar que es cancelable
        if (!reserva.esCancelable()) {
            throw new BusinessException(
                    "No se puede cancelar una reserva en estado " + reserva.getEstado()
            );
        }

        // Cancelar reserva (esto también libera la plaza en la franja)
        reserva.cancelar();

        // Guardar cambios
        reservaRepository.save(reserva);

        // Si la franja fue modificada, guardarla también
        if (reserva.getFranja() != null) {
            franjaRepository.save(reserva.getFranja());
        }

        return ApiResponse.success("Reserva cancelada correctamente");
    }

    /**
     * Cancela una reserva como administrador.
     *
     * A diferencia del método para clientes, este no verifica que la reserva
     * pertenezca al usuario autenticado, permitiendo al admin cancelar cualquier
     * reserva del sistema. Útil para cancelaciones por teléfono o cierres temporales.
     *
     * @param id ID de la reserva
     * @return ApiResponse confirmando la cancelación
     * @throws ResourceNotFoundException si no existe la reserva
     * @throws BusinessException si la reserva no es cancelable
     */
    @Transactional
    public ApiResponse cancelarReservaAdmin(Long id) {

        // Buscar reserva
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reserva no encontrada con ID: " + id
                ));

        // Verificar que es cancelable (PENDIENTE o CONFIRMADA)
        if (!reserva.esCancelable()) {
            throw new BusinessException(
                    "No se puede cancelar una reserva en estado " + reserva.getEstado() + ". " +
                            "Solo se pueden cancelar reservas en estado PENDIENTE o CONFIRMADA."
            );
        }

        // Cancelar reserva (esto también libera la plaza en la franja)
        reserva.cancelar();

        // Guardar cambios
        reservaRepository.save(reserva);

        // Si la franja fue modificada, guardarla también
        if (reserva.getFranja() != null) {
            franjaRepository.save(reserva.getFranja());
        }

        return ApiResponse.success(
                "Reserva cancelada correctamente por el administrador. " +
                        "Cliente: " + reserva.getUsuario().getNombre() + " (" + reserva.getUsuario().getEmail() + ")"
        );
    }

    /**
     * Obtiene todas las reservas del sistema (solo ADMIN).
     *
     * @return Lista de todas las ReservaResponse
     */
    @Transactional(readOnly = true)
    public List<ReservaResponse> getAllReservas() {
        return reservaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas del día actual (solo ADMIN).
     * Útil para dashboard: "Citas de hoy".
     *
     * @return Lista de ReservaResponse de hoy
     */
    @Transactional(readOnly = true)
    public List<ReservaResponse> getReservasDeHoy() {
        List<EstadoReserva> estadosActivos = Arrays.asList(
                EstadoReserva.PENDIENTE,
                EstadoReserva.CONFIRMADA
        );

        return reservaRepository.findReservasDelDia(LocalDate.now(), estadosActivos).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Confirma una reserva (solo ADMIN).
     * Cambia estado de PENDIENTE a CONFIRMADA.
     *
     * @param id ID de la reserva
     * @return ReservaResponse actualizada
     * @throws ResourceNotFoundException si no existe
     */
    @Transactional
    public ReservaResponse confirmarReserva(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con ID: " + id));

        reserva.confirmar();
        Reserva updatedReserva = reservaRepository.save(reserva);

        return mapToResponse(updatedReserva);
    }

    /**
     * Marca una reserva como completada (solo ADMIN).
     * Cambia estado de CONFIRMADA a COMPLETADA.
     *
     * @param id ID de la reserva
     * @return ReservaResponse actualizada
     * @throws ResourceNotFoundException si no existe
     */
    @Transactional
    public ReservaResponse completarReserva(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con ID: " + id));

        reserva.completar();
        Reserva updatedReserva = reservaRepository.save(reserva);

        return mapToResponse(updatedReserva);
    }

    /**
     * Crea una reserva manual con auto-registro de usuario si no existe.
     *
     * Si el usuario no está registrado, lo crea automáticamente con una contraseña
     * temporal. Útil para reservas telefónicas o clientes sin registro previo.
     *
     * @param usuarioEmail Email del cliente
     * @param usuarioNombre Nombre del cliente (si es nuevo usuario)
     * @param request Datos de la reserva
     * @return ReservaResponse
     */
    @Transactional
    public ReservaResponse createReservaManualConAutoRegistro(
            String usuarioEmail,
            String usuarioNombre,
            ReservaRequest request) {

        // 1. Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmail(usuarioEmail)
                .orElseGet(() -> {
                    // 2. Si no existe, crearlo automáticamente
                    Usuario nuevoUsuario = new Usuario();
                    nuevoUsuario.setEmail(usuarioEmail);
                    nuevoUsuario.setNombre(usuarioNombre != null && !usuarioNombre.isBlank()
                            ? usuarioNombre
                            : "Cliente " + usuarioEmail.split("@")[0]); // Nombre por defecto
                    nuevoUsuario.setRol(RolUsuario.CLIENTE);
                    nuevoUsuario.setActivo(true);

                    // Password temporal (el cliente deberá cambiarlo al hacer login)
                    // Usar un password aleatorio o uno por defecto
                    nuevoUsuario.setPasswordHash(passwordEncoder.encode("temporal123"));

                    // Telefono opcional
                    nuevoUsuario.setTelefono(null);

                    return usuarioRepository.save(nuevoUsuario);
                });

        // 3. Crear la reserva normalmente
        return createReserva(usuario.getEmail(), request);
    }


    /**
     * Edita una reserva existente (solo administrador).
     *
     * Permite cambiar la franja horaria y las notas. Si se cambia la franja,
     * libera la plaza de la anterior y ocupa una de la nueva, validando
     * disponibilidad y que no haya solapamientos.
     *
     * @param id ID de la reserva a editar
     * @param request Nuevos datos (franjaId, notas)
     * @return ReservaResponse actualizada
     * @throws ResourceNotFoundException si la reserva o la nueva franja no existen
     * @throws BusinessException si la reserva no es editable o viola reglas de negocio
     */
    @Transactional
    public ReservaResponse editarReserva(Long id, ReservaRequest request) {

        // 1. Buscar reserva
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reserva no encontrada con ID: " + id
                ));

        // 2. VALIDACIÓN: Solo se pueden editar reservas PENDIENTES o CONFIRMADAS
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new BusinessException(
                    "No se puede editar una reserva cancelada. Estado actual: " + reserva.getEstado()
            );
        }

        if (reserva.getEstado() == EstadoReserva.COMPLETADA) {
            throw new BusinessException(
                    "No se puede editar una reserva completada. Estado actual: " + reserva.getEstado()
            );
        }

        // 3. Verificar si se está cambiando la franja
        Long nuevaFranjaId = request.getFranjaId();
        boolean cambiaFranja = !reserva.getFranja().getId().equals(nuevaFranjaId);

        if (cambiaFranja) {
            // 3a. Obtener franja anterior para liberar su plaza
            FranjaHoraria franjaAnterior = reserva.getFranja();

            // 3b. Obtener nueva franja con BLOQUEO PESIMISTA
            FranjaHoraria nuevaFranja = franjaRepository.findByIdWithLock(nuevaFranjaId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Franja horaria no encontrada con ID: " + nuevaFranjaId
                    ));

            // 3c. VALIDACIÓN: Horario permitido (07:00 - 22:00)
            if (nuevaFranja.getHoraInicio().isBefore(HORA_APERTURA) ||
                    nuevaFranja.getHoraFin().isAfter(HORA_CIERRE)) {
                throw new BusinessException(
                        "Las reservas solo están permitidas entre las 07:00 y las 22:00. " +
                                "Horario de esta franja: " + nuevaFranja.getHoraInicio() + " - " + nuevaFranja.getHoraFin()
                );
            }

            // 3d. VALIDACIÓN: Verificar disponibilidad de la nueva franja
            if (!nuevaFranja.tieneDisponibilidad()) {
                throw new BusinessException(
                        "No hay plazas disponibles para esta franja horaria. " +
                                "Servicio: " + nuevaFranja.getServicio().getNombre() + ", " +
                                "Fecha: " + nuevaFranja.getFecha() + ", " +
                                "Hora: " + nuevaFranja.getHoraInicio()
                );
            }

            // 3e. VALIDACIÓN: Evitar solapamientos con otras reservas del usuario
            // (excluyendo la reserva actual)
            List<EstadoReserva> estadosActivos = Arrays.asList(
                    EstadoReserva.PENDIENTE,
                    EstadoReserva.CONFIRMADA
            );

            boolean tieneSolapamiento = reservaRepository.findByUsuarioIdAndEstadoIn(
                    reserva.getUsuario().getId(),
                    estadosActivos
            ).stream()
                    .filter(r -> !r.getId().equals(id)) // Excluir la reserva actual
                    .anyMatch(r ->
                            r.getFecha().equals(nuevaFranja.getFecha()) &&
                                    !(nuevaFranja.getHoraFin().isBefore(r.getHoraInicio()) ||
                                            nuevaFranja.getHoraInicio().isAfter(r.getHoraFin()))
                    );

            if (tieneSolapamiento) {
                throw new BusinessException(
                        "El usuario ya tiene otra reserva activa que solapa con este horario. " +
                                "No puede tener dos reservas al mismo tiempo."
                );
            }

            // 3f. OPERACIÓN CRÍTICA: Liberar plaza de la franja anterior
            franjaAnterior.incrementarPlazas();
            franjaRepository.save(franjaAnterior);

            // 3g. OPERACIÓN CRÍTICA: Decrementar plazas de la nueva franja
            nuevaFranja.decrementarPlazas();
            franjaRepository.save(nuevaFranja);

            // 3h. Actualizar datos de la reserva con la nueva franja
            reserva.setFranja(nuevaFranja);
            reserva.setServicio(nuevaFranja.getServicio());
            reserva.setFecha(nuevaFranja.getFecha());
            reserva.setHoraInicio(nuevaFranja.getHoraInicio());
            reserva.setHoraFin(nuevaFranja.getHoraFin());
            reserva.setPrecioFinal(nuevaFranja.getServicio().getPrecio());
        }

        // 4. Actualizar notas (siempre, aunque sea null para borrarlas)
        reserva.setNotas(request.getNotas());

        // 5. Guardar cambios
        Reserva reservaActualizada = reservaRepository.save(reserva);

        return mapToResponse(reservaActualizada);
    }

    /**
     * Convierte entidad Reserva a DTO ReservaResponse.
     *
     * @param reserva Entidad Reserva
     * @return ReservaResponse con datos completos
     */
    private ReservaResponse mapToResponse(Reserva reserva) {
        return new ReservaResponse(
                reserva.getId(),
                reserva.getUsuario().getId(),
                reserva.getUsuario().getNombre(),
                reserva.getUsuario().getEmail(),
                reserva.getServicio().getId(),
                reserva.getServicio().getNombre(),
                reserva.getFecha(),
                reserva.getHoraInicio(),
                reserva.getHoraFin(),
                reserva.getEstado().name(),
                reserva.getPrecioFinal(),
                reserva.getNotas(),
                reserva.getCreadoEn()
        );
    }
}