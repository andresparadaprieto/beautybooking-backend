package com.beautybooking.repository;

import com.beautybooking.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Servicio.
 *
 * Gestiona el acceso a datos de los servicios/tratamientos ofrecidos.
 *
 * @author Andres Eduardo Parada Prieto
 * @version 1.0
 * @since 2025
 */
@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    /**
     * Busca servicios activos.
     * Los servicios inactivos no se muestran en el catálogo público.
     *
     * Query generada:
     * SELECT * FROM servicios WHERE activo = ?
     *
     * @param activo true para servicios activos
     * @return Lista de servicios activos
     */
    List<Servicio> findByActivo(Boolean activo);

    /**
     * Busca servicios activos ordenados por nombre.
     * Útil para mostrar el catálogo en orden alfabético.
     *
     * @param activo true para servicios activos
     * @return Lista ordenada por nombre ASC
     */
    List<Servicio> findByActivoOrderByNombreAsc(Boolean activo);

    /**
     * Busca servicios por nombre (búsqueda parcial, case-insensitive).
     * Permite a los usuarios buscar servicios por palabras clave.
     *
     * Query generada:
     * SELECT * FROM servicios WHERE LOWER(nombre) LIKE LOWER(CONCAT('%', ?, '%'))
     *
     * @param nombre Palabra clave a buscar
     * @return Lista de servicios que contienen la palabra en su nombre
     */
    List<Servicio> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Cuenta servicios activos.
     * Útil para estadísticas del dashboard de administración.
     *
     * @param activo true para contar solo activos
     * @return Número de servicios activos
     */
    long countByActivo(Boolean activo);

    /**
     * Busca servicios por rango de precio usando @Query personalizada.
     * Ejemplo de uso de JPQL (Java Persistence Query Language).
     *
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @return Lista de servicios en ese rango de precio
     */
    @Query("SELECT s FROM Servicio s WHERE s.precio BETWEEN :precioMin AND :precioMax AND s.activo = true")
    List<Servicio> findByRangoPrecio(Double precioMin, Double precioMax);
}
