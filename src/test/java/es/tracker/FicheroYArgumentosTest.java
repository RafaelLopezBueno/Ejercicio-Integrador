package es.tracker;

import es.tracker.cli.Argumentos;
import es.tracker.exception.CandidaturaException;
import es.tracker.model.Candidatura;
import es.tracker.model.Estado;
import es.tracker.repository.CandidaturaCodec;
import es.tracker.repository.FileCandidaturaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FicheroYArgumentosTest {

    private static final Candidatura EJEMPLO = new Candidatura(1, "Desarrollador Java", "Indra",
            Estado.PENDIENTE, LocalDate.of(2026, 9, 15), "https://ejemplo.es", "Contacto por LinkedIn");

    @Test
    void codecDevuelveLaMismaCandidaturaTrasFormatearYParsear() {
        String linea = CandidaturaCodec.formatear(EJEMPLO);
        assertEquals("1;Desarrollador Java;Indra;pendiente;15/09/2026;https://ejemplo.es;Contacto por LinkedIn", linea);
        assertEquals(EJEMPLO, CandidaturaCodec.parsear(linea, 1));
    }

    @Test
    void codecSustituyeElSeparadorDentroDeLosCampos() {
        Candidatura conPuntoYComa = new Candidatura(2, "A;B", "Indra", Estado.RECHAZADO, LocalDate.of(2026, 1, 1), "", "x;y");
        String linea = CandidaturaCodec.formatear(conPuntoYComa);
        assertEquals("A,B", CandidaturaCodec.parsear(linea, 1).titulo());
    }

    @Test
    void codecRechazaLineasConPocosCampos() {
        assertThrows(CandidaturaException.class, () -> CandidaturaCodec.parsear("1;solo;tres", 5));
    }

    @Test
    void codecRechazaFechasConFormatoIncorrecto() {
        CandidaturaException error = assertThrows(CandidaturaException.class,
                () -> CandidaturaCodec.parsear("1;t;e;pendiente;2026-09-15;;", 3));
        assertTrue(error.getMessage().contains("dd/MM/yyyy"));
    }

    @Test
    void repositorioGuardaYRecuperaDesdeArchivo(@TempDir Path carpeta) {
        FileCandidaturaRepository repositorio = new FileCandidaturaRepository(carpeta.resolve("datos/candidaturas.txt"));
        assertTrue(repositorio.findAll().isEmpty());

        repositorio.saveAll(List.of(EJEMPLO));
        assertEquals(List.of(EJEMPLO), repositorio.findAll());
    }

    @Test
    void argumentosInterpretaOperacionPosicionalYOpciones() {
        Argumentos args = Argumentos.interpretar(new String[]{"-listar", "--estado", "pendiente", "--empresa", "Indra"});
        assertEquals("listar", args.operacion());
        assertEquals("pendiente", args.opcion("estado").orElseThrow());
        assertFalse(args.posicional().isPresent());

        assertEquals("13", Argumentos.interpretar(new String[]{"-ver", "13"}).posicional().orElseThrow());
    }

    @Test
    void argumentosFallaSiFaltaElValorDeUnaOpcion() {
        assertThrows(CandidaturaException.class, () -> Argumentos.interpretar(new String[]{"-listar", "--estado"}));
    }

    @Test
    void estadoRechazaValoresDesconocidos() {
        assertEquals(Estado.ACEPTADO, Estado.desdeTexto(" Aceptado "));
        assertThrows(CandidaturaException.class, () -> Estado.desdeTexto("quizás"));
    }
}
