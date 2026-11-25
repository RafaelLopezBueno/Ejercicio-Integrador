package es.tracker;

import es.tracker.config.DatabaseConfig;
import es.tracker.db.Database;
import es.tracker.exception.CandidaturaException;
import es.tracker.model.Empresa;
import es.tracker.model.EmpresaResumen;
import es.tracker.model.Entrevista;
import es.tracker.model.Estado;
import es.tracker.model.FiltroOfertas;
import es.tracker.model.Oferta;
import es.tracker.model.OfertaVista;
import es.tracker.model.ResultadoEntrevista;
import es.tracker.repository.JdbcEmpresaRepository;
import es.tracker.repository.JdbcEntrevistaRepository;
import es.tracker.repository.JdbcOfertaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoriosJdbcTest {

    private JdbcEmpresaRepository empresas;
    private JdbcOfertaRepository ofertas;
    private JdbcEntrevistaRepository entrevistas;
    private Empresa indra;
    private Empresa accenture;

    @BeforeEach
    void preparar() {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        Database database = new Database(new DatabaseConfig(url, "sa", "", true));
        database.crearEsquema();
        empresas = new JdbcEmpresaRepository(database);
        ofertas = new JdbcOfertaRepository(database);
        entrevistas = new JdbcEntrevistaRepository(database);

        indra = empresas.save(new Empresa(null, "Indra", "Consultoría", "indracompany.com", LocalDate.of(2026, 7, 1)));
        accenture = empresas.save(new Empresa(null, "Accenture", null, null, LocalDate.of(2026, 7, 2)));
        guardar(indra, "Dev Java", Estado.PENDIENTE, "2026-08-01", "30000");
        guardar(indra, "Backend Spring", Estado.ENTREVISTA, "2026-09-10", "42000");
        guardar(accenture, "Dev Java Senior", Estado.ENTREVISTA, "2026-09-20", "55000");
        guardar(accenture, "QA", Estado.RECHAZADO, "2026-10-05", null);
    }

    @Test
    void guardarEmpresaAsignaIdYPermiteBuscarlaSinDistinguirMayusculas() {
        assertNotNull(indra.id());
        assertEquals(indra, empresas.findByNombre("INDRA").orElseThrow());
    }

    @Test
    void empresaConNombreRepetidoLanzaError() {
        assertThrows(CandidaturaException.class,
                () -> empresas.save(new Empresa(null, "indra", null, null, LocalDate.now())));
    }

    @Test
    void actualizarEmpresaCambiaSusDatos() {
        Empresa cambiada = empresas.save(new Empresa(indra.id(), "Indra Sistemas", "TI", null, indra.fechaRegistro()));
        assertEquals("Indra Sistemas", empresas.findById(indra.id()).orElseThrow().nombre());
        assertEquals("TI", cambiada.sector());
    }

    @Test
    void empresasConTotalesUsaJoinYCuentaOfertas() {
        List<EmpresaResumen> resumenes = empresas.findAllConTotales();
        assertEquals(2, resumenes.size());
        assertEquals("Accenture", resumenes.getFirst().empresa().nombre());
        assertEquals(2, resumenes.getFirst().totalOfertas());
    }

    @Test
    void noSePuedeEliminarUnaEmpresaConOfertas() {
        assertThrows(CandidaturaException.class, () -> empresas.deleteById(indra.id()));
    }

    @Test
    void ofertaConEmpresaInexistenteLanzaError() {
        assertThrows(CandidaturaException.class, () -> ofertas.save(nueva(new Empresa(999, "x", null, null, null),
                "Fantasma", Estado.PENDIENTE, "2026-10-10", null)));
    }

    @Test
    void buscarSinFiltrosDevuelveTodasConSuEmpresaOrdenadasPorFecha() {
        List<OfertaVista> todas = ofertas.buscar(FiltroOfertas.vacio());
        assertEquals(4, todas.size());
        assertEquals("QA", todas.getFirst().oferta().titulo());
        assertEquals("Accenture", todas.getFirst().empresaNombre());
    }

    @Test
    void buscarCombinaEmpresaEstadoYSalario() {
        FiltroOfertas filtro = new FiltroOfertas(Estado.ENTREVISTA, "indra", null, new BigDecimal("40000"), null, null, null);
        List<OfertaVista> resultado = ofertas.buscar(filtro);
        assertEquals(1, resultado.size());
        assertEquals("Backend Spring", resultado.getFirst().oferta().titulo());
    }

    @Test
    void buscarCombinaFechaYEmpresa() {
        FiltroOfertas filtro = new FiltroOfertas(null, "accenture", null, null, null,
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));
        assertEquals(List.of("Dev Java Senior"),
                ofertas.buscar(filtro).stream().map(v -> v.oferta().titulo()).toList());
    }

    @Test
    void filtroPorSalarioExcluyeLasOfertasSinSalario() {
        FiltroOfertas filtro = new FiltroOfertas(null, null, null, BigDecimal.ZERO, new BigDecimal("100000"), null, null);
        assertEquals(3, ofertas.buscar(filtro).size());
    }

    @Test
    void buscarPorTextoDelTitulo() {
        assertEquals(2, ofertas.buscar(new FiltroOfertas(null, null, "java", null, null, null, null)).size());
    }

    @Test
    void cambiarEstadoActualizaLaOferta() {
        int id = ofertas.buscar(FiltroOfertas.vacio()).getFirst().oferta().id();
        assertTrue(ofertas.actualizarEstado(id, Estado.ACEPTADO));
        assertEquals(Estado.ACEPTADO, ofertas.findById(id).orElseThrow().estado());
        assertFalse(ofertas.actualizarEstado(9999, Estado.ACEPTADO));
    }

    @Test
    void crudDeEntrevistas() {
        int idOferta = ofertas.buscar(FiltroOfertas.vacio()).getLast().oferta().id();
        Entrevista creada = entrevistas.save(new Entrevista(null, idOferta, LocalDate.of(2026, 9, 12), "técnica",
                null, ResultadoEntrevista.PENDIENTE));
        assertEquals(1, entrevistas.findByOferta(idOferta).size());

        entrevistas.save(new Entrevista(creada.id(), idOferta, creada.fecha(), "técnica", "Fue bien",
                ResultadoEntrevista.APROBADA));
        assertEquals(ResultadoEntrevista.APROBADA, entrevistas.findById(creada.id()).orElseThrow().resultado());

        assertEquals(1, entrevistas.findAllVistas().size());
        assertTrue(entrevistas.deleteById(creada.id()));
        assertTrue(entrevistas.findByOferta(idOferta).isEmpty());
    }

    private void guardar(Empresa empresa, String titulo, Estado estado, String fecha, String salario) {
        ofertas.save(nueva(empresa, titulo, estado, fecha, salario));
    }

    private static Oferta nueva(Empresa empresa, String titulo, Estado estado, String fecha, String salario) {
        return new Oferta(null, empresa.id(), titulo, null, estado, LocalDate.parse(fecha), null,
                salario == null ? null : new BigDecimal(salario), null);
    }
}
