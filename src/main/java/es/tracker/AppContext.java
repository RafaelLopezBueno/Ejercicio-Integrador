package es.tracker;

import es.tracker.config.DatabaseConfig;
import es.tracker.db.Database;
import es.tracker.repository.JdbcEmpresaRepository;
import es.tracker.repository.JdbcEntrevistaRepository;
import es.tracker.repository.JdbcOfertaRepository;
import es.tracker.service.EmpresaService;
import es.tracker.service.EntrevistaService;
import es.tracker.service.ImportacionService;
import es.tracker.service.OfertaService;

/**
 * Agrupa los servicios de la aplicación para que los controladores de la interfaz puedan usarlos.
 */
public class AppContext {

    private final OfertaService ofertas;
    private final EmpresaService empresas;
    private final EntrevistaService entrevistas;
    private final ImportacionService importacion;

    private AppContext(OfertaService ofertas, EmpresaService empresas, EntrevistaService entrevistas,
                       ImportacionService importacion) {
        this.ofertas = ofertas;
        this.empresas = empresas;
        this.entrevistas = entrevistas;
        this.importacion = importacion;
    }

    /**
     * Conecta con la base de datos, crea las tablas si la configuración lo indica y construye los servicios.
     *
     * @param config parámetros de conexión
     * @return contexto listo para usar
     */
    public static AppContext crear(DatabaseConfig config) {
        Database database = new Database(config);
        if (config.init()) {
            database.crearEsquema();
        }
        JdbcEmpresaRepository empresaRepo = new JdbcEmpresaRepository(database);
        JdbcOfertaRepository ofertaRepo = new JdbcOfertaRepository(database);
        JdbcEntrevistaRepository entrevistaRepo = new JdbcEntrevistaRepository(database);
        return new AppContext(
                new OfertaService(database, ofertaRepo, empresaRepo, entrevistaRepo),
                new EmpresaService(database, empresaRepo, ofertaRepo, entrevistaRepo),
                new EntrevistaService(entrevistaRepo, ofertaRepo),
                new ImportacionService(database, empresaRepo, ofertaRepo));
    }

    public OfertaService ofertas() {
        return ofertas;
    }

    public EmpresaService empresas() {
        return empresas;
    }

    public EntrevistaService entrevistas() {
        return entrevistas;
    }

    public ImportacionService importacion() {
        return importacion;
    }
}
