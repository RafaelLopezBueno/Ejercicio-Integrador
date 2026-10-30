package es.tracker.exception;

/**
 * Error de negocio o de uso que debe mostrarse al usuario por consola.
 */
public class CandidaturaException extends RuntimeException {

    public CandidaturaException(String mensaje) {
        super(mensaje);
    }

    public CandidaturaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
