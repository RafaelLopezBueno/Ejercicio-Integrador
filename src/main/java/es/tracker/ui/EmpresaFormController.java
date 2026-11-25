package es.tracker.ui;

import es.tracker.AppContext;
import es.tracker.exception.CandidaturaException;
import es.tracker.model.Empresa;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.time.LocalDate;

/**
 * Formulario de alta y edición de empresas, con validación de los campos.
 */
public class EmpresaFormController {

    private static final String CLASE_ERROR = "campo-error";

    private final AppContext contexto;
    private final Navigator navegador;
    private Integer idEmpresa;

    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblSubtitulo;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtSector;
    @FXML
    private TextField txtWeb;
    @FXML
    private DatePicker dpRegistro;
    @FXML
    private Label errNombre;
    @FXML
    private Label errWeb;
    @FXML
    private Label errRegistro;
    @FXML
    private Label lblError;

    public EmpresaFormController(AppContext contexto, Navigator navegador) {
        this.contexto = contexto;
        this.navegador = navegador;
    }

    @FXML
    private void initialize() {
        dpRegistro.setConverter(Formatos.convertidorFecha());
        dpRegistro.setValue(LocalDate.now());
    }

    /**
     * Prepara el formulario para crear una empresa o para editar una existente.
     *
     * @param id identificador de la empresa a editar, o {@code null} para crear una nueva
     */
    public void iniciar(Integer id) {
        this.idEmpresa = id;
        if (id == null) {
            lblTitulo.setText("Nueva empresa");
            lblSubtitulo.setText("Registra una empresa para asociarle candidaturas (campos obligatorios *)");
            return;
        }
        Empresa empresa = contexto.empresas().obtener(id);
        lblTitulo.setText("Editar empresa");
        lblSubtitulo.setText("Modifica los datos y guarda los cambios (campos obligatorios *)");
        txtNombre.setText(empresa.nombre());
        txtSector.setText(empresa.sector());
        txtWeb.setText(empresa.web());
        dpRegistro.setValue(empresa.fechaRegistro());
    }

    @FXML
    private void guardar() {
        if (!validar()) {
            return;
        }
        try {
            contexto.empresas().guardar(new Empresa(idEmpresa, txtNombre.getText().trim(), txtSector.getText(),
                    txtWeb.getText(), dpRegistro.getValue()));
            navegador.mostrar(Vista.EMPRESAS);
        } catch (CandidaturaException e) {
            lblError.setText(e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        navegador.mostrar(Vista.EMPRESAS);
    }

    private boolean validar() {
        limpiarErrores();
        boolean correcto = true;
        String nombre = txtNombre.getText();
        if (nombre == null || nombre.isBlank()) {
            correcto = marcar(txtNombre, errNombre, "El nombre es obligatorio");
        } else if (nombre.trim().length() > 120) {
            correcto = marcar(txtNombre, errNombre, "El nombre no puede superar los 120 caracteres");
        }
        String web = txtWeb.getText();
        if (web != null && !web.isBlank() && !web.trim().matches("(?i)^https?://\\S+$")) {
            correcto = marcar(txtWeb, errWeb, "La web debe empezar por http:// o https://");
        }
        if (dpRegistro.getValue() == null) {
            correcto = marcar(dpRegistro, errRegistro, "Indica una fecha válida (dd/MM/yyyy)");
        }
        return correcto;
    }

    private boolean marcar(Node campo, Label etiqueta, String mensaje) {
        campo.getStyleClass().add(CLASE_ERROR);
        etiqueta.setText(mensaje);
        return false;
    }

    private void limpiarErrores() {
        lblError.setText("");
        for (Node campo : new Node[]{txtNombre, txtWeb, dpRegistro}) {
            campo.getStyleClass().remove(CLASE_ERROR);
        }
        for (Label etiqueta : new Label[]{errNombre, errWeb, errRegistro}) {
            etiqueta.setText("");
        }
    }
}
