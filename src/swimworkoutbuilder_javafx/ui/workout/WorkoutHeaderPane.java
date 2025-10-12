package swimworkoutbuilder_javafx.ui.workout;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.model.enums.Course;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.store.LocalStore;

import java.io.IOException;

/**
 * Minimal, focused editor for the Workout header (name, course, notes)
 * with explicit Edit / Save / Cancel and a deep-copy staging buffer.
 *
 * Behavior:
 *  - When a Workout is selected in AppState, fields populate.
 *  - Edit → copies current workout into a buffer and enables inputs.
 *  - Save → copy buffer back into current workout and persist.
 *  - Cancel → discard buffer, restore read-only view of current workout.
 *  - Disabled when no workout is selected.
 */
public final class WorkoutHeaderPane {

    // --- UI
    private final VBox root = new VBox(10);

    private final TextField tfName   = new TextField();
    private final ToggleGroup tgPool = new ToggleGroup();
    private final RadioButton rb25yd = new RadioButton("25 yd");
    private final RadioButton rb25m  = new RadioButton("25 m");
    private final RadioButton rb50m  = new RadioButton("50 m");
    private final TextArea   taNotes = new TextArea();

    private final Button btnEdit   = new Button("Edit");
    private final Button btnSave   = new Button("Save");
    private final Button btnCancel = new Button("Cancel");

    // staging buffer during edits (deep copy)
    private Workout buffer = null;

    public WorkoutHeaderPane() {
        buildUI();
        wireState();
        syncFromCurrent(false); // initial populate
        setEditing(false);
    }

    public Node node() { return root; }

    // ------------------------------------------------------------
    // UI
    // ------------------------------------------------------------
    private void buildUI() {
        root.setPadding(new Insets(8, 12, 8, 12));

        var title = new Label("Workout");
        title.getStyleClass().add("section-title");

        // Name
        tfName.setPromptText("Workout name (e.g., Tuesday Threshold)");
        tfName.setPrefColumnCount(28);

        // Pool length radios
        rb25yd.setToggleGroup(tgPool);
        rb25m.setToggleGroup(tgPool);
        rb50m.setToggleGroup(tgPool);

        HBox poolRow = new HBox(10, new Label("Pool length:"), rb25yd, rb25m, rb50m);
        poolRow.setAlignment(Pos.CENTER_LEFT);

        // Notes
        taNotes.setPromptText("Notes (optional)");
        taNotes.setPrefRowCount(2);

        // Buttons
        HBox buttons = new HBox(8, btnEdit, btnCancel, btnSave);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.addRow(0, new Label("Name:"), tfName);
        form.addRow(1, new Label("Course:"), poolRow);
        form.addRow(2, new Label("Notes:"), taNotes);

        ColumnConstraints c0 = new ColumnConstraints();
        ColumnConstraints c1 = new ColumnConstraints();
        c0.setHgrow(Priority.NEVER);
        c1.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c0, c1);

        root.getChildren().addAll(title, form, buttons);
    }

    // ------------------------------------------------------------
    // Behavior
    // ------------------------------------------------------------
    private void wireState() {
        // Toggle editing
        btnEdit.setOnAction(e -> beginEdit());
        btnCancel.setOnAction(e -> cancelEdit());
        btnSave.setOnAction(e -> saveEdit());

        // React to current workout changes
        AppState.get().currentWorkoutProperty().addListener((obs, oldW, newW) -> {
            if (isEditing()) {
                // if workout switched while editing, discard buffer
                buffer = null;
                setEditing(false);
            }
            syncFromCurrent(false);
        });
    }

    private boolean isEditing() { return buffer != null; }

    private void setEditing(boolean editing) {
        boolean hasWorkout = AppState.get().getCurrentWorkout() != null;

        tfName.setEditable(editing && hasWorkout);
        taNotes.setEditable(editing && hasWorkout);
        rb25yd.setDisable(!(editing && hasWorkout));
        rb25m.setDisable(!(editing && hasWorkout));
        rb50m.setDisable(!(editing && hasWorkout));

        btnEdit.setDisable(editing || !hasWorkout);
        btnCancel.setDisable(!editing);
        btnSave.setDisable(!editing);
    }

    private void beginEdit() {
        var current = AppState.get().getCurrentWorkout();
        if (current == null) return;

        buffer = new Workout(current); // deep copy constructor
        // populate fields from buffer
        tfName.setText(buffer.getName());
        taNotes.setText(buffer.getNotes() == null ? "" : buffer.getNotes());
        selectCourse(buffer.getCourse());

        setEditing(true);
    }

    private void cancelEdit() {
        buffer = null;
        syncFromCurrent(true); // reload display from actual current workout
        setEditing(false);
    }

    private void saveEdit() {
        if (buffer == null) return;
        var current = AppState.get().getCurrentWorkout();
        if (current == null) { cancelEdit(); return; }

        // push UI -> buffer
        buffer.setName(tfName.getText().trim());
        buffer.setNotes(taNotes.getText());
        buffer.setCourse(selectedCourse());

        // copy buffer -> current (preserves id/createdAt)
        current.copyFrom(buffer);

        // persist
        try {
            LocalStore.saveWorkout(current);
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Unable to save workout:\n" + ex.getMessage()).showAndWait();
            return; // stay in edit mode so user can retry or cancel
        }

        buffer = null;
        setEditing(false);
        syncFromCurrent(false);
    }

    private void syncFromCurrent(boolean preserveFocus) {
        var w = AppState.get().getCurrentWorkout();
        boolean has = (w != null);

        tfName.setText(has ? w.getName() : "");
        taNotes.setText(has ? (w.getNotes() == null ? "" : w.getNotes()) : "");
        selectCourse(has ? w.getCourse() : null);

        // lock when no workout
        tfName.setDisable(!has);
        taNotes.setDisable(!has);
        rb25yd.setDisable(!has);
        rb25m.setDisable(!has);
        rb50m.setDisable(!has);

        // buttons reflect mode
        btnEdit.setDisable(!has || isEditing());
        btnCancel.setDisable(!isEditing());
        btnSave.setDisable(!isEditing());

        if (!preserveFocus) tfName.getParent().requestFocus();
    }

    private void selectCourse(Course c) {
        if (c == null) {
            tgPool.selectToggle(null);
            return;
        }
        switch (c) {
            case SCY -> tgPool.selectToggle(rb25yd);
            case SCM -> tgPool.selectToggle(rb25m);
            case LCM -> tgPool.selectToggle(rb50m);
        }
    }

    private Course selectedCourse() {
        var t = tgPool.getSelectedToggle();
        if (t == rb25yd) return Course.SCY;
        if (t == rb25m)  return Course.SCM;
        if (t == rb50m)  return Course.LCM;
        return Course.SCY; // default
    }
}