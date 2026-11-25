package es.tracker;

import es.tracker.config.DatabaseConfig;
import es.tracker.exception.CandidaturaException;
import es.tracker.ui.Dialogos;
import es.tracker.ui.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Aplicación JavaFX de CandiTrack.
 */
public class App extends Application {

    private static final String TITULO = "CandiTrack · Tracker de candidaturas";

    @Override
    public void start(Stage ventana) throws IOException {
        AppContext contexto;
        try {
            contexto = AppContext.crear(DatabaseConfig.cargar());
        } catch (CandidaturaException e) {
            Dialogos.error("No se puede iniciar CandiTrack", e.getMessage());
            return;
        }
        FXMLLoader cargador = new FXMLLoader(App.class.getResource("/fxml/main.fxml"));
        cargador.setControllerFactory(tipo -> new MainController(contexto));
        Parent raiz = cargador.load();
        ventana.setTitle(TITULO);
        ventana.setScene(new Scene(raiz, 1280, 800));
        ventana.setMinWidth(1100);
        ventana.setMinHeight(700);
        ventana.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
