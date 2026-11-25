package es.tracker;

import es.tracker.config.DatabaseConfig;
import es.tracker.db.Database;
import es.tracker.exception.CandidaturaException;
import es.tracker.model.Empresa;
import es.tracker.model.Estado;
import es.tracker.model.FiltroOfertas;
import es.tracker.model.Oferta;
import es.tracker.repository.JdbcEmpresaRepository;
import es.tracker.repository.JdbcEntrevistaRepository;
import es.tracker.repository.JdbcOfertaRepository;
import es.tracker.service.EmpresaService;
import es.tracker.service.ImportacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprueba el comportamiento sobre un MySQL real. Solo se ejecuta si se define {@code TRACKER_TEST_DB_URL}
 * (y opcionalmente {@code TRACKER_TEST_DB_USER} y {@code TRACKER_TEST_DB_PASSWORD}). La base de datos debe
 * ser exclusiva de las pruebas: sus tablas se borran antes de cada test, por eso su nombre debe contener «test».
 */
@EnabledIfEnvironmentVariable(named = "TRACKER_TEST_DB_URL", matches = ".*test.*")
class MySqlIntegracionTest {

    private Database database;
    private JdbcEmpresaRepository empresas;
    private JdbcOfertaRepository ofertas;
    private JdbcEntrevistaRepository entrevistas;

    @BeforeEach
    void preparar() throws SQLException {
        DatabaseConfig config = new DatabaseConfig(System.getenv("TRACKER_TEST_DB_URL"),
                System.getenv().getOrDefault("TRACKER_TEST_DB_USER", "tracker"),
                System.getenv().getOrDefault("TRACKER_TEST_DB_PASSWORD", ""), true);
        database = new Database(config);
        try (Database.Conexion c = database.abrir(); Statement s = c.connection().createStatement()) {
            s.execute("DROP TABLE IF EXISTS entrevista");
            s.execute("DROP TABLE IF EXISTS oferta");
            s.execute("DROP TABLE IF EXISTS empresa");
        }
        database.crearEsquema();
        empresas = new JdbcEmpresaRepository(database);
        ofertas = new JdbcOfertaRepository(database);
        entrevistas = new JdbcEntrevistaRepository(database);
    }

    @Test
    void importaElCsvYFiltraConVariosCriterios() {
        ImportacionService importacion = new ImportacionService(database, empresas, ofertas);
        ImportacionService.Resultado resultado = importacion.importar(Path.of("data", "candidaturas_ejemplo.csv"));
        assertEquals(15, resultado.ofertasImportadas());
        assertEquals(4, resultado.empresasCreadas());

        var indraEntrevista = ofertas.buscar(new FiltroOfertas(Estado.ENTREVISTA, "indra", null, null, null, null, null));
        assertEquals(1, indraEntrevista.size());
        assertEquals("Analista Programador", indraEntrevista.getFirst().oferta().titulo());

        var septiembre = ofertas.buscar(new FiltroOfertas(null, "capgemini", null, null, null,
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)));
        assertEquals(1, septiembre.size());
        assertEquals(4, empresas.findAllConTotales().stream()
                .filter(r -> r.empresa().nombre().equals("Indra")).findFirst().orElseThrow().totalOfertas());
    }

    @Test
    void elSalarioSeGuardaYSeFiltraConDecimales() {
        Empresa empresa = empresas.save(new Empresa(null, "Prueba", null, null, LocalDate.of(2026, 1, 1)));
        ofertas.save(new Oferta(null, empresa.id(), "Con salario", null, Estado.PENDIENTE, LocalDate.of(2026, 5, 1),
                null, new BigDecimal("35000.50"), null));
        ofertas.save(new Oferta(null, empresa.id(), "Sin salario", null, Estado.PENDIENTE, LocalDate.of(2026, 5, 2),
                null, null, null));

        var conSalario = ofertas.buscar(new FiltroOfertas(null, null, null, new BigDecimal("30000"), null, null, null));
        assertEquals(1, conSalario.size());
        assertEquals(0, new BigDecimal("35000.50").compareTo(conSalario.getFirst().oferta().salario()));
    }

    @Test
    void laTransaccionSeDeshaceEnMySql() {
        assertThrows(CandidaturaException.class, () -> database.enTransaccion(() -> {
            empresas.save(new Empresa(null, "Temporal", null, null, LocalDate.now()));
            throw new CandidaturaException("fallo provocado");
        }));
        assertTrue(empresas.findByNombre("Temporal").isEmpty());
    }

    @Test
    void elNombreDeEmpresaNoDistingueMayusculas() {
        empresas.save(new Empresa(null, "Indra", null, null, LocalDate.now()));
        assertThrows(CandidaturaException.class,
                () -> empresas.save(new Empresa(null, "INDRA", null, null, LocalDate.now())));
    }

    @Test
    void eliminarUnaEmpresaEnCascadaBorraSusOfertas() {
        new ImportacionService(database, empresas, ofertas).importar(Path.of("data", "candidaturas_ejemplo.csv"));
        EmpresaService servicio = new EmpresaService(database, empresas, ofertas, entrevistas);
        int id = empresas.findByNombre("Indra").orElseThrow().id();

        assertThrows(CandidaturaException.class, () -> servicio.eliminar(id, false));
        assertEquals(4, servicio.eliminar(id, true));
        assertEquals(11, ofertas.buscar(FiltroOfertas.vacio()).size());
        assertTrue(empresas.findById(id).isEmpty());
    }
}
