package es.tracker.config;

import es.tracker.exception.CandidaturaException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * Parámetros de conexión a la base de datos.
 * Se leen de {@code config.properties} y se pueden sobrescribir con variables de entorno.
 *
 * @param url      URL JDBC
 * @param user     usuario
 * @param password contraseña
 * @param init     si es {@code true}, se crean las tablas al arrancar
 */
public record DatabaseConfig(String url, String user, String password, boolean init) {

    /** Nombre del archivo de configuración buscado en el directorio de trabajo. */
    public static final String ARCHIVO = "config.properties";

    /**
     * Carga la configuración desde {@code config.properties} (si existe) y las variables de entorno
     * {@code TRACKER_DB_URL}, {@code TRACKER_DB_USER}, {@code TRACKER_DB_PASSWORD} y {@code TRACKER_DB_INIT}.
     *
     * @return configuración resultante
     * @throws CandidaturaException si falta la URL o el archivo no se puede leer
     */
    public static DatabaseConfig cargar() {
        return cargar(Path.of(ARCHIVO), System.getenv());
    }

    /**
     * @param archivo archivo de propiedades; puede no existir
     * @param entorno variables de entorno que tienen prioridad sobre el archivo
     * @return configuración resultante
     * @throws CandidaturaException si falta la URL o el archivo no se puede leer
     */
    public static DatabaseConfig cargar(Path archivo, Map<String, String> entorno) {
        Properties propiedades = new Properties();
        if (Files.exists(archivo)) {
            try (InputStream in = Files.newInputStream(archivo)) {
                propiedades.load(in);
            } catch (IOException e) {
                throw new CandidaturaException("No se pudo leer " + archivo + ": " + e.getMessage(), e);
            }
        }
        String url = valor(propiedades, entorno, "db.url", "TRACKER_DB_URL");
        if (url == null || url.isBlank()) {
            throw new CandidaturaException("Falta la configuración de la base de datos. Copia config.properties.example "
                    + "como config.properties o define TRACKER_DB_URL.");
        }
        String init = valor(propiedades, entorno, "db.init", "TRACKER_DB_INIT");
        return new DatabaseConfig(url.trim(),
                valor(propiedades, entorno, "db.user", "TRACKER_DB_USER"),
                valor(propiedades, entorno, "db.password", "TRACKER_DB_PASSWORD"),
                init != null && Boolean.parseBoolean(init.trim()));
    }

    private static String valor(Properties propiedades, Map<String, String> entorno, String clave, String variable) {
        String delEntorno = entorno.get(variable);
        return delEntorno != null ? delEntorno : propiedades.getProperty(clave);
    }
}
