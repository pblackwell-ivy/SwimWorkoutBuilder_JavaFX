package swimworkoutbuilder_javafx.ui.workout;


import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.store.LocalStore;
import swimworkoutbuilder_javafx.ui.Theme;
/**
 * [Dialog] LoadWorkoutDialog for the "dialogs" feature.
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *   <li>Collect user input with clear primary/secondary actions</li>
 *   <li>Validate inputs and surface errors accessibly</li>
 *   <li>Return a result to the caller</li>
 * </ul>
 *
 * <p><b>Design Notes:</b>
 * <ul>
 *   <li>Follows canonical roles (primary/secondary/tertiary/destructive)</li>
 *   <li>ESC/Enter keys match platform expectations</li>
 *   <li>Validation separated from presentation</li>
 * </ul>
 *
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * // Typical usage for LoadWorkoutDialog
 * LoadWorkoutDialog obj = new LoadWorkoutDialog();
 * obj.toString(); // replace with real usage
 * }</pre>
 *
 * @author Parker Blackwell
 * @version 1.0
 * @since 2025-10-14
 */

public final class LoadWorkoutDialog {

    private LoadWorkoutDialog() {}

    /** Show a modal dialog listing workouts for the swimmer; return the chosen one or null if cancelled. */
    public static Workout show(UUID swimmerId) {
        if (swimmerId == null) {
            new Alert(Alert.AlertType.WARNING, "Select a swimmer first.").showAndWait();
            return null;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Load Workout");

        ListView<Workout> list = new ListView<>();

        // Load from disk (handle I/O errors gracefully)
        List<Workout> items;
        try {
            items = LocalStore.listWorkoutsFor(swimmerId);
        } catch (Exception ex) {
            items = Collections.emptyList();
            new Alert(Alert.AlertType.ERROR,
                    "Unable to read workouts from disk.\n\n" + ex.getMessage()).showAndWait();
        }
        list.getItems().setAll(items);

        // Cell text: "Name — optional notes"
        list.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(Workout w, boolean empty) {
                super.updateItem(w, empty);
                if (empty || w == null) { setText(null); return; }
                String notes = (w.getNotes() == null || w.getNotes().isBlank()) ? "" : " — " + w.getNotes();
                setText(w.getName() + notes);
            }
        });

        Button btnOpen = new Button("Open");
        Button btnCancel = new Button("Cancel");
        btnOpen.getStyleClass().addAll("button","primary");     // new
        btnCancel.getStyleClass().addAll("button","secondary"); // new
        btnOpen.setDefaultButton(true);
        btnCancel.setCancelButton(true);
        btnOpen.setDisable(true);

        list.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, sel) -> btnOpen.setDisable(sel == null));

        // Allow double-click or Enter to open
        list.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && list.getSelectionModel().getSelectedItem() != null) {
                btnOpen.fire();
            }
        });
        list.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && list.getSelectionModel().getSelectedItem() != null) {
                btnOpen.fire();
            }
        });

        final Workout[] result = new Workout[1];
        btnOpen.setOnAction(e -> {
            Workout sel = list.getSelectionModel().getSelectedItem();
            if (sel == null) { result[0] = null; dialog.close(); return; }
            try {
                // IMPORTANT: fetch the full workout with groups/sets
                result[0] = LocalStore.loadWorkout(sel.getId());
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Unable to load workout: " + ex.getMessage()).showAndWait();
                result[0] = null;
            }
            dialog.close();
        });
        btnCancel.setOnAction(e -> { result[0] = null; dialog.close(); });

        BorderPane root = new BorderPane(list);
        root.getStyleClass().add("surface");           // new
        HBox buttons = new HBox(10, btnCancel, btnOpen);
        buttons.setPadding(new Insets(8));
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.getStyleClass().add("toolbar");     // new
        root.setBottom(buttons);
        root.setPadding(new Insets(8));

        Scene scene = new Scene(root, 520, 380);
        Theme.apply(scene, LoadWorkoutDialog.class);
        dialog.setScene(scene);

        dialog.setScene(scene);
        dialog.showAndWait();
        return result[0];
    }
}
