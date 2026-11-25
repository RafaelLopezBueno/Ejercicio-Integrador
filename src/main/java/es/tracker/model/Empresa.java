package es.tracker.model;

import java.time.LocalDate;

/**
 * Empresa a la que se envían candidaturas.
 *
 * @param id            identificador, {@code null} si aún no está guardada
 * @param nombre        nombre único de la empresa
 * @param sector        sector de actividad (opcional)
 * @param web           sitio web (opcional)
 * @param fechaRegistro fecha en la que se registró en la aplicación
 */
public record Empresa(Integer id, String nombre, String sector, String web, LocalDate fechaRegistro) {

    @Override
    public String toString() {
        return nombre;
    }
}
