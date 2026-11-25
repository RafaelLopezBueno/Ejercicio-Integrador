package es.tracker.model;

/**
 * Entrevista junto con la oferta y la empresa a las que pertenece.
 *
 * @param entrevista    datos de la entrevista
 * @param tituloOferta  título de la oferta
 * @param empresaNombre nombre de la empresa
 */
public record EntrevistaVista(Entrevista entrevista, String tituloOferta, String empresaNombre) {
}
