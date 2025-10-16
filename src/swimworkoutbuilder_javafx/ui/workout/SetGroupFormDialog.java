package swimworkoutbuilder_javafx.ui.workout;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import swimworkoutbuilder_javafx.model.SetGroup;
import swimworkoutbuilder_javafx.ui.UiUtil;
import swimworkoutbuilder_javafx.ui.common.DialogUtil;

public final class SetGroupFormDialog {
    private SetGroupFormDialog() { }

    public static SetGroup show(SetGroup initial) {
        Stage dialog = new Stage();

        Label lblName = new Label("Group name:");
        TextField tfName = new TextField();
        tfName.setPromptText("e.g., Warmup, Main, Cooldown");
        tfName.setTextFormatter(UiUtil.maxLen(60));

        Label lblReps = new Label("Repetitions:");
        Spinner<Integer> spReps = new Spinner<>(1, 99, 1);
        spReps.setEditable(true);
        spReps.setPrefWidth(80);

        Label lblNote = new Label("Note:");
        TextField tfNote = new TextField();
        tfNote.setPromptText("optional");
        tfNote.setTextFormatter(UiUtil.maxLen(100));

        if (initial != null) {
            tfName.setText(initial.getName());
            spReps.getValueFactory().setValue(Math.max(1, initial.getReps()));
            tfNote.setText(initial.getNotes()==null? "" : initial.getNotes());
        }

        Button btnSave = new Button("Save");
        Button btnCancel = new Button("Cancel");
        btnSave.getStyleClass().addAll("button","primary");
        btnCancel.getStyleClass().addAll("button","secondary");
        btnSave.setDefaultButton(true);
        btnCancel.setCancelButton(true);

        final SetGroup[] out = new SetGroup[1];
        btnSave.setOnAction(e -> {
            String name = tfName.getText()==null? "" : tfName.getText().trim();
            if (name.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Group name is required.").showAndWait();
                return;
            }
            int reps = spReps.getValue();
            String note = tfNote.getText() == null ? "" : tfNote.getText().trim();
            if (initial == null) {
                SetGroup sg = new SetGroup(name);
                sg.setReps(reps);
                sg.setNotes(note);
                out[0] = sg;
            } else {
                initial.setName(name);
                initial.setReps(reps);
                initial.setNotes(note);
                out[0] = initial;
            }
            dialog.close();
        });
        btnCancel.setOnAction(e -> { out[0] = null; dialog.close(); });

        GridPane gp = new GridPane();
        gp.getStyleClass().add("form-grid");
        gp.setPadding(new Insets(12));

        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(110);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        gp.getColumnConstraints().addAll(c0,c1);

        gp.add(lblName, 0, 0); gp.add(tfName, 1, 0);
        gp.add(lblReps, 0, 1); gp.add(spReps, 1, 1);
        gp.add(lblNote, 0, 2); gp.add(tfNote, 1, 2);

        HBox buttons = new HBox(10, btnCancel, btnSave);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        gp.add(buttons, 1, 3);

        Scene scene = new Scene(gp, 460, 240);
        DialogUtil.prime(dialog, scene, null, 460, 240, (initial==null? "Add Group" : "Edit Group"));
        dialog.showAndWait();
        return out[0];
    }
}
