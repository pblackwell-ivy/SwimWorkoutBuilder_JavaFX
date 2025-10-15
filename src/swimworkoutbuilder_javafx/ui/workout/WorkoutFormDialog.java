package swimworkoutbuilder_javafx.ui.workout;


import java.util.UUID;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.model.enums.Course;
import swimworkoutbuilder_javafx.ui.Theme;

/**
 * Create or edit a Workout (name, course, notes).
 * Reps belong to SetGroup, so the Workout dialog intentionally does NOT include reps.
 */
public final class WorkoutFormDialog {
    private WorkoutFormDialog() {}

    /**
     * Show the dialog. If existing is null, creates a new Workout for swimmerId.
     * Returns the created/edited Workout, or null if cancelled.
     */
    public static Workout show(UUID swimmerId, Workout existing) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "New Workout" : "Edit Workout");

        // --- Fields --------------------------------------------------------
        TextField tfName = new TextField();
        tfName.setPromptText("Workout name (e.g., Main Threshold)");
        ChoiceBox<Course> cbCourse = new ChoiceBox<>();
        cbCourse.getItems().setAll(Course.values());

        // Plain labels for course, no multipliers
        cbCourse.setConverter(new StringConverter<>() {
            @Override public String toString(Course c) { return c == null ? "" : c.name(); }  // CHANGED: plain label
            @Override public Course fromString(String s) { return Course.valueOf(s); }
        });

        TextArea taNotes = new TextArea();
        taNotes.setPromptText("Optional notes…");
        taNotes.setPrefRowCount(3);

        // Prefill when editing
        if (existing != null) {
            tfName.setText(existing.getName());
            cbCourse.setValue(existing.getCourse());
            if (existing.getNotes() != null) taNotes.setText(existing.getNotes());
        } else {
            // Sensible defaults
            cbCourse.getSelectionModel().select(Course.SCY);
        }

        // --- Buttons -------------------------------------------------------
        Button btnSave = new Button("Save");
        btnSave.getStyleClass().add("primary");
        Button btnCancel = new Button("Cancel");

        btnSave.setDefaultButton(true);
        btnCancel.setCancelButton(true);

        // --- Layout --------------------------------------------------------
        GridPane form = new GridPane();
        form.getStyleClass().add("grid-pane");
        form.setHgap(8);
        form.setVgap(8);
        form.setPadding(new Insets(12));

        int r = 0;
        form.addRow(r++, new Label("Name:"),   tfName);
        form.addRow(r++, new Label("Course:"), cbCourse);
        form.addRow(r++, new Label("Notes:"),  taNotes);

        BorderPane root = new BorderPane(form);
        root.getStyleClass().add("surface");
        ToolBar bar = new ToolBar(new Separator(), new Separator()); // keeps height tidy
        // Use an HBox if you prefer right-aligned buttons:
        var buttons = new javafx.scene.layout.HBox(10, btnCancel, btnSave);
        buttons.setPadding(new Insets(10, 12, 12, 12));
        buttons.setStyle("-fx-alignment: center-right;");
        root.setBottom(buttons);

        Scene scene = new Scene(root, 460, 280);
        Theme.apply(scene, WorkoutFormDialog.class);                // NEW: theme hook
        dialog.setScene(scene);

        final Workout[] result = new Workout[1];

        // --- Actions -------------------------------------------------------
        btnSave.setOnAction(e -> {
            String name = tfName.getText().trim();
            Course course = cbCourse.getValue();
            String notes = taNotes.getText().trim();

            if (name.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Please enter a workout name.").showAndWait();
                return;
            }
            if (course == null) {
                new Alert(Alert.AlertType.WARNING, "Please choose a course.").showAndWait();
                return;
            }

            if (existing == null) {
                // Create brand-new workout
                Workout w = new Workout(swimmerId, name, course, notes, /* default rest */ 60);
                result[0] = w;
            } else {
                // Edit in place
                existing.setName(name);
                existing.setCourse(course);
                existing.setNotes(notes);
                result[0] = existing;
            }
            dialog.close();
        });

        btnCancel.setOnAction(e -> {
            result[0] = null;
            dialog.close();
        });

        dialog.showAndWait();
        return result[0];
    }
}
