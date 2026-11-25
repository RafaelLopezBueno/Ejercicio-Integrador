package es.tracker.service;

import es.tracker.db.Database;
import es.tracker.exception.CandidaturaException;
import es.tracker.model.Candidatura;
import es.tracker.model.Empresa;
import es.tracker.model.Oferta;
import es.tracker.repository.CandidaturaCodec;
import es.tracker.repository.EmpresaRepository;
import es.tracker.repository.OfertaRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Importa candidaturas desde un CSV con el formato {@code id;titulo;empresa;estado;fechaPostulacion;enlace;notas}.
 */
public class ImportacionService {

    private final Database database;
    private final EmpresaRepository empresas;
    private final OfertaRepository ofertas;

    public ImportacionService(Database database, EmpresaRepository empresas, OfertaRepository ofertas) {
        this.database = database;
        this.empresas = empresas;
        this.ofertas = ofertas;
    }

    /**
     * Importa el CSV en una sola transacción: las empresas que no existan se crean y cada línea válida se
     * añade como oferta. Las líneas con formato incorrecto se descartan y se informan; si falla la base de
     * datos no se guarda nada.
     *
     * @param csv ruta del archivo
     * @return resumen de la importación
     * @throws CandidaturaException si el archivo no existe, no se puede leer o falla la base de datos
     */
    public Resultado importar(Path csv) {
        List<String> lineas = leer(csv);
        return database.enTransaccion(() -> {
            Map<String, Integer> idsEmpresa = new HashMap<>();
            List<String> errores = new ArrayList<>();
            int ofertasImportadas = 0;
            int empresasCreadas = 0;
            for (int i = 0; i < lineas.size(); i++) {
                String linea = lineas.get(i);
                if (linea.isBlank() || linea.startsWith("id;")) {
                    continue;
                }
                Candidatura leida;
                try {
                    leida = CandidaturaCodec.parsear(linea, i + 1);
                } catch (CandidaturaException e) {
                    errores.add(e.getMessage());
                    continue;
                }
                String clave = leida.empresa().toLowerCase(Locale.ROOT);
                Integer idEmpresa = idsEmpresa.get(clave);
                if (idEmpresa == null) {
                    var existente = empresas.findByNombre(leida.empresa());
                    if (existente.isPresent()) {
                        idEmpresa = existente.get().id();
                    } else {
                        idEmpresa = empresas.save(new Empresa(null, leida.empresa(), null, null, LocalDate.now())).id();
                        empresasCreadas++;
                    }
                    idsEmpresa.put(clave, idEmpresa);
                }
                ofertas.save(new Oferta(null, idEmpresa, leida.titulo(), null, leida.estado(),
                        leida.fechaPostulacion(), leida.enlace(), null, leida.notas()));
                ofertasImportadas++;
            }
            return new Resultado(ofertasImportadas, empresasCreadas, errores);
        });
    }

    private static List<String> leer(Path csv) {
        try {
            return Files.readAllLines(csv, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            throw new CandidaturaException("No se encuentra el archivo " + csv);
        } catch (IOException | UncheckedIOException e) {
            throw new CandidaturaException("No se pudo leer el archivo " + csv + ": " + e.getMessage(), e);
        }
    }

    /**
     * Resumen de una importación.
     *
     * @param ofertasImportadas número de ofertas añadidas
     * @param empresasCreadas   número de empresas nuevas
     * @param errores           mensajes de las líneas descartadas
     */
    public record Resultado(int ofertasImportadas, int empresasCreadas, List<String> errores) {
    }
}
