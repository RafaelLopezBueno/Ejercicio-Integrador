package es.tracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Candidatura a una oferta de empleo de una empresa.
 *
 * @param id               identificador, {@code null} si aún no está guardada
 * @param idEmpresa        empresa que publica la oferta
 * @param titulo           título de la oferta
 * @param descripcion      descripción del puesto (opcional)
 * @param estado           estado actual del proceso
 * @param fechaPostulacion fecha en la que se envió la candidatura
 * @param enlace           enlace a la oferta (opcional)
 * @param salario          salario anual bruto (opcional)
 * @param notas            notas libres (opcional)
 */
public record Oferta(Integer id, int idEmpresa, String titulo, String descripcion, Estado estado,
                     LocalDate fechaPostulacion, String enlace, BigDecimal salario, String notas) {

    /**
     * @param nuevoEstado estado al que pasa la candidatura
     * @return copia de la oferta con el estado indicado
     */
    public Oferta conEstado(Estado nuevoEstado) {
        return new Oferta(id, idEmpresa, titulo, descripcion, nuevoEstado, fechaPostulacion, enlace, salario, notas);
    }
}
