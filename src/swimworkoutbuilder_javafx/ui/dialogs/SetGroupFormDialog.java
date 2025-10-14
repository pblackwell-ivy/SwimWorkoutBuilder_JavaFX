package swimworkoutbuilder_javafx.ui.dialogs;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import swimworkoutbuilder_javafx.ui.Theme;

import java.util.Optional;

/**
 * Create/Edit a workout SetGroup: name, reps, notes.
 * (Reps belong to groups, not workouts.)
 */
public final class SetGroupFormDialog { // NEW: entire file
    private SetGroupFormDialog() {}     // NEW

    /** NEW: Simple return type so the caller can pass values to the presenter. */
    public static final class Values { // NEW
        public final String name;     // NEW
        public final int reps;        // NEW
        public final String notes;    // NEW
        public Values(String name, int reps, String notes) { // NEW
            this.name = name; this.reps = reps; this.notes = notes; // NEW
        } // NEW
    } // NEW

    /**
     * NEW: Show a modal dialog and return values if user presses Save.
     * @param title dialog title (e.g., "Add Group" or "Edit Group")
     * @param initialName prefill for name (nullable)
     * @param initialReps prefill for reps (>=1), use 1 if null
     * @param initialNotes prefill for notes (nullable)
     */
    public static Optional<Values> show(String title, String initialName, Integer initialReps, String initialNotes) { // NEW
        Stage dialog = new Stage();                                  // NEW
        dialog.initModality(Modality.APPLICATION_MODAL);             // NEW
        dialog.setTitle(title == null ? "Group" : title);            // NEW

        TextField tfName = new TextField();                          // NEW
        tfName.setPromptText("Group name (e.g., Warmup, Main)");     // NEW

        Spinner<Integer> spReps = new Spinner<>(1, 99, 1);           // NEW
        spReps.setEditable(true);                                    // NEW

        TextArea taNotes = new TextArea();                           // NEW
        taNotes.setPromptText("Optional notes…");                    // NEW
        taNotes.setPrefRowCount(2);                                  // NEW

        // Prefill (NEW)
        if (initialName != null) tfName.setText(initialName);        // NEW
        spReps.getValueFactory().setValue(initialReps == null ? 1 : Math.max(1, initialReps)); // NEW
        if (initialNotes != null) taNotes.setText(initialNotes);     // NEW

        // Buttons (NEW)
        Button btnSave = new Button("Save");                         // NEW
        btnSave.getStyleClass().add("primary");                      // NEW
        Button btnCancel = new Button("Cancel");                     // NEW
        btnSave.setDefaultButton(true);                              // NEW
        btnCancel.setCancelButton(true);                             // NEW

        // Layout (NEW)
        GridPane form = new GridPane();                              // NEW
        form.getStyleClass().add("grid-pane");                       // NEW
        form.setHgap(8); form.setVgap(8); form.setPadding(new Insets(12)); // NEW
        int r = 0;                                                   // NEW
        form.addRow(r++, new Label("Name:"), tfName);                // NEW
        form.addRow(r++, new Label("Reps:"), spReps);                // NEW
        form.addRow(r++, new Label("Notes:"), taNotes);              // NEW

        BorderPane root = new BorderPane(form);                      // NEW
        root.getStyleClass().add("surface");                         // NEW
        var buttons = new javafx.scene.layout.HBox(10, btnCancel, btnSave); // NEW
        buttons.setPadding(new Insets(10, 12, 12, 12));              // NEW
        buttons.setStyle("-fx-alignment: center-right;");            // NEW
        root.setBottom(buttons);                                     // NEW

        Scene scene = new Scene(root, 420, 260);                     // NEW
        Theme.apply(scene, SetGroupFormDialog.class);                   // NEW
        dialog.setScene(scene);                                      // NEW

        final Values[] out = new Values[1];                          // NEW

        btnSave.setOnAction(e -> {                                   // NEW
            String name = tfName.getText().trim();                   // NEW
            if (name.isEmpty()) {                                    // NEW
                new Alert(Alert.AlertType.WARNING, "Please enter a group name.").showAndWait(); // NEW
                return;                                              // NEW
            }                                                        // NEW
            int reps;                                                // NEW
            try { reps = Integer.parseInt(spReps.getEditor().getText().trim()); } // NEW
            catch (Exception ex) { reps = spReps.getValue(); }       // NEW
            reps = Math.max(1, reps);                                // NEW
            String notes = taNotes.getText().trim();                 // NEW
            out[0] = new Values(name, reps, notes.isEmpty() ? null : notes); // NEW
            dialog.close();                                          // NEW
        });                                                          // NEW

        btnCancel.setOnAction(e -> { out[0] = null; dialog.close(); }); // NEW
        dialog.showAndWait();                                        // NEW
        return Optional.ofNullable(out[0]);                          // NEW
    }                                                                 // NEW
}