
package swimworkoutbuilder_javafx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import swimworkoutbuilder_javafx.ui.MainView;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        MainView root = new MainView();

        Scene scene = new Scene(root, 1180, 720);

        var cssURL = Main.class.getResource("/swimworkoutbuilder_javafx/ui/styles.css");
        if (cssURL != null) {
            scene.getStylesheets().add(cssURL.toExternalForm());
        } else {
            System.err.println("WARNING: styles.css not found at /swimworkoutbuilder_javafx/ui/styles.css");
        }
        stage.setTitle("SwimWorkoutBuilder (JavaFX-only)");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
