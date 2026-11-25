package es.tracker;

import es.tracker.config.DatabaseConfig;
import es.tracker.db.Database;
import es.tracker.exception.CandidaturaException;
import es.tracker.model.Empresa;
import es.tracker.model.Entrevista;
import es.tracker.model.Estado;
import es.tracker.model.FiltroOfertas;
import es.tracker.model.Oferta;
import es.tracker.model.ResultadoEntrevista;
import es.tracker.repository.EmpresaRepository;
import es.tracker.repository.EntrevistaRepository;
import es.tracker.repository.OfertaRepository;
import es.tracker.service.EmpresaService;
import es.tracker.service.EntrevistaService;
import es.tracker.service.OfertaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiciosTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 9, 15);

    @Mock
    private EmpresaRepository empresas;
    @Mock
    private OfertaRepository ofertas;
    @Mock
    private EntrevistaRepository entrevistas;

    private OfertaService ofertaService;
    private EmpresaService empresaService;
    private EntrevistaService entrevistaService;

    @BeforeEach
    void preparar() {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        Database database = new Database(new DatabaseConfig(url, "sa", "", false));
        ofertaService = new OfertaService(database, ofertas, empresas, entrevistas);
        empresaService = new EmpresaService(database, empresas, ofertas, entrevistas);
        entrevistaService = new EntrevistaService(entrevistas, ofertas);
    }

    @Test
    void guardarOfertaSinTituloLanzaErrorYNoTocaElRepositorio() {
        Oferta sinTitulo = new Oferta(null, 1, "  ", null, Estado.PENDIENTE, FECHA, null, null, null);
        assertThrows(CandidaturaException.class, () -> ofertaService.guardar(sinTitulo));
        verify(ofertas, never()).save(any());
    }

    @Test
    void guardarOfertaConEmpresaInexistenteLanzaError() {
        when(empresas.findById(7)).thenReturn(Optional.empty());
        Oferta oferta = new Oferta(null, 7, "Dev", null, Estado.PENDIENTE, FECHA, null, null, null);
        assertThrows(CandidaturaException.class, () -> ofertaService.guardar(oferta));
    }

    @Test
    void guardarOfertaRechazaSalarioNegativoYEnlaceInvalido() {
        Oferta negativa = new Oferta(null, 1, "Dev", null, Estado.PENDIENTE, FECHA, null, new BigDecimal("-1"), null);
        Oferta enlaceMalo = new Oferta(null, 1, "Dev", null, Estado.PENDIENTE, FECHA, "indra.com", null, null);
        assertThrows(CandidaturaException.class, () -> ofertaService.guardar(negativa));
        assertThrows(CandidaturaException.class, () -> ofertaService.guardar(enlaceMalo));
    }

    @Test
    void guardarOfertaValidaLlamaAlRepositorio() {
        Oferta oferta = new Oferta(null, 1, "Dev", null, Estado.PENDIENTE, FECHA, "https://indra.com", null, null);
        when(empresas.findById(1)).thenReturn(Optional.of(new Empresa(1, "Indra", null, null, FECHA)));
        when(ofertas.save(oferta)).thenReturn(oferta.conEstado(Estado.PENDIENTE));

        assertEquals("Dev", ofertaService.guardar(oferta).titulo());
        verify(ofertas).save(oferta);
    }

    @Test
    void buscarRechazaRangosInvertidos() {
        FiltroOfertas salarios = new FiltroOfertas(null, null, null, new BigDecimal("50000"), new BigDecimal("30000"), null, null);
        FiltroOfertas fechas = new FiltroOfertas(null, null, null, null, null, FECHA, FECHA.minusDays(1));
        assertThrows(CandidaturaException.class, () -> ofertaService.buscar(salarios));
        assertThrows(CandidaturaException.class, () -> ofertaService.buscar(fechas));
    }

    @Test
    void cambiarEstadoDeOfertaInexistenteLanzaError() {
        when(ofertas.actualizarEstado(5, Estado.ACEPTADO)).thenReturn(false);
        assertThrows(CandidaturaException.class, () -> ofertaService.cambiarEstado(5, Estado.ACEPTADO));
    }

    @Test
    void eliminarOfertaBorraPrimeroSusEntrevistas() {
        when(ofertas.deleteById(3)).thenReturn(true);
        ofertaService.eliminar(3);

        InOrder orden = inOrder(entrevistas, ofertas);
        orden.verify(entrevistas).deleteByOferta(3);
        orden.verify(ofertas).deleteById(3);
    }

    @Test
    void guardarEmpresaValidaNombreYWeb() {
        assertThrows(CandidaturaException.class,
                () -> empresaService.guardar(new Empresa(null, "", null, null, null)));
        assertThrows(CandidaturaException.class,
                () -> empresaService.guardar(new Empresa(null, "Indra", null, "indra.com", null)));
        verify(empresas, never()).save(any());
    }

    @Test
    void eliminarEmpresaConOfertasSinConfirmarLanzaErrorYNoBorraNada() {
        when(empresas.findById(1)).thenReturn(Optional.of(new Empresa(1, "Indra", null, null, FECHA)));
        when(ofertas.countByEmpresa(1)).thenReturn(2);

        assertThrows(CandidaturaException.class, () -> empresaService.eliminar(1, false));
        verify(empresas, never()).deleteById(1);
    }

    @Test
    void eliminarEmpresaEnCascadaBorraEntrevistasOfertasYEmpresaEnOrden() {
        when(empresas.findById(1)).thenReturn(Optional.of(new Empresa(1, "Indra", null, null, FECHA)));
        when(ofertas.countByEmpresa(1)).thenReturn(2);

        assertEquals(2, empresaService.eliminar(1, true));

        InOrder orden = inOrder(entrevistas, ofertas, empresas);
        orden.verify(entrevistas).deleteByEmpresa(1);
        orden.verify(ofertas).deleteByEmpresa(1);
        orden.verify(empresas).deleteById(1);
    }

    @Test
    void guardarEntrevistaExigeTipoYOfertaExistente() {
        Entrevista sinTipo = new Entrevista(null, 1, FECHA, " ", null, ResultadoEntrevista.PENDIENTE);
        assertThrows(CandidaturaException.class, () -> entrevistaService.guardar(sinTipo));

        Entrevista valida = new Entrevista(null, 9, FECHA, "técnica", null, ResultadoEntrevista.PENDIENTE);
        when(ofertas.findById(9)).thenReturn(Optional.empty());
        assertThrows(CandidaturaException.class, () -> entrevistaService.guardar(valida));
    }

    @Test
    void eliminarEntrevistaInexistenteLanzaError() {
        when(entrevistas.deleteById(4)).thenReturn(false);
        assertThrows(CandidaturaException.class, () -> entrevistaService.eliminar(4));
    }
}
