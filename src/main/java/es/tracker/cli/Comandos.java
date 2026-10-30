package es.tracker.cli;

import es.tracker.exception.CandidaturaException;
import es.tracker.model.Candidatura;
import es.tracker.model.Estado;
import es.tracker.service.CandidaturaService;

import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Ejecuta la operación indicada en los argumentos y escribe el resultado en la salida.
 */
public class Comandos {

    private final CandidaturaService servicio;
    private final PrintStream salida;

    public Comandos(CandidaturaService servicio, PrintStream salida) {
        this.servicio = servicio;
        this.salida = salida;
    }

    /**
     * @param args argumentos ya interpretados
     * @throws CandidaturaException si la operación no existe o sus argumentos son incorrectos
     */
    public void ejecutar(Argumentos args) {
        switch (args.operacion()) {
            case "listar" -> listar(args);
            case "añadir", "anadir" -> anadir(args);
            case "ver" -> ver(args);
            case "borrar" -> borrar(args);
            case "importar" -> importar(args);
            default -> throw new CandidaturaException("Operación desconocida: '" + args.operacion() + "'.");
        }
    }

    /**
     * @return texto de ayuda con todas las operaciones
     */
    public static String ayuda() {
        return """
                Uso: java -jar tracker-candidaturas-1.0.jar <operación> [argumentos]

                Operaciones:
                  -listar [--estado X] [--empresa Y]
                  -añadir --titulo X --empresa Y --estado Z --fecha dd/MM/yyyy [--enlace URL] [--notas TEXTO]
                  -ver ID
                  -borrar ID
                  -importar archivo.csv

                Estados: pendiente, entrevista, rechazado, aceptado
                Archivo de datos: data/candidaturas.txt (se cambia con la variable CANDIDATURAS_FILE)""";
    }

    private void listar(Argumentos args) {
        Estado estado = args.opcion("estado").map(Estado::desdeTexto).orElse(null);
        String empresa = args.opcion("empresa").orElse(null);
        List<Candidatura> resultado = servicio.listar(estado, empresa);
        if (resultado.isEmpty()) {
            salida.println("No hay candidaturas que mostrar.");
            return;
        }
        salida.printf("%-4s %-30s %-20s %-11s %-10s%n", "ID", "TÍTULO", "EMPRESA", "ESTADO", "FECHA");
        resultado.forEach(c -> salida.printf("%-4d %-30s %-20s %-11s %-10s%n", c.id(), recortar(c.titulo(), 30),
                recortar(c.empresa(), 20), c.estado().getTexto(), c.fechaPostulacion().format(Candidatura.FORMATO_FECHA)));
        salida.println(resultado.size() + " candidatura(s).");
    }

    private void anadir(Argumentos args) {
        String titulo = args.opcionObligatoria("titulo");
        String empresa = args.opcionObligatoria("empresa");
        Estado estado = Estado.desdeTexto(args.opcionObligatoria("estado"));
        LocalDate fecha = args.opcion("fecha").map(Comandos::parsearFecha).orElse(LocalDate.now());
        Candidatura creada = servicio.anadir(titulo, empresa, estado, fecha,
                args.opcion("enlace").orElse(""), args.opcion("notas").orElse(""));
        salida.println("Candidatura añadida con ID " + creada.id() + ".");
    }

    private void ver(Argumentos args) {
        Candidatura c = servicio.ver(leerId(args));
        salida.println("ID:         " + c.id());
        salida.println("Título:     " + c.titulo());
        salida.println("Empresa:    " + c.empresa());
        salida.println("Estado:     " + c.estado().getTexto());
        salida.println("Fecha:      " + c.fechaPostulacion().format(Candidatura.FORMATO_FECHA));
        salida.println("Enlace:     " + (c.enlace().isEmpty() ? "-" : c.enlace()));
        salida.println("Notas:      " + (c.notas().isEmpty() ? "-" : c.notas()));
    }

    private void borrar(Argumentos args) {
        Candidatura eliminada = servicio.borrar(leerId(args));
        salida.println("Candidatura " + eliminada.id() + " (" + eliminada.titulo() + ") eliminada.");
    }

    private void importar(Argumentos args) {
        String ruta = args.posicional()
                .orElseThrow(() -> new CandidaturaException("Falta el archivo CSV a importar."));
        CandidaturaService.ResultadoImportacion resultado = servicio.importar(Path.of(ruta));
        salida.println(resultado.importadas() + " candidatura(s) importada(s).");
        resultado.errores().forEach(error -> salida.println("Descartada -> " + error));
    }

    private static int leerId(Argumentos args) {
        String texto = args.posicional().orElseThrow(() -> new CandidaturaException("Falta el ID de la candidatura."));
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new CandidaturaException("El ID '" + texto + "' no es un número válido.", e);
        }
    }

    private static LocalDate parsearFecha(String texto) {
        try {
            return LocalDate.parse(texto, Candidatura.FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            throw new CandidaturaException("Fecha '" + texto + "' no válida. Usa el formato dd/MM/yyyy.", e);
        }
    }

    private static String recortar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 1) + "…";
    }
}
