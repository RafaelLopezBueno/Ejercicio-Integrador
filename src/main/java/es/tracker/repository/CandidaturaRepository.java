package es.tracker.repository;

import es.tracker.model.Candidatura;

import java.util.List;

/**
 * Acceso a la persistencia de candidaturas.
 */
public interface CandidaturaRepository {

    /**
     * @return todas las candidaturas almacenadas, en orden de aparición
     */
    List<Candidatura> findAll();

    /**
     * Sustituye el contenido almacenado por la lista indicada.
     *
     * @param candidaturas lista completa a persistir
     */
    void saveAll(List<Candidatura> candidaturas);
}
