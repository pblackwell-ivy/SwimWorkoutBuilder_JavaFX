package swimworkoutbuilder_javafx;


import java.io.IOException;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.store.LocalStore;
import swimworkoutbuilder_javafx.ui.MainView;

/**
 * Entry point for the SwimWorkoutBuilder JavaFX application.
 *
 * <p>This class initializes global state ({@link AppState}), loads saved swimmers
 * from disk, restores the last selected swimmer if available, and sets up
 * the main UI layout via {@link MainView}.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Initialize JavaFX application stage and scene.</li>
 *   <li>Load swimmers from {@link LocalStore} and populate {@link AppState}.</li>
 *   <li>Ensure graceful recovery if no swimmers can be loaded.</li>
 *   <li>Apply CSS stylesheet and show the main window.</li>
 * </ul>
 *
 * @author Parker Blackwell
 * @version MVP 1.0 (October 2025)
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // Access shared application state
        var app = AppState.get();

        // Build root UI layout
        MainView root = new MainView();

        // --- Load swimmers from disk into AppState ---
        try {
            app.setSwimmers(FXCollections.observableArrayList(LocalStore.listAllSwimmers()));
        } catch (IOException ex) {
            ex.printStackTrace(); // log for developer visibility
            new Alert(
                    Alert.AlertType.WARNING,
                    "Could not load saved swimmers. You can still create a new swimmer.\n\nDetails: " + ex.getMessage()
            ).showAndWait();
            app.setSwimmers(FXCollections.observableArrayList());
        }

        // --- Select first swimmer if none currently active ---
        if (app.getCurrentSwimmer() == null && !app.getSwimmers().isEmpty()) {
            app.setCurrentSwimmer(app.getSwimmers().get(0));
        }

        // --- Scene setup ---
        Scene scene = new Scene(root, 1180, 720);

        // --- Load and apply stylesheet ---
        var cssName = "styles-ocean-depth.css";
        var cssPathAndFile = "/swimworkoutbuilder_javafx/ui/" + cssName;
        var cssURL = Main.class.getResource(cssPathAndFile);
        if (cssURL != null) {
            scene.getStylesheets().add(cssURL.toExternalForm());
        } else {
            System.err.println("WARNING: " + cssName + " not found at " + cssPathAndFile);
        }

        // --- Stage setup ---
        stage.setTitle("SwimWorkoutBuilder (JavaFX-only)");
        stage.setScene(scene);
        stage.show();
    }

    /** Launches the JavaFX application. */
    public static void main(String[] args) {
        launch(args);
    }
}
