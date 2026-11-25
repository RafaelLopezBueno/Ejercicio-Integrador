package es.tracker.ui;

import es.tracker.AppContext;
import es.tracker.exception.CandidaturaException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

/**
 * Carga las pantallas FXML en el área de contenido de la ventana principal.
 * Los controladores se crean con un constructor {@code (AppContext, Navigator)}.
 */
public class Navigator {

    private final AppContext contexto;
    private final Pane contenido;

    public Navigator(AppContext contexto, Pane contenido) {
        this.contexto = contexto;
        this.contenido = contenido;
    }

    /**
     * Muestra una pantalla.
     *
     * @param vista pantalla a mostrar
     * @param <C>   tipo del controlador de la pantalla
     * @return controlador de la pantalla cargada
     */
    public <C> C mostrar(Vista vista) {
        return mostrar(vista, controlador -> { });
    }

    /**
     * Muestra una pantalla y permite pasar datos a su controlador antes de que se vea.
     *
     * @param vista   pantalla a mostrar
     * @param iniciar acción que recibe el controlador ya creado
     * @param <C>     tipo del controlador de la pantalla
     * @return controlador de la pantalla cargada
     * @throws CandidaturaException si el archivo FXML no se puede cargar
     */
    public <C> C mostrar(Vista vista, Consumer<C> iniciar) {
        FXMLLoader cargador = new FXMLLoader(Navigator.class.getResource(vista.recurso()));
        cargador.setControllerFactory(this::crearControlador);
        Parent raiz;
        try {
            raiz = cargador.load();
        } catch (IOException e) {
            throw new CandidaturaException("No se pudo cargar la pantalla " + vista.recurso() + ": " + e.getMessage(), e);
        }
        C controlador = cargador.getController();
        iniciar.accept(controlador);
        contenido.getChildren().setAll(raiz);
        return controlador;
    }

    private Object crearControlador(Class<?> tipo) {
        try {
            return tipo.getConstructor(AppContext.class, Navigator.class).newInstance(contexto, this);
        } catch (InvocationTargetException e) {
            throw new CandidaturaException("No se pudo crear " + tipo.getSimpleName() + ": " + e.getCause().getMessage(), e);
        } catch (ReflectiveOperationException e) {
            throw new CandidaturaException("El controlador " + tipo.getSimpleName()
                    + " debe tener un constructor (AppContext, Navigator)", e);
        }
    }
}
