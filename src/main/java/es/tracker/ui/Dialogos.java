package es.tracker.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Cuadros de diálogo estándar de la aplicación.
 */
public final class Dialogos {

    private Dialogos() {
    }

    /**
     * Muestra un mensaje informativo.
     */
    public static void info(String titulo, String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, titulo, mensaje);
    }

    /**
     * Muestra un mensaje de error.
     */
    public static void error(String titulo, String mensaje) {
        mostrar(Alert.AlertType.ERROR, titulo, mensaje);
    }

    /**
     * Pide confirmación al usuario.
     *
     * @return {@code true} si pulsa Aceptar
     */
    public static boolean confirmar(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.OK, ButtonType.CANCEL);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        return alerta.showAndWait().filter(boton -> boton == ButtonType.OK).isPresent();
    }

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.showAndWait();
    }
}
