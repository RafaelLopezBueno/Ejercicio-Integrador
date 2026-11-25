package es.tracker;

import es.tracker.config.DatabaseConfig;
import es.tracker.db.Database;
import es.tracker.exception.CandidaturaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseTest {

    private Database database;

    @BeforeEach
    void preparar() {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        database = new Database(new DatabaseConfig(url, "sa", "", true));
        database.crearEsquema();
    }

    @Test
    void crearEsquemaEsRepetible() {
        database.crearEsquema();
        assertEquals(0, contarEmpresas());
    }

    @Test
    void transaccionConfirmaSiTodoVaBien() {
        database.enTransaccion(() -> {
            insertarEmpresa("Indra");
            insertarEmpresa("Accenture");
            return null;
        });
        assertEquals(2, contarEmpresas());
    }

    @Test
    void transaccionSeDeshaceSiHayError() {
        assertThrows(CandidaturaException.class, () -> database.enTransaccion(() -> {
            insertarEmpresa("Indra");
            throw new CandidaturaException("fallo provocado");
        }));
        assertEquals(0, contarEmpresas());
    }

    @Test
    void transaccionesAnidadasSeUnenALaExterna() {
        assertThrows(CandidaturaException.class, () -> database.enTransaccion(() -> {
            database.enTransaccion(() -> {
                insertarEmpresa("Indra");
                return null;
            });
            throw new CandidaturaException("fallo en la externa");
        }));
        assertEquals(0, contarEmpresas());
    }

    @Test
    void configuracionDelEntornoTienePrioridadSobreElArchivo(@TempDir Path carpeta) throws IOException {
        Path archivo = carpeta.resolve("config.properties");
        Files.writeString(archivo, "db.url=jdbc:h2:mem:a\ndb.user=archivo\ndb.init=true\n");

        DatabaseConfig config = DatabaseConfig.cargar(archivo, Map.of("TRACKER_DB_USER", "entorno"));

        assertEquals("jdbc:h2:mem:a", config.url());
        assertEquals("entorno", config.user());
        assertTrue(config.init());
    }

    @Test
    void configuracionSinUrlLanzaError(@TempDir Path carpeta) {
        assertThrows(CandidaturaException.class,
                () -> DatabaseConfig.cargar(carpeta.resolve("no-existe.properties"), Map.of()));
    }

    private void insertarEmpresa(String nombre) {
        try (Database.Conexion conexion = database.abrir(); Statement s = conexion.connection().createStatement()) {
            s.executeUpdate("INSERT INTO empresa (nombre, fecha_registro) VALUES ('" + nombre + "', CURRENT_DATE)");
        } catch (SQLException e) {
            throw new CandidaturaException(e.getMessage(), e);
        }
    }

    private int contarEmpresas() {
        try (Database.Conexion conexion = database.abrir();
             Statement s = conexion.connection().createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM empresa")) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new CandidaturaException(e.getMessage(), e);
        }
    }
}
