package es.tracker.model;

import java.time.LocalDate;

/**
 * Entrevista asociada a una oferta.
 *
 * @param id        identificador, {@code null} si aún no está guardada
 * @param idOferta  oferta a la que pertenece
 * @param fecha     fecha de la entrevista
 * @param tipo      tipo de entrevista (telefónica, técnica, RR. HH., ...)
 * @param notas     notas libres (opcional)
 * @param resultado resultado de la entrevista
 */
public record Entrevista(Integer id, int idOferta, LocalDate fecha, String tipo, String notas,
                         ResultadoEntrevista resultado) {
}
