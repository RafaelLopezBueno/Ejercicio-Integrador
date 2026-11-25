package es.tracker.repository;

import es.tracker.exception.CandidaturaException;
import es.tracker.exception.DataAccessException;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Utilidades comunes a los repositorios JDBC.
 */
final class Jdbc {

    private Jdbc() {
    }

    /**
     * @return {@code true} si el error se debe a una clave única repetida
     */
    static boolean esDuplicado(SQLException e) {
        return "23505".equals(e.getSQLState()) || e.getErrorCode() == 1062;
    }

    /**
     * @return {@code true} si el error se debe a una clave foránea que no existe o que está en uso
     */
    static boolean esClaveForanea(SQLException e) {
        return "23503".equals(e.getSQLState()) || e.getErrorCode() == 1452 || e.getErrorCode() == 1451;
    }

    /**
     * Convierte un error SQL en una excepción de la aplicación con un mensaje legible.
     */
    static CandidaturaException error(String operacion, SQLException e) {
        return new DataAccessException("Error de base de datos al " + operacion + ": " + e.getMessage(), e);
    }

    static void setDecimal(PreparedStatement sentencia, int indice, BigDecimal valor) throws SQLException {
        if (valor == null) {
            sentencia.setNull(indice, Types.DECIMAL);
        } else {
            sentencia.setBigDecimal(indice, valor);
        }
    }

    /**
     * @return {@code null} si el texto es nulo o está en blanco; el texto recortado en otro caso
     */
    static String vacioANulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    static String noNulo(String texto) {
        return texto == null ? "" : texto;
    }
}
