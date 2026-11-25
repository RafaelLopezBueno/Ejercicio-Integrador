package es.tracker.db;

import es.tracker.config.DatabaseConfig;
import es.tracker.exception.DataAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Punto de acceso a la base de datos. Entrega conexiones a los repositorios y permite agrupar
 * varias operaciones en una única transacción.
 */
public class Database {

    private static final String RECURSO_ESQUEMA = "/db/schema.sql";

    private final DatabaseConfig config;
    private final ThreadLocal<Connection> transaccion = new ThreadLocal<>();

    public Database(DatabaseConfig config) {
        this.config = config;
    }

    /**
     * Obtiene una conexión. Dentro de {@link #enTransaccion(Operacion)} devuelve la conexión de la
     * transacción en curso y no la cierra al terminar; fuera de ella abre y cierra una nueva.
     *
     * @return conexión lista para usar en un bloque try-with-resources
     * @throws DataAccessException si no se puede conectar
     */
    public Conexion abrir() {
        Connection actual = transaccion.get();
        if (actual != null) {
            return new Conexion(actual, false);
        }
        return new Conexion(nuevaConexion(), true);
    }

    /**
     * Ejecuta varias operaciones en una transacción: si todas terminan bien se confirma (commit) y,
     * si alguna lanza una excepción, se deshace todo (rollback). Si ya hay una transacción en curso,
     * la operación se une a ella.
     *
     * @param operacion trabajo a realizar
     * @param <T>       tipo del resultado
     * @return resultado de la operación
     * @throws DataAccessException si falla la gestión de la transacción
     */
    public <T> T enTransaccion(Operacion<T> operacion) {
        if (transaccion.get() != null) {
            return operacion.ejecutar();
        }
        Connection conexion = nuevaConexion();
        try {
            conexion.setAutoCommit(false);
            transaccion.set(conexion);
            T resultado = operacion.ejecutar();
            conexion.commit();
            return resultado;
        } catch (RuntimeException | SQLException e) {
            deshacer(conexion);
            if (e instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new DataAccessException("Error en la transacción: " + e.getMessage(), e);
        } finally {
            transaccion.remove();
            cerrar(conexion);
        }
    }

    /**
     * Crea las tablas ejecutando {@code db/schema.sql}. Es seguro llamarlo varias veces.
     *
     * @throws DataAccessException si no se puede leer o ejecutar el script
     */
    public void crearEsquema() {
        String script;
        try (InputStream in = Database.class.getResourceAsStream(RECURSO_ESQUEMA)) {
            if (in == null) {
                throw new DataAccessException("No se encuentra " + RECURSO_ESQUEMA + " en el classpath", null);
            }
            script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DataAccessException("No se pudo leer el esquema: " + e.getMessage(), e);
        }
        try (Conexion conexion = abrir(); Statement sentencia = conexion.connection().createStatement()) {
            for (String sql : script.replaceAll("(?m)^--.*$", "").split(";")) {
                if (!sql.isBlank()) {
                    sentencia.execute(sql.trim());
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo crear el esquema: " + e.getMessage(), e);
        }
    }

    /**
     * @return configuración con la que se creó la base de datos
     */
    public DatabaseConfig config() {
        return config;
    }

    private Connection nuevaConexion() {
        try {
            return DriverManager.getConnection(config.url(), config.user(), config.password());
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo conectar con la base de datos: " + e.getMessage(), e);
        }
    }

    private static void deshacer(Connection conexion) {
        try {
            conexion.rollback();
        } catch (SQLException ignorada) {
            // la conexión se cierra a continuación; no hay nada más que hacer
        }
    }

    private static void cerrar(Connection conexion) {
        try {
            conexion.close();
        } catch (SQLException ignorada) {
            // no se puede recuperar nada de una conexión que falla al cerrarse
        }
    }

    /**
     * Trabajo que se ejecuta dentro de una transacción.
     *
     * @param <T> tipo del resultado
     */
    @FunctionalInterface
    public interface Operacion<T> {
        T ejecutar();
    }

    /**
     * Conexión prestada por {@link Database}. Al cerrarla solo se cierra la conexión real si no
     * pertenece a una transacción.
     */
    public static final class Conexion implements AutoCloseable {

        private final Connection connection;
        private final boolean propia;

        private Conexion(Connection connection, boolean propia) {
            this.connection = connection;
            this.propia = propia;
        }

        public Connection connection() {
            return connection;
        }

        @Override
        public void close() {
            if (propia) {
                cerrar(connection);
            }
        }
    }
}
