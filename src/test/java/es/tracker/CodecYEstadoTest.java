package es.tracker;

import es.tracker.exception.CandidaturaException;
import es.tracker.model.Candidatura;
import es.tracker.model.Estado;
import es.tracker.repository.CandidaturaCodec;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodecYEstadoTest {

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
    void estadoRechazaValoresDesconocidos() {
        assertEquals(Estado.ACEPTADO, Estado.desdeTexto(" Aceptado "));
        assertThrows(CandidaturaException.class, () -> Estado.desdeTexto("quizás"));
    }
}
