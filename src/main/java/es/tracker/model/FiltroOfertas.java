package es.tracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Criterios combinables para buscar ofertas. Un criterio a {@code null} no se aplica.
 *
 * @param estado      estado exacto
 * @param empresa     texto contenido en el nombre de la empresa, sin distinguir mayúsculas
 * @param texto       texto contenido en el título de la oferta, sin distinguir mayúsculas
 * @param salarioMin  salario mínimo (inclusive)
 * @param salarioMax  salario máximo (inclusive)
 * @param fechaDesde  fecha de postulación mínima (inclusive)
 * @param fechaHasta  fecha de postulación máxima (inclusive)
 */
public record FiltroOfertas(Estado estado, String empresa, String texto, BigDecimal salarioMin,
                            BigDecimal salarioMax, LocalDate fechaDesde, LocalDate fechaHasta) {

    /**
     * @return filtro que no restringe ningún criterio
     */
    public static FiltroOfertas vacio() {
        return new FiltroOfertas(null, null, null, null, null, null, null);
    }
}
