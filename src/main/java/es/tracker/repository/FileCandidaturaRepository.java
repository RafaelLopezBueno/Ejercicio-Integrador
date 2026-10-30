package es.tracker.repository;

import es.tracker.exception.CandidaturaException;
import es.tracker.model.Candidatura;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio que guarda las candidaturas en un archivo de texto, una por línea.
 */
public class FileCandidaturaRepository implements CandidaturaRepository {

    private final Path archivo;

    /**
     * @param archivo ruta del archivo de datos; se crea al guardar si no existe
     */
    public FileCandidaturaRepository(Path archivo) {
        this.archivo = archivo;
    }

    @Override
    public List<Candidatura> findAll() {
        if (!Files.exists(archivo)) {
            return new ArrayList<>();
        }
        List<String> lineas = leerLineas(archivo);
        return Lectura.parsear(lineas);
    }

    @Override
    public void saveAll(List<Candidatura> candidaturas) {
        List<String> lineas = new ArrayList<>();
        lineas.add(CandidaturaCodec.CABECERA);
        candidaturas.stream().map(CandidaturaCodec::formatear).forEach(lineas::add);
        try {
            Path padre = archivo.toAbsolutePath().getParent();
            if (padre != null) {
                Files.createDirectories(padre);
            }
            Path temporal = Files.createTempFile(padre, "candidaturas", ".tmp");
            Files.write(temporal, lineas, StandardCharsets.UTF_8);
            Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new CandidaturaException("No se pudo escribir el archivo " + archivo + ": " + e.getMessage(), e);
        }
    }

    /**
     * Lee todas las líneas de un archivo de texto.
     *
     * @param ruta archivo a leer
     * @return líneas del archivo
     * @throws CandidaturaException si el archivo no existe o no se puede leer
     */
    public static List<String> leerLineas(Path ruta) {
        try {
            return Files.readAllLines(ruta, StandardCharsets.UTF_8);
        } catch (java.nio.file.NoSuchFileException e) {
            throw new CandidaturaException("No se encuentra el archivo " + ruta);
        } catch (IOException | UncheckedIOException e) {
            throw new CandidaturaException("No se pudo leer el archivo " + ruta + ": " + e.getMessage(), e);
        }
    }
}
