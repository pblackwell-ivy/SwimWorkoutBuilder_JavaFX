package swimworkoutbuilder_javafx.ui.dialogs;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.store.LocalStore;

import java.util.UUID;

public final class LoadWorkoutDialog {

    private LoadWorkoutDialog() {}

    /** Show a modal dialog listing workouts for the swimmer; return the chosen one or null if cancelled. */
    public static Workout show(UUID swimmerId) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Load Workout");

        ListView<Workout> list = new ListView<>();
        list.getItems().setAll(LocalStore.listWorkoutsFor(swimmerId));

        // Nice list text: "Workout Name — optional notes"
        list.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(Workout w, boolean empty) {
                super.updateItem(w, empty);
                if (empty || w == null) { setText(null); return; }
                String notes = (w.getNotes()==null || w.getNotes().isBlank()) ? "" : " — " + w.getNotes();
                setText(w.getName() + notes);
            }
        });

        Button btnOpen = new Button("Open");
        Button btnCancel = new Button("Cancel");
        btnOpen.setDefaultButton(true);
        btnCancel.setCancelButton(true);
        btnOpen.setDisable(true);

        list.getSelectionModel().selectedItemProperty().addListener((obs,o,sel) -> btnOpen.setDisable(sel==null));

        final Workout[] result = new Workout[1];
        btnOpen.setOnAction(e -> { result[0] = list.getSelectionModel().getSelectedItem(); dialog.close(); });
        btnCancel.setOnAction(e -> { result[0] = null; dialog.close(); });

        BorderPane root = new BorderPane(list);
        HBox buttons = new HBox(10, btnCancel, btnOpen);
        buttons.setPadding(new Insets(8));
        buttons.setStyle("-fx-alignment: center-right;");
        root.setBottom(buttons);
        root.setPadding(new Insets(8));

        dialog.setScene(new Scene(root, 520, 380));
        dialog.showAndWait();
        return result[0];
    }
}
