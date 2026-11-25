package es.tracker.service;

import es.tracker.db.Database;
import es.tracker.exception.CandidaturaException;
import es.tracker.model.Empresa;
import es.tracker.model.EmpresaResumen;
import es.tracker.repository.EmpresaRepository;
import es.tracker.repository.EntrevistaRepository;
import es.tracker.repository.OfertaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de negocio de las empresas.
 */
public class EmpresaService {

    private final Database database;
    private final EmpresaRepository empresas;
    private final OfertaRepository ofertas;
    private final EntrevistaRepository entrevistas;

    public EmpresaService(Database database, EmpresaRepository empresas, OfertaRepository ofertas,
                          EntrevistaRepository entrevistas) {
        this.database = database;
        this.empresas = empresas;
        this.ofertas = ofertas;
        this.entrevistas = entrevistas;
    }

    /**
     * @return todas las empresas ordenadas por nombre
     */
    public List<Empresa> listar() {
        return empresas.findAll();
    }

    /**
     * @return todas las empresas con su número de ofertas
     */
    public List<EmpresaResumen> listarConTotales() {
        return empresas.findAllConTotales();
    }

    /**
     * @throws CandidaturaException si no existe
     */
    public Empresa obtener(int id) {
        return empresas.findById(id)
                .orElseThrow(() -> new CandidaturaException("No existe ninguna empresa con ID " + id));
    }

    /**
     * Crea o actualiza una empresa tras validar sus datos. Si no trae fecha de registro se usa la de hoy.
     *
     * @param empresa datos a guardar; con id se actualiza y sin id se crea
     * @return empresa guardada
     * @throws CandidaturaException si los datos no son válidos o el nombre ya existe
     */
    public Empresa guardar(Empresa empresa) {
        Validaciones.textoObligatorio(empresa.nombre(), "nombre", 120);
        Validaciones.longitudMaxima(empresa.sector(), "sector", 120);
        Validaciones.longitudMaxima(empresa.web(), "web", 255);
        Validaciones.urlOpcional(empresa.web(), "web");
        LocalDate fecha = empresa.fechaRegistro() != null ? empresa.fechaRegistro() : LocalDate.now();
        return empresas.save(new Empresa(empresa.id(), empresa.nombre(), empresa.sector(), empresa.web(), fecha));
    }

    /**
     * Elimina una empresa. Si tiene ofertas, solo se elimina cuando se indica expresamente, y entonces se
     * borran también sus entrevistas y ofertas dentro de una misma transacción.
     *
     * @param id             empresa a eliminar
     * @param incluirOfertas {@code true} para borrar también sus ofertas y entrevistas
     * @return número de ofertas eliminadas junto con la empresa
     * @throws CandidaturaException si no existe o tiene ofertas y no se autorizó su borrado
     */
    public int eliminar(int id, boolean incluirOfertas) {
        return database.enTransaccion(() -> {
            obtener(id);
            int total = ofertas.countByEmpresa(id);
            if (total > 0 && !incluirOfertas) {
                throw new CandidaturaException("La empresa tiene " + total + " oferta(s) asociada(s). "
                        + "Elimínalas antes o confirma el borrado en cascada.");
            }
            entrevistas.deleteByEmpresa(id);
            ofertas.deleteByEmpresa(id);
            empresas.deleteById(id);
            return total;
        });
    }
}
