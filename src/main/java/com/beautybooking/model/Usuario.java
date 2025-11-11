package com.beautybooking.model;

import com.beautybooking.model.enums.RolUsuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Usuario - Representa a los usuarios del sistema (clientes y administradores).
 *
 * Mapea la tabla 'usuarios' en la base de datos.
 * Almacena información personal, credenciales de acceso y rol del usuario.
 *
 * Relaciones:
 * - Un usuario puede tener muchas reservas (relación OneToMany con Reserva)
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "usuarios", indexes = {
        @Index(name = "idx_email", columnList = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    /**
     * Identificador único del usuario.
     * Generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre completo del usuario.
     * Campo obligatorio, máximo 100 caracteres.
     */
    @Column(nullable = false, length = 100)
    private String nombre;

    /**
     * Email del usuario - usado como nombre de usuario para login.
     * Debe ser único en el sistema.
     * Campo obligatorio, máximo 150 caracteres.
     */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Contraseña hasheada con BCrypt.
     * NUNCA se almacena en texto plano.
     * Campo obligatorio.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * Rol del usuario en el sistema (CLIENTE o ADMIN).
     * Determina los permisos y accesos del usuario.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolUsuario rol;

    /**
     * Número de teléfono del usuario (opcional).
     * Usado para notificaciones SMS o contacto.
     */
    @Column(length = 20)
    private String telefono;

    /**
     * Indica si el usuario está activo en el sistema.
     * Usuarios inactivos no pueden hacer login.
     * Por defecto: true
     */
    @Column(nullable = false)
    private Boolean activo = true;

    /**
     * Fecha y hora de creación del usuario.
     * Se establece automáticamente al crear el registro.
     */
    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    /**
     * Lista de reservas realizadas por este usuario.
     * Relación OneToMany con Reserva.
     * CascadeType.ALL: las operaciones se propagan a las reservas.
     * orphanRemoval = true: elimina reservas huérfanas automáticamente.
     */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reserva> reservas = new ArrayList<>();

    /**
     * Constructor de conveniencia para crear usuarios nuevos.
     * Útil en tests y DataLoader.
     */
    public Usuario(String nombre, String email, String passwordHash, RolUsuario rol) {
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = true;
    }
}