package es.tracker.service;

import es.tracker.db.Database;
import es.tracker.exception.CandidaturaException;
import es.tracker.model.Estado;
import es.tracker.model.FiltroOfertas;
import es.tracker.model.Oferta;
import es.tracker.model.OfertaVista;
import es.tracker.repository.EmpresaRepository;
import es.tracker.repository.EntrevistaRepository;
import es.tracker.repository.OfertaRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lógica de negocio de las ofertas (candidaturas).
 */
public class OfertaService {

    private final Database database;
    private final OfertaRepository ofertas;
    private final EmpresaRepository empresas;
    private final EntrevistaRepository entrevistas;

    public OfertaService(Database database, OfertaRepository ofertas, EmpresaRepository empresas,
                         EntrevistaRepository entrevistas) {
        this.database = database;
        this.ofertas = ofertas;
        this.empresas = empresas;
        this.entrevistas = entrevistas;
    }

    /**
     * Busca ofertas combinando los criterios del filtro.
     *
     * @throws CandidaturaException si el rango de salarios o de fechas está invertido
     */
    public List<OfertaVista> buscar(FiltroOfertas filtro) {
        if (filtro.salarioMin() != null && filtro.salarioMax() != null
                && filtro.salarioMin().compareTo(filtro.salarioMax()) > 0) {
            throw new CandidaturaException("El salario mínimo no puede ser mayor que el máximo");
        }
        if (filtro.fechaDesde() != null && filtro.fechaHasta() != null && filtro.fechaDesde().isAfter(filtro.fechaHasta())) {
            throw new CandidaturaException("La fecha 'desde' no puede ser posterior a la fecha 'hasta'");
        }
        return ofertas.buscar(filtro);
    }

    /**
     * @return la oferta con el nombre de su empresa
     * @throws CandidaturaException si no existe
     */
    public OfertaVista detalle(int id) {
        return ofertas.findVistaById(id)
                .orElseThrow(() -> new CandidaturaException("No existe ninguna candidatura con ID " + id));
    }

    /**
     * Crea o actualiza una oferta tras validar sus datos.
     *
     * @param oferta datos a guardar; con id se actualiza y sin id se crea
     * @return oferta guardada
     * @throws CandidaturaException si los datos no son válidos o la empresa no existe
     */
    public Oferta guardar(Oferta oferta) {
        Validaciones.textoObligatorio(oferta.titulo(), "título", 160);
        Validaciones.obligatorio(oferta.estado(), "estado");
        Validaciones.obligatorio(oferta.fechaPostulacion(), "fecha de postulación");
        Validaciones.longitudMaxima(oferta.enlace(), "enlace", 500);
        Validaciones.urlOpcional(oferta.enlace(), "enlace");
        if (oferta.salario() != null && oferta.salario().compareTo(BigDecimal.ZERO) < 0) {
            throw new CandidaturaException("El salario no puede ser negativo");
        }
        if (empresas.findById(oferta.idEmpresa()).isEmpty()) {
            throw new CandidaturaException("La empresa indicada no existe");
        }
        return ofertas.save(oferta);
    }

    /**
     * Cambia el estado de una candidatura.
     *
     * @throws CandidaturaException si la oferta no existe
     */
    public void cambiarEstado(int id, Estado estado) {
        Validaciones.obligatorio(estado, "estado");
        if (!ofertas.actualizarEstado(id, estado)) {
            throw new CandidaturaException("No existe ninguna candidatura con ID " + id);
        }
    }

    /**
     * Elimina una oferta y sus entrevistas en una misma transacción.
     *
     * @throws CandidaturaException si no existe
     */
    public void eliminar(int id) {
        database.enTransaccion(() -> {
            entrevistas.deleteByOferta(id);
            if (!ofertas.deleteById(id)) {
                throw new CandidaturaException("No existe ninguna candidatura con ID " + id);
            }
            return null;
        });
    }
}
