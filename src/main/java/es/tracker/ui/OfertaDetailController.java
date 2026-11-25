package es.tracker.ui;

import es.tracker.AppContext;
import es.tracker.exception.CandidaturaException;
import es.tracker.model.Empresa;
import es.tracker.model.Entrevista;
import es.tracker.model.Estado;
import es.tracker.model.Oferta;
import es.tracker.model.OfertaVista;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Detalle de una oferta con los datos de su empresa y la lista de entrevistas.
 */
public class OfertaDetailController {

    private final AppContext contexto;
    private final Navigator navegador;
    private int idOferta;

    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblSubtitulo;
    @FXML
    private Label lblEstado;
    @FXML
    private Label lblFecha;
    @FXML
    private Label lblSalario;
    @FXML
    private Label lblEnlace;
    @FXML
    private Label lblDescripcion;
    @FXML
    private Label lblNotas;
    @FXML
    private Label lblEmpresa;
    @FXML
    private Label lblSector;
    @FXML
    private Label lblWeb;
    @FXML
    private MenuButton mnuEstado;
    @FXML
    private TableView<Entrevista> tablaEntrevistas;
    @FXML
    private TableColumn<Entrevista, String> colFecha;
    @FXML
    private TableColumn<Entrevista, String> colTipo;
    @FXML
    private TableColumn<Entrevista, String> colResultado;

    public OfertaDetailController(AppContext contexto, Navigator navegador) {
        this.contexto = contexto;
        this.navegador = navegador;
    }

    @FXML
    private void initialize() {
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(Formatos.fecha(c.getValue().fecha())));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().tipo()));
        colResultado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().resultado().getTexto()));
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
        tablaEntrevistas.setPlaceholder(new Label("Sin entrevistas registradas."));
        for (Estado estado : Estado.values()) {
            MenuItem item = new MenuItem(estado.getTexto());
            item.setOnAction(evento -> cambiarEstado(estado));
            mnuEstado.getItems().add(item);
        }
    }

    /**
     * Carga y muestra los datos de una oferta.
     *
     * @param id identificador de la oferta
     * @throws CandidaturaException si la oferta no existe
     */
    public void iniciar(int id) {
        this.idOferta = id;
        OfertaVista vista = contexto.ofertas().detalle(id);
        Oferta oferta = vista.oferta();
        Empresa empresa = contexto.empresas().obtener(oferta.idEmpresa());

        lblTitulo.setText(oferta.titulo());
        lblSubtitulo.setText("Candidatura #" + oferta.id() + " · " + vista.empresaNombre());
        lblEstado.setText(oferta.estado().getTexto());
        lblEstado.getStyleClass().removeIf(clase -> clase.startsWith("badge-"));
        lblEstado.getStyleClass().add("badge-" + oferta.estado().getTexto());
        lblFecha.setText(Formatos.fecha(oferta.fechaPostulacion()));
        lblSalario.setText(Formatos.salario(oferta.salario()));
        lblEnlace.setText(textoOGuion(oferta.enlace()));
        lblDescripcion.setText(textoOGuion(oferta.descripcion()));
        lblNotas.setText(textoOGuion(oferta.notas()));
        lblEmpresa.setText(empresa.nombre());
        lblSector.setText(textoOGuion(empresa.sector()));
        lblWeb.setText(textoOGuion(empresa.web()));
        tablaEntrevistas.setItems(FXCollections.observableArrayList(contexto.entrevistas().deOferta(id)));
    }

    @FXML
    private void volver() {
        navegador.mostrar(Vista.LISTADO);
    }

    @FXML
    private void editar() {
        navegador.mostrar(Vista.FORMULARIO_OFERTA, (OfertaFormController controlador) -> controlador.iniciar(idOferta));
    }

    @FXML
    private void eliminar() {
        if (!Dialogos.confirmar("Eliminar candidatura",
                "Se eliminará «" + lblTitulo.getText() + "» y sus entrevistas. Esta acción no se puede deshacer.")) {
            return;
        }
        try {
            contexto.ofertas().eliminar(idOferta);
            navegador.mostrar(Vista.LISTADO);
        } catch (CandidaturaException e) {
            Dialogos.error("No se pudo eliminar", e.getMessage());
        }
    }

    private void cambiarEstado(Estado estado) {
        try {
            contexto.ofertas().cambiarEstado(idOferta, estado);
            iniciar(idOferta);
        } catch (CandidaturaException e) {
            Dialogos.error("No se pudo cambiar el estado", e.getMessage());
        }
    }

    private static String textoOGuion(String texto) {
        return texto == null || texto.isBlank() ? "—" : texto;
    }
}
