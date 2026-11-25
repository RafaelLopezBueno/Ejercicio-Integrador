package es.tracker.repository;

import es.tracker.model.Entrevista;
import es.tracker.model.EntrevistaVista;

import java.util.List;
import java.util.Optional;

/**
 * Persistencia de entrevistas.
 */
public interface EntrevistaRepository {

    /**
     * @return todas las entrevistas con su oferta y empresa, de la más reciente a la más antigua
     */
    List<EntrevistaVista> findAllVistas();

    /**
     * @return entrevistas de una oferta, de la más reciente a la más antigua
     */
    List<Entrevista> findByOferta(int idOferta);

    Optional<Entrevista> findById(int id);

    /**
     * Inserta la entrevista si no tiene id y la actualiza en caso contrario.
     *
     * @return entrevista guardada, con su id
     */
    Entrevista save(Entrevista entrevista);

    /**
     * @return {@code true} si existía y se eliminó
     */
    boolean deleteById(int id);

    /**
     * @return número de entrevistas eliminadas
     */
    int deleteByOferta(int idOferta);

    /**
     * Elimina las entrevistas de todas las ofertas de una empresa.
     *
     * @return número de entrevistas eliminadas
     */
    int deleteByEmpresa(int idEmpresa);
}
