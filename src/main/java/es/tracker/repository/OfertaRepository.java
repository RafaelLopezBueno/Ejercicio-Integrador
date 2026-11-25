package es.tracker.repository;

import es.tracker.model.Estado;
import es.tracker.model.FiltroOfertas;
import es.tracker.model.Oferta;
import es.tracker.model.OfertaVista;

import java.util.List;
import java.util.Optional;

/**
 * Persistencia de ofertas.
 */
public interface OfertaRepository {

    /**
     * Busca ofertas combinando todos los criterios indicados en el filtro.
     *
     * @param filtro criterios; los nulos no se aplican
     * @return ofertas con el nombre de su empresa, de la más reciente a la más antigua
     */
    List<OfertaVista> buscar(FiltroOfertas filtro);

    Optional<Oferta> findById(int id);

    /**
     * @return la oferta con el nombre de su empresa, si existe
     */
    Optional<OfertaVista> findVistaById(int id);

    /**
     * Inserta la oferta si no tiene id y la actualiza en caso contrario.
     *
     * @return oferta guardada, con su id
     */
    Oferta save(Oferta oferta);

    /**
     * @return {@code true} si la oferta existía y se actualizó
     */
    boolean actualizarEstado(int id, Estado estado);

    /**
     * @return {@code true} si existía y se eliminó
     */
    boolean deleteById(int id);

    /**
     * @return número de ofertas eliminadas
     */
    int deleteByEmpresa(int idEmpresa);

    int countByEmpresa(int idEmpresa);
}
