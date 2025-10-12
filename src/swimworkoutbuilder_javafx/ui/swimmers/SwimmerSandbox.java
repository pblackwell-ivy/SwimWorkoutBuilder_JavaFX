package swimworkoutbuilder_javafx.ui.swimmers;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.store.LocalStore;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Smoke test harness for SwimmerPane/SwimmerPresenter that adapts
 * to minor API differences (constructors / bind methods / node getters).
 */
public class SwimmerSandbox extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // ---- Seed AppState from LocalStore (tolerant to signature changes) ----
        AppState app = AppState.get();

        ObservableList<Swimmer> swimmers = FXCollections.observableArrayList();
        try {
            Method list = LocalStore.class.getMethod("listSwimmers");
            @SuppressWarnings("unchecked")
            List<Swimmer> loaded = (List<Swimmer>) list.invoke(null);
            if (loaded != null) swimmers.addAll(loaded);
        } catch (ReflectiveOperationException ignored) {
            // Run with empty list if the method is absent/different
        }

        app.setSwimmers(swimmers);
        if (!swimmers.isEmpty()) app.setCurrentSwimmer(swimmers.get(0));

        // ---- Construct Presenter (prefer (AppState), else no-arg + optional init) ----
        SwimmerPresenter presenter;
        try {
            Constructor<SwimmerPresenter> c1 = SwimmerPresenter.class.getConstructor(AppState.class);
            presenter = c1.newInstance(app);
        } catch (NoSuchMethodException nsme) {
            presenter = SwimmerPresenter.class.getDeclaredConstructor().newInstance();
            try {
                Method init = SwimmerPresenter.class.getMethod("init", AppState.class);
                init.invoke(presenter, app);
            } catch (ReflectiveOperationException ignored) {
                try {
                    Method setApp = SwimmerPresenter.class.getMethod("setAppState", AppState.class);
                    setApp.invoke(presenter, app);
                } catch (ReflectiveOperationException ignored2) { /* presenter may read AppState.get() */ }
            }
        }

        // ---- Construct View (prefer ctor with presenter, else no-arg + bind(...)) ----
        SwimmerPane pane;
        try {
            Constructor<SwimmerPane> vc = SwimmerPane.class.getConstructor(SwimmerPresenter.class);
            pane = vc.newInstance(presenter);
        } catch (NoSuchMethodException nsme) {
            pane = SwimmerPane.class.getDeclaredConstructor().newInstance();
            boolean bound = false;
            try {
                Method bindPresenter = SwimmerPane.class.getMethod("bind", SwimmerPresenter.class);
                bindPresenter.invoke(pane, presenter);
                bound = true;
            } catch (ReflectiveOperationException ignored) {}
            if (!bound) {
                try {
                    Method bindApp = SwimmerPane.class.getMethod("bind", AppState.class);
                    bindApp.invoke(pane, app);
                } catch (ReflectiveOperationException ignored) { /* pane may self-wire */ }
            }
        }

        // ---- Obtain a root Parent for the Scene (node() / getRoot()) ----
        Parent root = null;
        Node node = null;

        try {
            Method nodeMethod = SwimmerPane.class.getMethod("node");
            Object n = nodeMethod.invoke(pane);
            if (n instanceof Parent pr) root = pr;
            else if (n instanceof Node nn) node = nn;
        } catch (ReflectiveOperationException ignored) { }

        if (root == null) {
            try {
                Method getRoot = SwimmerPane.class.getMethod("getRoot");
                Object r = getRoot.invoke(pane);
                if (r instanceof Parent pr) root = pr;
                else if (r instanceof Node nn) node = nn;
            } catch (ReflectiveOperationException ignored) { }
        }

        if (root == null) {
            if (node == null) {
                throw new IllegalStateException("SwimmerPane did not expose a root via node() or getRoot().");
            }
            root = new StackPane(node); // wrap Node in a Parent for Scene
        }

        // ---- Show ----
        stage.setTitle("SwimmerPane Smoke Test");
        stage.setScene(new Scene(root, 900, 600));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}