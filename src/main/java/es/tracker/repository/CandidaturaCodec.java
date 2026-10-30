package es.tracker.repository;

import es.tracker.exception.CandidaturaException;
import es.tracker.model.Candidatura;
import es.tracker.model.Estado;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Convierte candidaturas a líneas de texto con campos separados por punto y coma y viceversa.
 */
public final class CandidaturaCodec {

    /** Cabecera de los archivos de datos. */
    public static final String CABECERA = "id;titulo;empresa;estado;fechaPostulacion;enlace;notas";

    private static final String SEPARADOR = ";";
    private static final int CAMPOS = 7;

    private CandidaturaCodec() {
    }

    /**
     * @param candidatura candidatura a serializar
     * @return línea de texto lista para escribir en el archivo
     */
    public static String formatear(Candidatura candidatura) {
        return String.join(SEPARADOR,
                String.valueOf(candidatura.id()),
                limpiar(candidatura.titulo()),
                limpiar(candidatura.empresa()),
                candidatura.estado().getTexto(),
                candidatura.fechaPostulacion().format(Candidatura.FORMATO_FECHA),
                limpiar(candidatura.enlace()),
                limpiar(candidatura.notas()));
    }

    /**
     * @param linea       línea del archivo
     * @param numeroLinea número de línea, usado solo en los mensajes de error
     * @return candidatura leída
     * @throws CandidaturaException si la línea no tiene el formato esperado
     */
    public static Candidatura parsear(String linea, int numeroLinea) {
        String[] campos = linea.split(SEPARADOR, -1);
        if (campos.length < CAMPOS) {
            throw new CandidaturaException("Línea " + numeroLinea + " incorrecta: se esperaban "
                    + CAMPOS + " campos y hay " + campos.length);
        }
        try {
            int id = Integer.parseInt(campos[0].trim());
            Estado estado = Estado.desdeTexto(campos[3]);
            LocalDate fecha = LocalDate.parse(campos[4].trim(), Candidatura.FORMATO_FECHA);
            String notas = String.join(SEPARADOR, java.util.Arrays.copyOfRange(campos, 6, campos.length));
            return new Candidatura(id, campos[1].trim(), campos[2].trim(), estado, fecha, campos[5].trim(), notas.trim());
        } catch (NumberFormatException e) {
            throw new CandidaturaException("Línea " + numeroLinea + " incorrecta: el id '" + campos[0] + "' no es un número", e);
        } catch (DateTimeParseException e) {
            throw new CandidaturaException("Línea " + numeroLinea + " incorrecta: la fecha '" + campos[4]
                    + "' no tiene el formato dd/MM/yyyy", e);
        } catch (CandidaturaException e) {
            throw new CandidaturaException("Línea " + numeroLinea + " incorrecta: " + e.getMessage(), e);
        }
    }

    private static String limpiar(String texto) {
        return texto == null ? "" : texto.replace(SEPARADOR, ",").replace("\r", " ").replace("\n", " ").trim();
    }
}
