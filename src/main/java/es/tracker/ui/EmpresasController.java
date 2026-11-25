package es.tracker.ui;

import es.tracker.AppContext;
import es.tracker.exception.CandidaturaException;
import es.tracker.model.EmpresaResumen;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

/**
 * Listado de empresas con alta, edición y borrado.
 */
public class EmpresasController {

    private final AppContext contexto;
    private final Navigator navegador;

    @FXML
    private Label lblResumen;
    @FXML
    private TableView<EmpresaResumen> tabla;
    @FXML
    private TableColumn<EmpresaResumen, String> colNombre;
    @FXML
    private TableColumn<EmpresaResumen, String> colSector;
    @FXML
    private TableColumn<EmpresaResumen, String> colWeb;
    @FXML
    private TableColumn<EmpresaResumen, String> colRegistro;
    @FXML
    private TableColumn<EmpresaResumen, String> colOfertas;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnEliminar;

    public EmpresasController(AppContext contexto, Navigator navegador) {
        this.contexto = contexto;
        this.navegador = navegador;
    }

    @FXML
    private void initialize() {
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().empresa().nombre()));
        colSector.setCellValueFactory(c -> new SimpleStringProperty(valor(c.getValue().empresa().sector())));
        colWeb.setCellValueFactory(c -> new SimpleStringProperty(valor(c.getValue().empresa().web())));
        colWeb.getStyleClass().add("valor-mono");
        colRegistro.setCellValueFactory(c -> new SimpleStringProperty(Formatos.fecha(c.getValue().empresa().fechaRegistro())));
        colOfertas.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().totalOfertas())));
        colOfertas.getStyleClass().add("columna-derecha");
        tabla.setPlaceholder(new Label("Todavía no hay empresas registradas."));
        tabla.setRowFactory(t -> {
            TableRow<EmpresaResumen> fila = new TableRow<>();
            fila.setOnMouseClicked(evento -> {
                if (evento.getClickCount() == 2 && !fila.isEmpty()) {
                    editar();
                }
            });
            return fila;
        });
        var sinSeleccion = tabla.getSelectionModel().selectedItemProperty().isNull();
        btnEditar.disableProperty().bind(sinSeleccion);
        btnEliminar.disableProperty().bind(sinSeleccion);
        recargar();
    }

    @FXML
    private void nueva() {
        navegador.mostrar(Vista.FORMULARIO_EMPRESA, (EmpresaFormController controlador) -> controlador.iniciar(null));
    }

    @FXML
    private void editar() {
        EmpresaResumen seleccion = tabla.getSelectionModel().getSelectedItem();
        if (seleccion != null) {
            int id = seleccion.empresa().id();
            navegador.mostrar(Vista.FORMULARIO_EMPRESA, (EmpresaFormController controlador) -> controlador.iniciar(id));
        }
    }

    @FXML
    private void eliminar() {
        EmpresaResumen seleccion = tabla.getSelectionModel().getSelectedItem();
        if (seleccion == null) {
            return;
        }
        String nombre = seleccion.empresa().nombre();
        int ofertas = seleccion.totalOfertas();
        String mensaje = ofertas == 0
                ? "Se eliminará la empresa «" + nombre + "»."
                : "La empresa «" + nombre + "» tiene " + ofertas + " candidatura(s). Se eliminarán también esas "
                + "candidaturas y sus entrevistas. Esta acción no se puede deshacer.";
        if (!Dialogos.confirmar("Eliminar empresa", mensaje)) {
            return;
        }
        try {
            contexto.empresas().eliminar(seleccion.empresa().id(), ofertas > 0);
            recargar();
        } catch (CandidaturaException e) {
            Dialogos.error("No se pudo eliminar", e.getMessage());
        }
    }

    private void recargar() {
        try {
            tabla.setItems(FXCollections.observableArrayList(contexto.empresas().listarConTotales()));
            int total = tabla.getItems().size();
            lblResumen.setText(total == 1 ? "1 empresa registrada" : total + " empresas registradas");
        } catch (CandidaturaException e) {
            Dialogos.error("No se pudieron cargar las empresas", e.getMessage());
        }
    }

    private static String valor(String texto) {
        return texto == null || texto.isBlank() ? "—" : texto;
    }
}
