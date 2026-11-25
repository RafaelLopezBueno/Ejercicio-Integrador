package es.tracker;

import es.tracker.config.DatabaseConfig;
import es.tracker.db.Database;
import es.tracker.exception.CandidaturaException;
import es.tracker.model.Estado;
import es.tracker.model.FiltroOfertas;
import es.tracker.repository.JdbcEmpresaRepository;
import es.tracker.repository.JdbcOfertaRepository;
import es.tracker.service.ImportacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImportacionServiceTest {

    private static final Path CSV_EJEMPLO = Path.of("data", "candidaturas_ejemplo.csv");

    private JdbcEmpresaRepository empresas;
    private JdbcOfertaRepository ofertas;
    private ImportacionService importacion;

    @BeforeEach
    void preparar() {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        Database database = new Database(new DatabaseConfig(url, "sa", "", true));
        database.crearEsquema();
        empresas = new JdbcEmpresaRepository(database);
        ofertas = new JdbcOfertaRepository(database);
        importacion = new ImportacionService(database, empresas, ofertas);
    }

    @Test
    void importaElCsvDeEjemploCreandoLasEmpresas() {
        ImportacionService.Resultado resultado = importacion.importar(CSV_EJEMPLO);

        assertEquals(15, resultado.ofertasImportadas());
        assertEquals(4, resultado.empresasCreadas());
        assertEquals(0, resultado.errores().size());
        assertEquals(15, ofertas.buscar(FiltroOfertas.vacio()).size());
        assertEquals(4, empresas.findAll().size());
    }

    @Test
    void importarDosVecesReutilizaLasEmpresas() {
        importacion.importar(CSV_EJEMPLO);
        ImportacionService.Resultado segunda = importacion.importar(CSV_EJEMPLO);

        assertEquals(0, segunda.empresasCreadas());
        assertEquals(30, ofertas.buscar(FiltroOfertas.vacio()).size());
        assertEquals(8, ofertas.buscar(new FiltroOfertas(Estado.PENDIENTE, null, null, null, null, null, null)).size());
    }

    @Test
    void lasLineasIncorrectasSeDescartanYSeInforman(@TempDir Path carpeta) throws IOException {
        Path csv = carpeta.resolve("mezcla.csv");
        Files.writeString(csv, """
                id;titulo;empresa;estado;fechaPostulacion;enlace;notas
                1;Dev;Indra;pendiente;01/08/2026;;
                2;Mala;Indra;quizas;01/08/2026;;
                3;Pocas;columnas
                """);

        ImportacionService.Resultado resultado = importacion.importar(csv);

        assertEquals(1, resultado.ofertasImportadas());
        assertEquals(2, resultado.errores().size());
    }

    @Test
    void archivoInexistenteLanzaError(@TempDir Path carpeta) {
        assertThrows(CandidaturaException.class, () -> importacion.importar(carpeta.resolve("no-existe.csv")));
    }
}
