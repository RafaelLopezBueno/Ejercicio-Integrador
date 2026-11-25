package es.tracker.model;

/**
 * Empresa con el número de ofertas que tiene registradas.
 *
 * @param empresa      datos de la empresa
 * @param totalOfertas número de candidaturas asociadas
 */
public record EmpresaResumen(Empresa empresa, int totalOfertas) {
}
