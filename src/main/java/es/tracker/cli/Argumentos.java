package es.tracker.cli;

import es.tracker.exception.CandidaturaException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Argumentos de línea de comandos ya interpretados: una operación, un valor posicional opcional
 * y opciones con la forma {@code --nombre valor}.
 */
public final class Argumentos {

    private final String operacion;
    private final String posicional;
    private final Map<String, String> opciones;

    private Argumentos(String operacion, String posicional, Map<String, String> opciones) {
        this.operacion = operacion;
        this.posicional = posicional;
        this.opciones = opciones;
    }

    /**
     * Interpreta los argumentos de {@code main}. La operación puede escribirse con uno o dos guiones
     * (o sin ellos), por ejemplo {@code -listar}.
     *
     * @param args argumentos recibidos
     * @return argumentos interpretados
     * @throws CandidaturaException si no hay operación o una opción no tiene valor
     */
    public static Argumentos interpretar(String[] args) {
        if (args.length == 0) {
            throw new CandidaturaException("No se ha indicado ninguna operación.");
        }
        String operacion = args[0].replaceFirst("^-{1,2}", "").toLowerCase();
        String posicional = null;
        Map<String, String> opciones = new HashMap<>();
        for (int i = 1; i < args.length; i++) {
            String actual = args[i];
            if (actual.startsWith("--")) {
                String nombre = actual.substring(2).toLowerCase();
                if (i + 1 >= args.length || args[i + 1].startsWith("--")) {
                    throw new CandidaturaException("Falta el valor de la opción --" + nombre);
                }
                opciones.put(nombre, args[++i]);
            } else if (posicional == null) {
                posicional = actual;
            } else {
                throw new CandidaturaException("Argumento inesperado: " + actual);
            }
        }
        return new Argumentos(operacion, posicional, opciones);
    }

    public String operacion() {
        return operacion;
    }

    /**
     * @return valor sin nombre que sigue a la operación (id o ruta de archivo), si existe
     */
    public Optional<String> posicional() {
        return Optional.ofNullable(posicional);
    }

    /**
     * @param nombre nombre de la opción sin guiones
     * @return valor de la opción, si se indicó
     */
    public Optional<String> opcion(String nombre) {
        return Optional.ofNullable(opciones.get(nombre));
    }

    /**
     * @param nombre nombre de la opción sin guiones
     * @return valor de la opción
     * @throws CandidaturaException si no se indicó
     */
    public String opcionObligatoria(String nombre) {
        return opcion(nombre).orElseThrow(() -> new CandidaturaException("Falta el argumento obligatorio --" + nombre));
    }
}
