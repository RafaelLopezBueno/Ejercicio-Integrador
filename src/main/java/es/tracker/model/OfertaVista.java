package es.tracker.model;

/**
 * Oferta junto con el nombre de su empresa, resultado de unir ambas tablas.
 *
 * @param oferta        datos de la oferta
 * @param empresaNombre nombre de la empresa que la publica
 */
public record OfertaVista(Oferta oferta, String empresaNombre) {
}
