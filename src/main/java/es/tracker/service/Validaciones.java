package es.tracker.service;

import es.tracker.exception.CandidaturaException;

/**
 * Comprobaciones de datos de entrada comunes a los servicios.
 */
final class Validaciones {

    private Validaciones() {
    }

    /**
     * @param valor  texto a comprobar
     * @param campo  nombre del campo, usado en el mensaje de error
     * @param maximo longitud máxima permitida
     * @throws CandidaturaException si está vacío o supera la longitud máxima
     */
    static void textoObligatorio(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new CandidaturaException("El campo '" + campo + "' es obligatorio");
        }
        longitudMaxima(valor, campo, maximo);
    }

    /**
     * @throws CandidaturaException si el texto supera la longitud máxima
     */
    static void longitudMaxima(String valor, String campo, int maximo) {
        if (valor != null && valor.trim().length() > maximo) {
            throw new CandidaturaException("El campo '" + campo + "' no puede superar los " + maximo + " caracteres");
        }
    }

    /**
     * @throws CandidaturaException si el texto, de existir, no empieza por http:// o https://
     */
    static void urlOpcional(String valor, String campo) {
        if (valor != null && !valor.isBlank() && !valor.trim().matches("(?i)^https?://\\S+$")) {
            throw new CandidaturaException("El campo '" + campo + "' debe ser una dirección que empiece por http:// o https://");
        }
    }

    /**
     * @throws CandidaturaException si el valor es nulo
     */
    static void obligatorio(Object valor, String campo) {
        if (valor == null) {
            throw new CandidaturaException("El campo '" + campo + "' es obligatorio");
        }
    }
}
