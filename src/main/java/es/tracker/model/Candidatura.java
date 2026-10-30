package es.tracker.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Candidatura a una oferta de empleo. Cada instancia equivale a una línea del archivo de datos.
 *
 * @param id               identificador único
 * @param titulo           título de la oferta
 * @param empresa          nombre de la empresa
 * @param estado           estado actual del proceso
 * @param fechaPostulacion fecha en la que se envió la candidatura
 * @param enlace           enlace a la oferta (puede estar vacío)
 * @param notas            notas libres (pueden estar vacías)
 */
public record Candidatura(int id, String titulo, String empresa, Estado estado,
                          LocalDate fechaPostulacion, String enlace, String notas) {

    /** Formato de fecha usado en el archivo y en la línea de comandos. */
    public static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * @param nuevoId identificador a asignar
     * @return copia de la candidatura con el nuevo identificador
     */
    public Candidatura conId(int nuevoId) {
        return new Candidatura(nuevoId, titulo, empresa, estado, fechaPostulacion, enlace, notas);
    }
}
