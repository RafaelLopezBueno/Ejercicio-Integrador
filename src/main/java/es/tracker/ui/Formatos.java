package es.tracker.ui;

import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Formatos de fechas e importes usados en las pantallas.
 */
public final class Formatos {

    /** Formato de fecha de toda la aplicación. */
    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Locale ES = Locale.of("es", "ES");

    private Formatos() {
    }

    /**
     * @return la fecha como dd/MM/yyyy, o una cadena vacía si es nula
     */
    public static String fecha(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FECHA);
    }

    /**
     * @return el importe en euros sin decimales, o «—» si es nulo
     */
    public static String salario(BigDecimal salario) {
        return salario == null ? "—" : String.format(ES, "%,.0f €", salario);
    }

    /**
     * Convierte un texto con coma o punto decimal en un número.
     *
     * @param texto texto introducido por el usuario
     * @return el número, o {@code null} si el texto está vacío
     * @throws NumberFormatException si el texto no es un número
     */
    public static BigDecimal decimal(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return new BigDecimal(texto.trim().replace(',', '.'));
    }

    /**
     * @return convertidor para que los selectores de fecha usen dd/MM/yyyy; un texto inválido da {@code null}
     */
    public static StringConverter<LocalDate> convertidorFecha() {
        return new StringConverter<>() {
            @Override
            public String toString(LocalDate fecha) {
                return fecha(fecha);
            }

            @Override
            public LocalDate fromString(String texto) {
                if (texto == null || texto.isBlank()) {
                    return null;
                }
                try {
                    return LocalDate.parse(texto.trim(), FECHA);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        };
    }
}
