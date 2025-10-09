
package swimworkoutbuilder_javafx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import swimworkoutbuilder_javafx.ui.MainView;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        MainView root = new MainView();
        stage.setTitle("SwimWorkoutBuilder (JavaFX-only)");
        stage.setScene(new Scene(root, 1180, 720));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
