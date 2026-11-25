package es.tracker;

import es.tracker.config.DatabaseConfig;
import es.tracker.ui.EmpresaFormController;
import es.tracker.ui.MainController;
import es.tracker.ui.Navigator;
import es.tracker.ui.OfertaDetailController;
import es.tracker.ui.OfertaFormController;
import es.tracker.ui.Vista;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Comprueba que los archivos FXML se cargan con JavaFX y que sus controladores se enlazan correctamente.
 */
class FxmlCargaTest {

    @BeforeAll
    static void arrancarJavaFx() throws InterruptedException {
        CountDownLatch listo = new CountDownLatch(1);
        try {
            Platform.startup(listo::countDown);
        } catch (IllegalStateException yaIniciado) {
            listo.countDown();
        }
        listo.await(10, TimeUnit.SECONDS);
    }

    @Test
    void laVentanaPrincipalCargaElListadoConLosDatosImportados() throws Exception {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        AppContext contexto = AppContext.crear(new DatabaseConfig(url, "sa", "", true));
        contexto.importacion().importar(Path.of("data", "candidaturas_ejemplo.csv"));

        Parent raiz = enHiloFx(() -> {
            try {
                FXMLLoader cargador = new FXMLLoader(App.class.getResource("/fxml/main.fxml"));
                cargador.setControllerFactory(tipo -> new MainController(contexto));
                return cargador.load();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });

        StackPane contenido = (StackPane) raiz.lookup("#contenido");
        assertNotNull(contenido);
        assertEquals(1, contenido.getChildren().size());
        TableView<?> tabla = (TableView<?>) contenido.lookup("#tabla");
        assertNotNull(tabla);
        assertEquals(15, tabla.getItems().size());
    }

    @Test
    void navegaAlFormularioYAlDetalleDeUnaOferta() throws Exception {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        AppContext contexto = AppContext.crear(new DatabaseConfig(url, "sa", "", true));
        contexto.importacion().importar(Path.of("data", "candidaturas_ejemplo.csv"));

        StackPane anfitrion = enHiloFx(StackPane::new);
        Navigator navegador = new Navigator(contexto, anfitrion);

        String tituloDetalle = enHiloFx(() -> {
            navegador.mostrar(Vista.DETALLE, (OfertaDetailController c) -> c.iniciar(1));
            return ((Label) contenidoDe(anfitrion).lookup("#lblTitulo")).getText();
        });
        assertEquals("Desarrollador Java Junior", tituloDetalle);

        String tituloEdicion = enHiloFx(() -> {
            navegador.mostrar(Vista.FORMULARIO_OFERTA, (OfertaFormController c) -> c.iniciar(1));
            return ((TextField) contenidoDe(anfitrion).lookup("#txtTitulo")).getText();
        });
        assertEquals("Desarrollador Java Junior", tituloEdicion);

        String tituloAlta = enHiloFx(() -> {
            navegador.mostrar(Vista.FORMULARIO_OFERTA, (OfertaFormController c) -> c.iniciar(null));
            return ((Label) contenidoDe(anfitrion).lookup("#lblTitulo")).getText();
        });
        assertEquals("Nueva candidatura", tituloAlta);
    }

    @Test
    void cargaLasPantallasDeEmpresas() throws Exception {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        AppContext contexto = AppContext.crear(new DatabaseConfig(url, "sa", "", true));
        contexto.importacion().importar(Path.of("data", "candidaturas_ejemplo.csv"));

        StackPane anfitrion = enHiloFx(StackPane::new);
        Navigator navegador = new Navigator(contexto, anfitrion);

        int filas = enHiloFx(() -> {
            navegador.mostrar(Vista.EMPRESAS);
            return ((TableView<?>) anfitrion.lookup("#tabla")).getItems().size();
        });
        assertEquals(4, filas);

        String nombre = enHiloFx(() -> {
            navegador.mostrar(Vista.FORMULARIO_EMPRESA, (EmpresaFormController c) -> c.iniciar(1));
            return ((TextField) contenidoDe(anfitrion).lookup("#txtNombre")).getText();
        });
        assertEquals("Indra", nombre);
    }

    private static Node contenidoDe(StackPane anfitrion) {
        return ((ScrollPane) anfitrion.getChildren().getFirst()).getContent();
    }

    private static <T> T enHiloFx(Supplier<T> tarea) throws Exception {
        CompletableFuture<T> resultado = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                resultado.complete(tarea.get());
            } catch (Throwable e) {
                resultado.completeExceptionally(e);
            }
        });
        return resultado.get(20, TimeUnit.SECONDS);
    }
}
