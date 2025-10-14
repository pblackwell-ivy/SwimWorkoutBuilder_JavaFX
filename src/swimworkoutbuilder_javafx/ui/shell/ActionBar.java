package swimworkoutbuilder_javafx.ui.shell;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.store.LocalStore;
import swimworkoutbuilder_javafx.ui.dialogs.LoadWorkoutDialog;
import swimworkoutbuilder_javafx.ui.dialogs.WorkoutFormDialog;
import swimworkoutbuilder_javafx.ui.workout.WorkoutBuilderPresenter; // NEW

import java.io.IOException;

public class ActionBar {

    private final HBox root = new HBox(10);
    private final Button btnNewWorkout  = new Button("+ New Workout");
    private final Button btnLoadWorkout = new Button("📁 Load Workout");
    private final Button btnSave        = new Button("💾 Save");   // NEW
    private final Button btnCancel      = new Button("↩ Cancel"); // NEW
    private final Button btnPrint       = new Button("⎙ Print");

    private WorkoutBuilderPresenter presenter; // NEW (nullable)

    // --- ORIGINAL no-arg constructor ---
    public ActionBar() { this(null); } // delegates to new one  ✅

    // --- NEW constructor with presenter wiring ---
    public ActionBar(WorkoutBuilderPresenter presenter) { // CHANGED
        this.presenter = presenter; // may be null

        root.setPadding(new Insets(8, 12, 8, 12));

        btnNewWorkout.getStyleClass().add("primary");
        btnLoadWorkout.getStyleClass().add("secondary");
        btnSave.getStyleClass().add("primary");
        btnCancel.getStyleClass().add("ghost");
        btnPrint.getStyleClass().add("ghost");

        root.getChildren().addAll(btnNewWorkout, btnLoadWorkout, btnSave, btnCancel, btnPrint);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        root.getChildren().add(spacer);

        AppState app = AppState.get();

        btnNewWorkout.setOnAction(e -> {
            var cur = app.getCurrentSwimmer();
            if (cur == null) {
                new Alert(Alert.AlertType.INFORMATION, "Choose or create a swimmer first.").showAndWait();
                return;
            }
            Workout w = WorkoutFormDialog.show(cur.getId(), null);
            if (w != null) {
                try { LocalStore.saveWorkout(w); } catch (Exception ignored) {}
                app.setCurrentWorkout(w);
            }
        });

        btnLoadWorkout.setOnAction(e -> {
            var cur = app.getCurrentSwimmer();
            if (cur == null) {
                new Alert(Alert.AlertType.INFORMATION, "Choose or create a swimmer first.").showAndWait();
                return;
            }
            var w = LoadWorkoutDialog.show(cur.getId());
            if (w != null) app.setCurrentWorkout(w);
        });

        // NEW: only wire save/cancel if presenter available
        if (presenter != null) {
            btnSave.setOnAction(e -> {
                var w = app.getCurrentWorkout();
                if (w == null) return;
                try {
                    presenter.commitTo(w);
                    new Alert(Alert.AlertType.INFORMATION, "Workout saved.").showAndWait();
                } catch (IOException ex) {
                    new Alert(Alert.AlertType.ERROR, "Save failed: " + ex.getMessage()).showAndWait();
                }
            });

            btnCancel.setOnAction(e -> {
                var w = app.getCurrentWorkout();
                if (w == null) return;
                try {
                    var reloaded = LocalStore.loadWorkout(w.getId());
                    if (reloaded != null) app.setCurrentWorkout(reloaded);
                } catch (IOException ex) {
                    new Alert(Alert.AlertType.ERROR, "Reload failed: " + ex.getMessage()).showAndWait();
                }
            });

            var noWorkout = app.currentWorkoutProperty().isNull();
            var clean = presenter.dirtyProperty().not();
            btnSave.disableProperty().bind(noWorkout.or(clean));
            btnCancel.disableProperty().bind(noWorkout.or(clean));
        } else {
            // fallback: disabled if no presenter
            btnSave.setDisable(true);
            btnCancel.setDisable(true);
        }

        btnPrint.setOnAction(e ->
                new Alert(Alert.AlertType.INFORMATION, "Print Preview: coming soon.").showAndWait()
        );

        // Disable new/load when no swimmer selected
        btnNewWorkout.disableProperty().bind(app.currentSwimmerProperty().isNull());
        btnLoadWorkout.disableProperty().bind(app.currentSwimmerProperty().isNull());
    }

    public Node node() { return root; }
}