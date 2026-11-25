package es.tracker.ui;

import es.tracker.AppContext;
import es.tracker.exception.CandidaturaException;
import es.tracker.model.Estado;
import es.tracker.model.FiltroOfertas;
import es.tracker.model.Oferta;
import es.tracker.model.OfertaVista;
import es.tracker.service.ImportacionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Listado de ofertas con filtros combinables, cambio de estado, borrado e importación de CSV.
 */
public class OfertasListController {

    private static final Locale ES = Locale.of("es", "ES");
    private static final String CLASE_ERROR = "campo-error";

    private final AppContext contexto;
    private final Navigator navegador;
    private FiltroOfertas filtroActual = FiltroOfertas.vacio();

    @FXML
    private Label lblResumen;
    @FXML
    private Label lblError;
    @FXML
    private ComboBox<Estado> cmbEstado;
    @FXML
    private TextField txtEmpresa;
    @FXML
    private TextField txtTitulo;
    @FXML
    private TextField txtSalarioMin;
    @FXML
    private TextField txtSalarioMax;
    @FXML
    private DatePicker dpDesde;
    @FXML
    private DatePicker dpHasta;
    @FXML
    private TableView<OfertaVista> tabla;
    @FXML
    private TableColumn<OfertaVista, String> colId;
    @FXML
    private TableColumn<OfertaVista, String> colTitulo;
    @FXML
    private TableColumn<OfertaVista, String> colEmpresa;
    @FXML
    private TableColumn<OfertaVista, String> colEstado;
    @FXML
    private TableColumn<OfertaVista, String> colFecha;
    @FXML
    private TableColumn<OfertaVista, String> colSalario;
    @FXML
    private MenuButton mnuEstado;
    @FXML
    private Button btnEliminar;

    public OfertasListController(AppContext contexto, Navigator navegador) {
        this.contexto = contexto;
        this.navegador = navegador;
    }

    @FXML
    private void initialize() {
        configurarFiltros();
        configurarTabla();
        configurarMenuEstado();
        recargar();
    }

    @FXML
    private void aplicarFiltros() {
        limpiarErrores();
        try {
            filtroActual = leerFiltro();
            recargar();
        } catch (CandidaturaException e) {
            lblError.setText(e.getMessage());
        }
    }

    @FXML
    private void limpiarFiltros() {
        cmbEstado.getSelectionModel().selectFirst();
        txtEmpresa.clear();
        txtTitulo.clear();
        txtSalarioMin.clear();
        txtSalarioMax.clear();
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        limpiarErrores();
        filtroActual = FiltroOfertas.vacio();
        recargar();
    }

    @FXML
    private void importarCsv() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Importar candidaturas desde CSV");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
        File archivo = selector.showOpenDialog(tabla.getScene().getWindow());
        if (archivo == null) {
            return;
        }
        try {
            ImportacionService.Resultado resultado = contexto.importacion().importar(archivo.toPath());
            StringBuilder mensaje = new StringBuilder()
                    .append(resultado.ofertasImportadas()).append(" candidatura(s) importada(s)\n")
                    .append(resultado.empresasCreadas()).append(" empresa(s) nueva(s)");
            if (!resultado.errores().isEmpty()) {
                mensaje.append("\n\nLíneas descartadas:\n").append(String.join("\n", resultado.errores()));
            }
            Dialogos.info("Importación terminada", mensaje.toString());
            recargar();
        } catch (CandidaturaException e) {
            Dialogos.error("No se pudo importar", e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        OfertaVista seleccion = tabla.getSelectionModel().getSelectedItem();
        if (seleccion == null) {
            return;
        }
        Oferta oferta = seleccion.oferta();
        if (!Dialogos.confirmar("Eliminar candidatura",
                "Se eliminará «" + oferta.titulo() + "» (" + seleccion.empresaNombre() + ") y sus entrevistas.")) {
            return;
        }
        try {
            contexto.ofertas().eliminar(oferta.id());
            recargar();
        } catch (CandidaturaException e) {
            Dialogos.error("No se pudo eliminar", e.getMessage());
        }
    }

    private void cambiarEstado(Estado estado) {
        OfertaVista seleccion = tabla.getSelectionModel().getSelectedItem();
        if (seleccion == null) {
            return;
        }
        int id = seleccion.oferta().id();
        try {
            contexto.ofertas().cambiarEstado(id, estado);
            recargar();
            tabla.getItems().stream().filter(v -> v.oferta().id() == id).findFirst()
                    .ifPresent(v -> tabla.getSelectionModel().select(v));
        } catch (CandidaturaException e) {
            Dialogos.error("No se pudo cambiar el estado", e.getMessage());
        }
    }

    private void recargar() {
        try {
            tabla.setItems(FXCollections.observableArrayList(contexto.ofertas().buscar(filtroActual)));
            int total = tabla.getItems().size();
            lblResumen.setText(total == 1 ? "1 candidatura" : total + " candidaturas");
        } catch (CandidaturaException e) {
            Dialogos.error("No se pudieron cargar las candidaturas", e.getMessage());
        }
    }

    private FiltroOfertas leerFiltro() {
        BigDecimal salarioMin = leerSalario(txtSalarioMin, "mínimo");
        BigDecimal salarioMax = leerSalario(txtSalarioMax, "máximo");
        return new FiltroOfertas(cmbEstado.getValue(), txtEmpresa.getText(), txtTitulo.getText(), salarioMin,
                salarioMax, dpDesde.getValue(), dpHasta.getValue());
    }

    private BigDecimal leerSalario(TextField campo, String nombre) {
        String texto = campo.getText() == null ? "" : campo.getText().trim();
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(texto.replace(',', '.'));
        } catch (NumberFormatException e) {
            campo.getStyleClass().add(CLASE_ERROR);
            throw new CandidaturaException("El salario " + nombre + " no es un número válido", e);
        }
    }

    private void limpiarErrores() {
        lblError.setText("");
        txtSalarioMin.getStyleClass().remove(CLASE_ERROR);
        txtSalarioMax.getStyleClass().remove(CLASE_ERROR);
    }

    private void configurarFiltros() {
        cmbEstado.getItems().add(null);
        cmbEstado.getItems().addAll(Estado.values());
        cmbEstado.setConverter(new StringConverter<>() {
            @Override
            public String toString(Estado estado) {
                return estado == null ? "Todos los estados" : estado.getTexto();
            }

            @Override
            public Estado fromString(String texto) {
                return null;
            }
        });
        cmbEstado.getSelectionModel().selectFirst();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(c -> new SimpleStringProperty("#" + c.getValue().oferta().id()));
        colTitulo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().oferta().titulo()));
        colEmpresa.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().empresaNombre()));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().oferta().estado().getTexto()));
        colEstado.setCellFactory(columna -> new CeldaEstado());
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().oferta().fechaPostulacion().format(es.tracker.model.Candidatura.FORMATO_FECHA)));
        colSalario.setCellValueFactory(c -> new SimpleStringProperty(formatearSalario(c.getValue().oferta().salario())));
        colSalario.getStyleClass().add("columna-derecha");
        tabla.setPlaceholder(new Label("No hay candidaturas que mostrar."));

        mnuEstado.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        btnEliminar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
    }

    private void configurarMenuEstado() {
        for (Estado estado : Estado.values()) {
            MenuItem item = new MenuItem(estado.getTexto());
            item.setOnAction(evento -> cambiarEstado(estado));
            mnuEstado.getItems().add(item);
        }
    }

    private static String formatearSalario(BigDecimal salario) {
        return salario == null ? "—" : String.format(ES, "%,.0f €", salario);
    }

    /** Celda que muestra el estado como una etiqueta de color. */
    private static final class CeldaEstado extends TableCell<OfertaVista, String> {
        @Override
        protected void updateItem(String estado, boolean vacia) {
            super.updateItem(estado, vacia);
            if (vacia || estado == null) {
                setGraphic(null);
                return;
            }
            Label etiqueta = new Label(estado);
            etiqueta.getStyleClass().addAll("badge", "badge-" + estado);
            setGraphic(etiqueta);
        }
    }
}
