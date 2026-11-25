package es.tracker.ui;

/**
 * Pantallas de la aplicación, cada una definida en un archivo FXML.
 */
public enum Vista {
    LISTADO("ofertas-list");

    private final String archivo;

    Vista(String archivo) {
        this.archivo = archivo;
    }

    /**
     * @return ruta del recurso FXML dentro del classpath
     */
    public String recurso() {
        return "/fxml/" + archivo + ".fxml";
    }
}
