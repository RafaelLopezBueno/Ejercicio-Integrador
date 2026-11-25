package es.tracker.service;

import es.tracker.exception.CandidaturaException;
import es.tracker.model.Entrevista;
import es.tracker.model.EntrevistaVista;
import es.tracker.repository.EntrevistaRepository;
import es.tracker.repository.OfertaRepository;

import java.util.List;

/**
 * Lógica de negocio de las entrevistas.
 */
public class EntrevistaService {

    private final EntrevistaRepository entrevistas;
    private final OfertaRepository ofertas;

    public EntrevistaService(EntrevistaRepository entrevistas, OfertaRepository ofertas) {
        this.entrevistas = entrevistas;
        this.ofertas = ofertas;
    }

    /**
     * @return todas las entrevistas con su oferta y empresa
     */
    public List<EntrevistaVista> listar() {
        return entrevistas.findAllVistas();
    }

    /**
     * @return entrevistas de una oferta
     */
    public List<Entrevista> deOferta(int idOferta) {
        return entrevistas.findByOferta(idOferta);
    }

    /**
     * Crea o actualiza una entrevista tras validar sus datos.
     *
     * @throws CandidaturaException si faltan datos o la oferta no existe
     */
    public Entrevista guardar(Entrevista entrevista) {
        Validaciones.obligatorio(entrevista.fecha(), "fecha");
        Validaciones.textoObligatorio(entrevista.tipo(), "tipo", 30);
        Validaciones.obligatorio(entrevista.resultado(), "resultado");
        if (ofertas.findById(entrevista.idOferta()).isEmpty()) {
            throw new CandidaturaException("La candidatura indicada no existe");
        }
        return entrevistas.save(entrevista);
    }

    /**
     * @throws CandidaturaException si no existe
     */
    public void eliminar(int id) {
        if (!entrevistas.deleteById(id)) {
            throw new CandidaturaException("No existe ninguna entrevista con ID " + id);
        }
    }
}
