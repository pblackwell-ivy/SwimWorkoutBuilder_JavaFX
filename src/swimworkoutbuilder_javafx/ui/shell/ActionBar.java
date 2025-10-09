package swimworkoutbuilder_javafx.ui.shell;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.store.LocalStore;
import swimworkoutbuilder_javafx.ui.dialogs.LoadWorkoutDialog;
import swimworkoutbuilder_javafx.ui.dialogs.WorkoutFormDialog;

public class ActionBar {

    private final HBox root = new HBox(10);
    private final Button btnNewWorkout  = new Button("+ New Workout");
    private final Button btnLoadWorkout = new Button("📁 Load Workout");
    private final Button btnPrint       = new Button("⎙ Print");

    public ActionBar() {
        root.setPadding(new Insets(8, 12, 8, 12));

        // Left-side action buttons only (no swimmer chooser here anymore)
        root.getChildren().addAll(btnNewWorkout, btnLoadWorkout, btnPrint);

        // Spacer to keep room on the right if needed later
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        root.getChildren().add(spacer);

        btnNewWorkout.setOnAction(e -> {
            Swimmer cur = AppState.get().getCurrentSwimmer();
            if (cur == null) {
                new Alert(Alert.AlertType.INFORMATION, "Choose or create a swimmer first.").showAndWait();
                return;
            }
            Workout w = WorkoutFormDialog.show(cur.getId(), null);
            if (w != null) {
                try { LocalStore.saveWorkout(w); } catch (Exception ignored) {}
                AppState.get().setCurrentWorkout(w);
            }
        });

        btnLoadWorkout.setOnAction(e -> {
            Swimmer cur = AppState.get().getCurrentSwimmer();
            if (cur == null) {
                new Alert(Alert.AlertType.INFORMATION, "Choose or create a swimmer first.").showAndWait();
                return;
            }
            var w = LoadWorkoutDialog.show(cur.getId());
            if (w != null) AppState.get().setCurrentWorkout(w);
        });

        btnPrint.setOnAction(e ->
                new Alert(Alert.AlertType.INFORMATION, "Print Preview: coming soon.").showAndWait()
        );

        // Enable/disable by state
        AppState.get().currentSwimmerProperty().addListener((obs, o, s) -> {
            btnNewWorkout.setDisable(s == null);
            btnLoadWorkout.setDisable(s == null);
            btnPrint.setDisable(AppState.get().getCurrentWorkout() == null);
        });
        AppState.get().currentWorkoutProperty().addListener((obs, o, w) ->
                btnPrint.setDisable(w == null)
        );

        // Initial state
        btnNewWorkout.setDisable(AppState.get().getCurrentSwimmer() == null);
        btnLoadWorkout.setDisable(AppState.get().getCurrentSwimmer() == null);
        btnPrint.setDisable(AppState.get().getCurrentWorkout() == null);
    }

    public Node node() { return root; }
}