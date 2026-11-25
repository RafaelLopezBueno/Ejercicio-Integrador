package es.tracker.ui;

import es.tracker.AppContext;
import es.tracker.exception.CandidaturaException;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;

/**
 * Ventana principal: menú lateral y área donde se cargan las pantallas.
 */
public class MainController {

    private final AppContext contexto;
    private Navigator navegador;

    @FXML
    private StackPane contenido;
    @FXML
    private ToggleButton btnOfertas;
    @FXML
    private ToggleButton btnEmpresas;

    public MainController(AppContext contexto) {
        this.contexto = contexto;
    }

    @FXML
    private void initialize() {
        navegador = new Navigator(contexto, contenido);
        irAOfertas();
    }

    @FXML
    private void irAOfertas() {
        btnOfertas.setSelected(true);
        mostrar(Vista.LISTADO);
    }

    @FXML
    private void irAEmpresas() {
        btnEmpresas.setSelected(true);
        mostrar(Vista.EMPRESAS);
    }

    private void mostrar(Vista vista) {
        try {
            navegador.mostrar(vista);
        } catch (CandidaturaException e) {
            Dialogos.error("Error", e.getMessage());
        }
    }
}
