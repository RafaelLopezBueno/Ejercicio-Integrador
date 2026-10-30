package es.tracker.model;

import es.tracker.exception.CandidaturaException;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Estados posibles de una candidatura laboral.
 */
public enum Estado {
    PENDIENTE("pendiente"),
    ENTREVISTA("entrevista"),
    RECHAZADO("rechazado"),
    ACEPTADO("aceptado");

    private final String texto;

    Estado(String texto) {
        this.texto = texto;
    }

    /**
     * @return nombre en minúsculas tal y como se guarda en el archivo
     */
    public String getTexto() {
        return texto;
    }

    /**
     * Convierte un texto en un estado ignorando mayúsculas y espacios.
     *
     * @param valor texto introducido por el usuario
     * @return estado correspondiente
     * @throws CandidaturaException si el texto no es un estado válido
     */
    public static Estado desdeTexto(String valor) {
        if (valor != null) {
            String limpio = valor.trim();
            for (Estado estado : values()) {
                if (estado.texto.equalsIgnoreCase(limpio)) {
                    return estado;
                }
            }
        }
        String validos = Arrays.stream(values()).map(Estado::getTexto).collect(Collectors.joining(", "));
        throw new CandidaturaException("Estado no válido: '" + valor + "'. Valores permitidos: " + validos);
    }
}
