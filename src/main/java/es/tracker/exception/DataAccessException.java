package es.tracker.exception;

/**
 * Error al acceder a la base de datos.
 */
public class DataAccessException extends CandidaturaException {

    public DataAccessException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
