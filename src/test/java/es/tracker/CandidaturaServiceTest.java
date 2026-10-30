package es.tracker;

import es.tracker.exception.CandidaturaException;
import es.tracker.model.Candidatura;
import es.tracker.model.Estado;
import es.tracker.repository.CandidaturaRepository;
import es.tracker.service.CandidaturaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CandidaturaServiceTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 9, 15);

    private MemoriaRepository repositorio;
    private CandidaturaService servicio;

    @BeforeEach
    void preparar() {
        repositorio = new MemoriaRepository();
        servicio = new CandidaturaService(repositorio);
        servicio.anadir("Dev Java", "Indra", Estado.PENDIENTE, FECHA, "", "");
        servicio.anadir("QA", "Accenture", Estado.ENTREVISTA, FECHA, "", "");
        servicio.anadir("Backend", "Indra", Estado.ENTREVISTA, FECHA, "", "");
    }

    @Test
    void anadirAsignaIdsConsecutivos() {
        Candidatura nueva = servicio.anadir("Ops", "NTT Data", Estado.PENDIENTE, FECHA, "", "");
        assertEquals(4, nueva.id());
    }

    @Test
    void anadirSinTituloLanzaError() {
        assertThrows(CandidaturaException.class,
                () -> servicio.anadir(" ", "Indra", Estado.PENDIENTE, FECHA, "", ""));
    }

    @Test
    void listarSinFiltrosDevuelveTodas() {
        assertEquals(3, servicio.listar(null, null).size());
    }

    @Test
    void listarFiltraPorEstadoYEmpresaSinDistinguirMayusculas() {
        List<Candidatura> resultado = servicio.listar(Estado.ENTREVISTA, "indra");
        assertEquals(1, resultado.size());
        assertEquals("Backend", resultado.getFirst().titulo());
    }

    @Test
    void verDevuelveLaCandidaturaIndicada() {
        assertEquals("QA", servicio.ver(2).titulo());
    }

    @Test
    void verIdInexistenteLanzaError() {
        assertThrows(CandidaturaException.class, () -> servicio.ver(99));
    }

    @Test
    void borrarEliminaSoloLaIndicada() {
        servicio.borrar(2);
        assertEquals(2, repositorio.datos.size());
        assertThrows(CandidaturaException.class, () -> servicio.ver(2));
    }

    @Test
    void borrarIdInexistenteLanzaError() {
        assertThrows(CandidaturaException.class, () -> servicio.borrar(99));
    }

    @Test
    void importarConservaIdLibreYReasignaElOcupado(@TempDir Path carpeta) throws IOException {
        Path csv = carpeta.resolve("datos.csv");
        Files.writeString(csv, """
                id;titulo;empresa;estado;fechaPostulacion;enlace;notas
                2;Repetido;Indra;pendiente;01/08/2026;;
                10;Libre;Indra;aceptado;02/08/2026;;
                x;Mala;Indra;aceptado;02/08/2026;;
                """);
        CandidaturaService.ResultadoImportacion resultado = servicio.importar(csv);

        assertEquals(2, resultado.importadas());
        assertEquals(1, resultado.errores().size());
        assertTrue(repositorio.datos.stream().anyMatch(c -> c.id() == 10 && c.titulo().equals("Libre")));
        assertTrue(repositorio.datos.stream().anyMatch(c -> c.id() == 4 && c.titulo().equals("Repetido")));
    }

    @Test
    void importarArchivoInexistenteLanzaError(@TempDir Path carpeta) {
        assertThrows(CandidaturaException.class, () -> servicio.importar(carpeta.resolve("no-existe.csv")));
    }

    private static final class MemoriaRepository implements CandidaturaRepository {
        private List<Candidatura> datos = new ArrayList<>();

        @Override
        public List<Candidatura> findAll() {
            return new ArrayList<>(datos);
        }

        @Override
        public void saveAll(List<Candidatura> candidaturas) {
            datos = new ArrayList<>(candidaturas);
        }
    }
}
