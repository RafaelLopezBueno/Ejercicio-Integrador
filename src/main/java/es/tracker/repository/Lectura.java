package es.tracker.repository;

import es.tracker.model.Candidatura;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilidad para convertir las líneas de un archivo en candidaturas.
 */
final class Lectura {

    private Lectura() {
    }

    /**
     * Ignora la cabecera y las líneas vacías.
     *
     * @param lineas contenido del archivo
     * @return candidaturas leídas
     */
    static List<Candidatura> parsear(List<String> lineas) {
        List<Candidatura> resultado = new ArrayList<>();
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            if (linea.isBlank() || linea.startsWith("id;")) {
                continue;
            }
            resultado.add(CandidaturaCodec.parsear(linea, i + 1));
        }
        return resultado;
    }
}
