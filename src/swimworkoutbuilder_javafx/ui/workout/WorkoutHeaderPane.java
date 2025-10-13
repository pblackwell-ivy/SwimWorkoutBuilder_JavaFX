package swimworkoutbuilder_javafx.ui.workout;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.model.enums.Course;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.state.AppState;

/**
 * Compact header for the current workout: name, notes, course, distance, timestamps.
 * <p>
 * This version intentionally avoids binding to presenter text properties (like
 * totalDistanceTextProperty()) so it compiles cleanly with the current codebase.
 * It listens to AppState.currentWorkoutProperty() and computes display values
 * directly from the model.
 */
public final class WorkoutHeaderPane {

    private final VBox root = new VBox(6);

    // Top row: name (editable) + right-side summary
    private final TextField tfName = new TextField();
    private final Label lblSummary = new Label();          // e.g., "SCY • 2,000 yd"
    private final Label lblNotes = new Label();            // muted, single line
    private final Label lblTimestamps = new Label();       // created/updated

    private Workout current;

    public WorkoutHeaderPane(AppState app /* kept for future presenter hooks */) {
        // Layout
        root.setPadding(new Insets(8, 8, 8, 8));
        root.getStyleClass().add("workout-header");

        tfName.setPromptText("Workout name");
        tfName.setPrefColumnCount(24);

        lblNotes.getStyleClass().add("text-subtle");
        lblSummary.getStyleClass().add("text-strong");

        HBox topRow = new HBox(10, tfName);
        HBox.setHgrow(tfName, Priority.ALWAYS);
        // spacer + summary on the right
        var spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topRow.getChildren().addAll(spacer, lblSummary);
        topRow.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(topRow, lblNotes, lblTimestamps);

        // Wire state: respond to workout changes
        app.currentWorkoutProperty().addListener((obs, oldW, newW) -> {
            current = newW;
            refreshFromModel();
        });

        // Inline edit of name writes back to the model (and touches updatedAt up-stack)
        tfName.textProperty().addListener((obs, o, n) -> {
            if (current != null) {
                current.setName(n == null ? "" : n.trim());
            }
        });

        // Initial paint
        current = app.getCurrentWorkout();
        refreshFromModel();
    }

    public Node node() {
        return root;
    }

    // ---- helpers ----

    private void refreshFromModel() {
        if (current == null) {
            tfName.setText("");
            lblNotes.setText("");
            lblSummary.setText("—");
            lblTimestamps.setText("");
            return;
        }

        // Name
        tfName.setText(safe(current.getName()));

        // Notes (muted, single line)
        String notes = safe(current.getNotes());
        lblNotes.setText(notes.isBlank() ? "" : notes);

        // Course + Distance (computed from model)
        Course c = current.getCourse();
        Distance total = current.totalDistance();
        String summary = (c == null ? "" : c.name()) + (total == null ? "" : " • " + total.toShortString());
        lblSummary.setText(summary.isBlank() ? "—" : summary);

        // Timestamps (if present)
        String created = current.getCreatedAt() == null ? "" : current.getCreatedAt().toString();
        String updated = current.getUpdatedAt() == null ? "" : current.getUpdatedAt().toString();
        String ts = "";
        if (!created.isBlank()) ts = "Created: " + created;
        if (!updated.isBlank()) ts = (ts.isBlank() ? "" : ts + "   ") + "Updated: " + updated;
        lblTimestamps.setText(ts);
        lblTimestamps.getStyleClass().add("text-subtle");
    }

    private static String safe(String s) { return s == null ? "" : s; }
}