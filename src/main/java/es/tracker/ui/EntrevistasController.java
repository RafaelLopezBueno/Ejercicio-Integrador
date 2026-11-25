package es.tracker.ui;

import es.tracker.AppContext;
import es.tracker.exception.CandidaturaException;
import es.tracker.model.Entrevista;
import es.tracker.model.EntrevistaVista;
import es.tracker.model.FiltroOfertas;
import es.tracker.model.OfertaVista;
import es.tracker.model.ResultadoEntrevista;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.util.StringConverter;

import java.time.LocalDate;

/**
 * Gestión de entrevistas: listado y formulario para crear, editar y eliminar.
 */
public class EntrevistasController {

    private static final String CLASE_ERROR = "campo-error";

    private final AppContext contexto;
    private Integer idEdicion;

    @FXML
    private Label lblResumen;
    @FXML
    private TableView<EntrevistaVista> tabla;
    @FXML
    private TableColumn<EntrevistaVista, String> colFecha;
    @FXML
    private TableColumn<EntrevistaVista, String> colOferta;
    @FXML
    private TableColumn<EntrevistaVista, String> colEmpresa;
    @FXML
    private TableColumn<EntrevistaVista, String> colTipo;
    @FXML
    private TableColumn<EntrevistaVista, String> colResultado;
    @FXML
    private Label lblFormulario;
    @FXML
    private ComboBox<OfertaVista> cmbOferta;
    @FXML
    private DatePicker dpFecha;
    @FXML
    private ComboBox<String> cmbTipo;
    @FXML
    private ComboBox<ResultadoEntrevista> cmbResultado;
    @FXML
    private TextArea txtNotas;
    @FXML
    private Label errOferta;
    @FXML
    private Label errFecha;
    @FXML
    private Label errTipo;
    @FXML
    private Label lblMensaje;
    @FXML
    private Button btnEliminar;

    public EntrevistasController(AppContext contexto, Navigator navegador) {
        this.contexto = contexto;
    }

    @FXML
    private void initialize() {
        configurarTabla();
        configurarFormulario();
        recargar();
        limpiarFormulario();
    }

    @FXML
    private void nueva() {
        tabla.getSelectionModel().clearSelection();
        limpiarFormulario();
    }

    @FXML
    private void guardar() {
        lblMensaje.setText("");
        if (!validar()) {
            return;
        }
        try {
            Entrevista guardada = contexto.entrevistas().guardar(new Entrevista(idEdicion, cmbOferta.getValue().oferta().id(),
                    dpFecha.getValue(), cmbTipo.getValue(), txtNotas.getText(), cmbResultado.getValue()));
            recargar();
            tabla.getItems().stream().filter(v -> v.entrevista().id().equals(guardada.id())).findFirst()
                    .ifPresent(v -> tabla.getSelectionModel().select(v));
            lblMensaje.setText("Entrevista guardada");
        } catch (CandidaturaException e) {
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        EntrevistaVista seleccion = tabla.getSelectionModel().getSelectedItem();
        if (seleccion == null) {
            return;
        }
        if (!Dialogos.confirmar("Eliminar entrevista", "Se eliminará la entrevista del "
                + Formatos.fecha(seleccion.entrevista().fecha()) + " de «" + seleccion.tituloOferta() + "».")) {
            return;
        }
        try {
            contexto.entrevistas().eliminar(seleccion.entrevista().id());
            recargar();
            limpiarFormulario();
        } catch (CandidaturaException e) {
            Dialogos.error("No se pudo eliminar", e.getMessage());
        }
    }

    private void configurarTabla() {
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(Formatos.fecha(c.getValue().entrevista().fecha())));
        colOferta.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().tituloOferta()));
        colEmpresa.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().empresaNombre()));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().entrevista().tipo()));
        colResultado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().entrevista().resultado().getTexto()));
        colResultado.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(String resultado, boolean vacia) {
                super.updateItem(resultado, vacia);
                if (vacia || resultado == null) {
                    setGraphic(null);
                    return;
                }
                Label etiqueta = new Label(resultado);
                etiqueta.getStyleClass().addAll("badge", "badge-" + resultado);
                setGraphic(etiqueta);
            }
        });
        tabla.setPlaceholder(new Label("Todavía no hay entrevistas registradas."));
        tabla.getSelectionModel().selectedItemProperty().addListener((observable, anterior, nueva) -> {
            if (nueva != null) {
                cargarFormulario(nueva);
            }
        });
        btnEliminar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
    }

    private void configurarFormulario() {
        cmbOferta.setConverter(new StringConverter<>() {
            @Override
            public String toString(OfertaVista vista) {
                return vista == null ? "" : vista.oferta().titulo() + " · " + vista.empresaNombre();
            }

            @Override
            public OfertaVista fromString(String texto) {
                return null;
            }
        });
        cmbOferta.getItems().setAll(contexto.ofertas().buscar(FiltroOfertas.vacio()));
        cmbTipo.getItems().setAll("telefónica", "técnica", "RR. HH.", "final", "otra");
        cmbResultado.getItems().setAll(ResultadoEntrevista.values());
        dpFecha.setConverter(Formatos.convertidorFecha());
    }

    private void cargarFormulario(EntrevistaVista vista) {
        Entrevista entrevista = vista.entrevista();
        idEdicion = entrevista.id();
        lblFormulario.setText("Editar entrevista");
        lblMensaje.setText("");
        limpiarErrores();
        cmbOferta.getItems().stream().filter(o -> o.oferta().id() == entrevista.idOferta()).findFirst()
                .ifPresent(o -> cmbOferta.getSelectionModel().select(o));
        dpFecha.setValue(entrevista.fecha());
        cmbTipo.setValue(entrevista.tipo());
        cmbResultado.setValue(entrevista.resultado());
        txtNotas.setText(entrevista.notas());
    }

    private void limpiarFormulario() {
        idEdicion = null;
        lblFormulario.setText("Programar entrevista");
        lblMensaje.setText("");
        limpiarErrores();
        cmbOferta.getSelectionModel().clearSelection();
        dpFecha.setValue(LocalDate.now());
        cmbTipo.getSelectionModel().clearSelection();
        cmbResultado.getSelectionModel().select(ResultadoEntrevista.PENDIENTE);
        txtNotas.clear();
    }

    private void recargar() {
        try {
            tabla.setItems(FXCollections.observableArrayList(contexto.entrevistas().listar()));
            int total = tabla.getItems().size();
            lblResumen.setText(total == 1 ? "1 entrevista registrada" : total + " entrevistas registradas");
        } catch (CandidaturaException e) {
            Dialogos.error("No se pudieron cargar las entrevistas", e.getMessage());
        }
    }

    private boolean validar() {
        limpiarErrores();
        boolean correcto = true;
        if (cmbOferta.getValue() == null) {
            correcto = marcar(cmbOferta, errOferta, "Selecciona una candidatura");
        }
        if (dpFecha.getValue() == null) {
            correcto = marcar(dpFecha, errFecha, "Indica una fecha válida (dd/MM/yyyy)");
        }
        if (cmbTipo.getValue() == null || cmbTipo.getValue().isBlank()) {
            correcto = marcar(cmbTipo, errTipo, "Selecciona el tipo de entrevista");
        }
        if (cmbResultado.getValue() == null) {
            cmbResultado.setValue(ResultadoEntrevista.PENDIENTE);
        }
        return correcto;
    }

    private boolean marcar(Node campo, Label etiqueta, String mensaje) {
        campo.getStyleClass().add(CLASE_ERROR);
        etiqueta.setText(mensaje);
        return false;
    }

    private void limpiarErrores() {
        for (Node campo : new Node[]{cmbOferta, dpFecha, cmbTipo}) {
            campo.getStyleClass().remove(CLASE_ERROR);
        }
        for (Label etiqueta : new Label[]{errOferta, errFecha, errTipo}) {
            etiqueta.setText("");
        }
    }
}
