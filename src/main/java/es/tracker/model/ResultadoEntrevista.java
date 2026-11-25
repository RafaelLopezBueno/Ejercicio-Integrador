package es.tracker.model;

import es.tracker.exception.CandidaturaException;

/**
 * Resultado de una entrevista.
 */
public enum ResultadoEntrevista {
    PENDIENTE("pendiente"),
    APROBADA("aprobada"),
    RECHAZADA("rechazada");

    private final String texto;

    ResultadoEntrevista(String texto) {
        this.texto = texto;
    }

    /**
     * @return nombre en minúsculas tal y como se guarda en la base de datos
     */
    public String getTexto() {
        return texto;
    }

    /**
     * @param valor texto guardado o introducido
     * @return resultado correspondiente, ignorando mayúsculas y espacios
     * @throws CandidaturaException si el texto no es un resultado válido
     */
    public static ResultadoEntrevista desdeTexto(String valor) {
        if (valor != null) {
            for (ResultadoEntrevista resultado : values()) {
                if (resultado.texto.equalsIgnoreCase(valor.trim())) {
                    return resultado;
                }
            }
        }
        throw new CandidaturaException("Resultado de entrevista no válido: '" + valor + "'");
    }

    @Override
    public String toString() {
        return texto;
    }
}
