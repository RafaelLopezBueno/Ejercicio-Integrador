package es.tracker.ui;

import es.tracker.AppContext;
import es.tracker.exception.CandidaturaException;
import es.tracker.model.Empresa;
import es.tracker.model.Estado;
import es.tracker.model.Oferta;
import es.tracker.model.OfertaVista;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Formulario de alta y edición de ofertas, con validación de los campos.
 */
public class OfertaFormController {

    private static final String CLASE_ERROR = "campo-error";

    private final AppContext contexto;
    private final Navigator navegador;
    private Integer idOferta;

    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblSubtitulo;
    @FXML
    private TextField txtTitulo;
    @FXML
    private ComboBox<Empresa> cmbEmpresa;
    @FXML
    private ComboBox<Estado> cmbEstado;
    @FXML
    private DatePicker dpFecha;
    @FXML
    private TextField txtSalario;
    @FXML
    private TextField txtEnlace;
    @FXML
    private TextArea txtDescripcion;
    @FXML
    private TextArea txtNotas;
    @FXML
    private Label errTitulo;
    @FXML
    private Label errEmpresa;
    @FXML
    private Label errEstado;
    @FXML
    private Label errFecha;
    @FXML
    private Label errSalario;
    @FXML
    private Label errEnlace;
    @FXML
    private Label lblError;

    public OfertaFormController(AppContext contexto, Navigator navegador) {
        this.contexto = contexto;
        this.navegador = navegador;
    }

    @FXML
    private void initialize() {
        cmbEmpresa.getItems().setAll(contexto.empresas().listar());
        cmbEstado.getItems().setAll(Estado.values());
        cmbEstado.getSelectionModel().select(Estado.PENDIENTE);
        dpFecha.setConverter(Formatos.convertidorFecha());
        dpFecha.setValue(LocalDate.now());
        if (cmbEmpresa.getItems().isEmpty()) {
            lblError.setText("Todavía no hay empresas registradas. Registra una antes de crear candidaturas.");
        }
    }

    /**
     * Prepara el formulario para crear una oferta o para editar una existente.
     *
     * @param id identificador de la oferta a editar, o {@code null} para crear una nueva
     */
    public void iniciar(Integer id) {
        this.idOferta = id;
        if (id == null) {
            lblTitulo.setText("Nueva candidatura");
            lblSubtitulo.setText("Rellena los datos de la oferta (campos obligatorios *)");
            return;
        }
        OfertaVista vista = contexto.ofertas().detalle(id);
        Oferta oferta = vista.oferta();
        lblTitulo.setText("Editar candidatura");
        lblSubtitulo.setText("Modifica los datos y guarda los cambios (campos obligatorios *)");
        txtTitulo.setText(oferta.titulo());
        cmbEmpresa.getItems().stream().filter(e -> e.id() == oferta.idEmpresa()).findFirst()
                .ifPresent(e -> cmbEmpresa.getSelectionModel().select(e));
        cmbEstado.getSelectionModel().select(oferta.estado());
        dpFecha.setValue(oferta.fechaPostulacion());
        txtSalario.setText(oferta.salario() == null ? "" : oferta.salario().stripTrailingZeros().toPlainString());
        txtEnlace.setText(oferta.enlace());
        txtDescripcion.setText(oferta.descripcion());
        txtNotas.setText(oferta.notas());
    }

    @FXML
    private void guardar() {
        if (!validar()) {
            return;
        }
        try {
            Oferta guardada = contexto.ofertas().guardar(new Oferta(idOferta, cmbEmpresa.getValue().id(),
                    txtTitulo.getText().trim(), txtDescripcion.getText(), cmbEstado.getValue(), dpFecha.getValue(),
                    txtEnlace.getText(), salario(), txtNotas.getText()));
            verDetalle(guardada.id());
        } catch (CandidaturaException e) {
            lblError.setText(e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        if (idOferta == null) {
            navegador.mostrar(Vista.LISTADO);
        } else {
            verDetalle(idOferta);
        }
    }

    private void verDetalle(int id) {
        navegador.mostrar(Vista.DETALLE, (OfertaDetailController controlador) -> controlador.iniciar(id));
    }

    private boolean validar() {
        limpiarErrores();
        boolean correcto = true;
        if (txtTitulo.getText() == null || txtTitulo.getText().isBlank()) {
            correcto = marcar(txtTitulo, errTitulo, "El título es obligatorio");
        } else if (txtTitulo.getText().trim().length() > 160) {
            correcto = marcar(txtTitulo, errTitulo, "El título no puede superar los 160 caracteres");
        }
        if (cmbEmpresa.getValue() == null) {
            correcto = marcar(cmbEmpresa, errEmpresa, "Selecciona una empresa");
        }
        if (cmbEstado.getValue() == null) {
            correcto = marcar(cmbEstado, errEstado, "Selecciona un estado");
        }
        if (dpFecha.getValue() == null) {
            correcto = marcar(dpFecha, errFecha, "Indica una fecha válida (dd/MM/yyyy)");
        }
        try {
            BigDecimal salario = salario();
            if (salario != null && salario.signum() < 0) {
                correcto = marcar(txtSalario, errSalario, "El salario no puede ser negativo");
            }
        } catch (NumberFormatException e) {
            correcto = marcar(txtSalario, errSalario, "El salario debe ser un número");
        }
        String enlace = txtEnlace.getText();
        if (enlace != null && !enlace.isBlank() && !enlace.trim().matches("(?i)^https?://\\S+$")) {
            correcto = marcar(txtEnlace, errEnlace, "El enlace debe empezar por http:// o https://");
        }
        return correcto;
    }

    private BigDecimal salario() {
        return Formatos.decimal(txtSalario.getText());
    }

    private boolean marcar(javafx.scene.Node campo, Label etiqueta, String mensaje) {
        campo.getStyleClass().add(CLASE_ERROR);
        etiqueta.setText(mensaje);
        return false;
    }

    private void limpiarErrores() {
        lblError.setText("");
        for (javafx.scene.Node campo : new javafx.scene.Node[]{txtTitulo, cmbEmpresa, cmbEstado, dpFecha, txtSalario, txtEnlace}) {
            campo.getStyleClass().remove(CLASE_ERROR);
        }
        for (Label etiqueta : new Label[]{errTitulo, errEmpresa, errEstado, errFecha, errSalario, errEnlace}) {
            etiqueta.setText("");
        }
    }
}
