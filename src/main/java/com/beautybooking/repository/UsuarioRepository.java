package com.beautybooking.repository;

import com.beautybooking.model.Usuario;
import com.beautybooking.model.enums.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Usuario.
 *
 * Proporciona métodos de acceso a datos (CRUD) para usuarios del sistema.
 * Extiende JpaRepository que incluye métodos básicos como:
 * - save(), findById(), findAll(), deleteById(), etc.
 *
 * También define queries personalizadas usando Spring Data JPA naming conventions.
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su email (usado para login).
     *
     * Spring Data JPA genera automáticamente la query:
     * SELECT * FROM usuarios WHERE email = ?
     *
     * @param email Email del usuario
     * @return Optional<Usuario> - vacío si no existe
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el email dado.
     * Útil para validar emails duplicados en registro.
     *
     * Query generada:
     * SELECT COUNT(*) > 0 FROM usuarios WHERE email = ?
     *
     * @param email Email a verificar
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);

    /**
     * Busca usuarios por rol (CLIENTE o ADMIN).
     * Útil para listar todos los administradores o clientes.
     *
     * @param rol Rol a buscar
     * @return Lista de usuarios con ese rol
     */
    List<Usuario> findByRol(RolUsuario rol);

    /**
     * Busca usuarios activos.
     * Útil para filtrar usuarios que pueden hacer login.
     *
     * @param activo true para activos, false para inactivos
     * @return Lista de usuarios según el estado
     */
    List<Usuario> findByActivo(Boolean activo);

    /**
     * Busca un usuario por email y que esté activo.
     * Combina dos condiciones: email Y activo = true.
     * Usado en autenticación para verificar que el usuario existe y está activo.
     *
     * @param email Email del usuario
     * @param activo Estado activo
     * @return Optional<Usuario>
     */
    Optional<Usuario> findByEmailAndActivo(String email, Boolean activo);
}