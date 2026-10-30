package es.tracker;

import es.tracker.cli.Argumentos;
import es.tracker.cli.Comandos;
import es.tracker.exception.CandidaturaException;
import es.tracker.repository.FileCandidaturaRepository;
import es.tracker.service.CandidaturaService;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Punto de entrada de la aplicación: ejecuta una operación y termina.
 */
public final class Main {

    private static final String ARCHIVO_POR_DEFECTO = "data/candidaturas.txt";

    private Main() {
    }

    public static void main(String[] args) {
        PrintStream salida = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        PrintStream errores = new PrintStream(System.err, true, StandardCharsets.UTF_8);
        if (args.length == 0) {
            errores.println("Error: no se ha indicado ninguna operación.");
            errores.println(Comandos.ayuda());
            System.exit(1);
        }
        try {
            String ruta = System.getenv().getOrDefault("CANDIDATURAS_FILE", ARCHIVO_POR_DEFECTO);
            CandidaturaService servicio = new CandidaturaService(new FileCandidaturaRepository(Path.of(ruta)));
            new Comandos(servicio, salida).ejecutar(Argumentos.interpretar(args));
        } catch (CandidaturaException e) {
            errores.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
