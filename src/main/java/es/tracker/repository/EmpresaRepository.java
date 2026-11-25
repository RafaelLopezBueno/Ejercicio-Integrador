package es.tracker.repository;

import es.tracker.model.Empresa;
import es.tracker.model.EmpresaResumen;

import java.util.List;
import java.util.Optional;

/**
 * Persistencia de empresas.
 */
public interface EmpresaRepository {

    /**
     * @return todas las empresas ordenadas por nombre
     */
    List<Empresa> findAll();

    /**
     * @return todas las empresas con su número de ofertas, ordenadas por nombre
     */
    List<EmpresaResumen> findAllConTotales();

    Optional<Empresa> findById(int id);

    /**
     * @param nombre nombre exacto, sin distinguir mayúsculas
     * @return la empresa con ese nombre, si existe
     */
    Optional<Empresa> findByNombre(String nombre);

    /**
     * Inserta la empresa si no tiene id y la actualiza en caso contrario.
     *
     * @return empresa guardada, con su id
     */
    Empresa save(Empresa empresa);

    /**
     * @return {@code true} si existía y se eliminó
     */
    boolean deleteById(int id);
}
