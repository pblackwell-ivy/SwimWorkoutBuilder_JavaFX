package swimworkoutbuilder_javafx.ui.dialogs;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import swimworkoutbuilder_javafx.model.Swimmer;

public final class SwimmerFormDialog {

    private SwimmerFormDialog() {}

    // Create new swimmer
    public static Swimmer show() {
        return showInternal(null);
    }

    // Edit existing swimmer (prefilled; preserves UUID & seeds)
    public static Swimmer show(Swimmer existing) {
        return showInternal(existing);
    }

    private static Swimmer showInternal(Swimmer existing) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "Add Swimmer" : "Edit Swimmer");

        TextField tfFirst = new TextField(existing == null ? "" : existing.getFirstName());
        TextField tfLast  = new TextField(existing == null ? "" : existing.getLastName());
        TextField tfPref  = new TextField(existing == null ? "" :
                (existing.getPreferredName() == null ? "" : existing.getPreferredName()));
        TextField tfTeam  = new TextField(existing == null ? "" :
                (existing.getTeamName() == null ? "" : existing.getTeamName()));

        GridPane grid = new GridPane();
        grid.getStyleClass().add("grid-pane");
        grid.setHgap(8); grid.setVgap(8); grid.setPadding(new Insets(12));
        int r = 0;
        grid.add(new Label("First name:"), 0, r); grid.add(tfFirst, 1, r++);
        grid.add(new Label("Last name:"),  0, r); grid.add(tfLast,  1, r++);
        grid.add(new Label("Preferred:"),  0, r); grid.add(tfPref,  1, r++);
        grid.add(new Label("Team:"),       0, r); grid.add(tfTeam,  1, r++);

        Button btnCancel = new Button("Cancel");
        Button btnSave   = new Button("Save");
        btnSave.setDefaultButton(true);
        btnCancel.setCancelButton(true);

        HBox buttons = new HBox(10, btnCancel, btnSave);
        buttons.setStyle("-fx-alignment: center-right;");
        buttons.setPadding(new Insets(8));

        BorderPane root = new BorderPane(grid);
        dialog.getScene().getRoot().getStyleClass().add("surface");
        root.setBottom(buttons);

        final Swimmer[] result = new Swimmer[1];
        btnSave.setOnAction(e -> {
            String first = tfFirst.getText().trim();
            String last  = tfLast.getText().trim();
            if (first.isEmpty() || last.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION, "First and last name are required.").showAndWait();
                return;
            }
            if (existing == null) {
                result[0] = new Swimmer(first, last,
                        tfPref.getText().trim().isEmpty() ? null : tfPref.getText().trim(),
                        tfTeam.getText().trim().isEmpty() ? null : tfTeam.getText().trim());
            } else {
                existing.setFirstName(first);
                existing.setLastName(last);
                existing.setPreferredName(tfPref.getText().trim().isEmpty() ? null : tfPref.getText().trim());
                existing.setTeamName(tfTeam.getText().trim().isEmpty() ? null : tfTeam.getText().trim());
                result[0] = existing;
            }
            dialog.close();
        });
        btnCancel.setOnAction(e -> { result[0] = null; dialog.close(); });

        dialog.setScene(new Scene(root, 460, 250));
        dialog.showAndWait();
        return result[0];
    }
}