package es.tracker.service;

import es.tracker.exception.CandidaturaException;
import es.tracker.model.Candidatura;
import es.tracker.model.Estado;
import es.tracker.repository.CandidaturaCodec;
import es.tracker.repository.CandidaturaRepository;
import es.tracker.repository.FileCandidaturaRepository;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lógica de negocio de las candidaturas: filtrado, alta, consulta, borrado e importación.
 */
public class CandidaturaService {

    private final CandidaturaRepository repositorio;

    public CandidaturaService(CandidaturaRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Lista las candidaturas aplicando, si se indican, los filtros de estado y empresa.
     *
     * @param estado  estado exacto o {@code null} para no filtrar
     * @param empresa texto contenido en el nombre de la empresa (sin distinguir mayúsculas) o {@code null}
     * @return candidaturas que cumplen todos los filtros
     */
    public List<Candidatura> listar(Estado estado, String empresa) {
        return repositorio.findAll().stream()
                .filter(c -> estado == null || c.estado() == estado)
                .filter(c -> empresa == null || c.empresa().toLowerCase().contains(empresa.trim().toLowerCase()))
                .toList();
    }

    /**
     * Añade una candidatura con el siguiente identificador libre.
     *
     * @return candidatura creada, ya con su id
     * @throws CandidaturaException si falta el título o la empresa
     */
    public Candidatura anadir(String titulo, String empresa, Estado estado, LocalDate fecha,
                              String enlace, String notas) {
        exigirTexto(titulo, "titulo");
        exigirTexto(empresa, "empresa");
        List<Candidatura> todas = repositorio.findAll();
        Candidatura nueva = new Candidatura(siguienteId(todas), titulo.trim(), empresa.trim(), estado, fecha,
                enlace == null ? "" : enlace, notas == null ? "" : notas);
        todas.add(nueva);
        repositorio.saveAll(todas);
        return nueva;
    }

    /**
     * @param id identificador buscado
     * @return candidatura con ese id
     * @throws CandidaturaException si no existe
     */
    public Candidatura ver(int id) {
        return repositorio.findAll().stream()
                .filter(c -> c.id() == id)
                .findFirst()
                .orElseThrow(() -> new CandidaturaException("No existe ninguna candidatura con ID " + id));
    }

    /**
     * Elimina la candidatura con el id indicado.
     *
     * @return candidatura eliminada
     * @throws CandidaturaException si no existe
     */
    public Candidatura borrar(int id) {
        List<Candidatura> todas = repositorio.findAll();
        Candidatura objetivo = todas.stream()
                .filter(c -> c.id() == id)
                .findFirst()
                .orElseThrow(() -> new CandidaturaException("No existe ninguna candidatura con ID " + id));
        todas.remove(objetivo);
        repositorio.saveAll(todas);
        return objetivo;
    }

    /**
     * Importa candidaturas desde un CSV con el mismo formato que el archivo de datos.
     * Conserva el id del CSV salvo que ya esté en uso, en cuyo caso asigna uno nuevo.
     *
     * @param csv ruta del archivo CSV
     * @return resultado con las importadas y las líneas descartadas
     * @throws CandidaturaException si el archivo no existe o no se puede leer
     */
    public ResultadoImportacion importar(Path csv) {
        List<String> lineas = FileCandidaturaRepository.leerLineas(csv);
        List<Candidatura> existentes = repositorio.findAll();
        Set<Integer> idsUsados = new HashSet<>();
        existentes.forEach(c -> idsUsados.add(c.id()));

        int importadas = 0;
        List<String> errores = new ArrayList<>();
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            if (linea.isBlank() || linea.startsWith("id;")) {
                continue;
            }
            try {
                Candidatura leida = CandidaturaCodec.parsear(linea, i + 1);
                if (idsUsados.contains(leida.id())) {
                    leida = leida.conId(siguienteId(existentes));
                }
                idsUsados.add(leida.id());
                existentes.add(leida);
                importadas++;
            } catch (CandidaturaException e) {
                errores.add(e.getMessage());
            }
        }
        if (importadas > 0) {
            existentes.sort(Comparator.comparingInt(Candidatura::id));
            repositorio.saveAll(existentes);
        }
        return new ResultadoImportacion(importadas, errores);
    }

    private static int siguienteId(List<Candidatura> candidaturas) {
        return candidaturas.stream().mapToInt(Candidatura::id).max().orElse(0) + 1;
    }

    private static void exigirTexto(String valor, String nombre) {
        if (valor == null || valor.isBlank()) {
            throw new CandidaturaException("Falta el argumento obligatorio --" + nombre);
        }
    }

    /**
     * Resumen de una importación.
     *
     * @param importadas número de candidaturas añadidas
     * @param errores    mensajes de las líneas que no se pudieron leer
     */
    public record ResultadoImportacion(int importadas, List<String> errores) {
    }
}
