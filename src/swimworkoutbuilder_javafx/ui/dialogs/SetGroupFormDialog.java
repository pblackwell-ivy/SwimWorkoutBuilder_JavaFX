package swimworkoutbuilder_javafx.ui.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import swimworkoutbuilder_javafx.model.SetGroup;

public class SetGroupFormDialog {

    public static SetGroup show(SetGroup existing) {
        Dialog<SetGroup> d = new Dialog<>();
        d.initStyle(StageStyle.UTILITY);
        d.initModality(Modality.APPLICATION_MODAL);
        d.setTitle(existing == null ? "New Group" : "Edit Group");

        ButtonType BT_SAVE = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(BT_SAVE, ButtonType.CANCEL);

        TextField tfName = new TextField();
        tfName.setPromptText("e.g. Main Set");

        Spinner<Integer> spReps = new Spinner<>(1, 999, 1);
        spReps.setEditable(true);

        // NEW: rest between sets (seconds)
        Spinner<Integer> spRestBetweenSets = new Spinner<>(0, 3600, 0);
        spRestBetweenSets.setEditable(true);

        // EXISTING: rest between group repeats (seconds)
        Spinner<Integer> spRestAfterGroup = new Spinner<>(0, 3600, 0);
        spRestAfterGroup.setEditable(true);

        TextField tfNotes = new TextField();
        tfNotes.setPromptText("Notes (≤100 chars)");
        tfNotes.textProperty().addListener((obs, o, n) -> {
            if (n != null && n.length() > 100) tfNotes.setText(n.substring(0, 100));
        });

        // Populate if editing
        if (existing != null) {
            tfName.setText(existing.getName());
            spReps.getValueFactory().setValue(existing.getReps());
            spRestBetweenSets.getValueFactory().setValue(existing.getRestBetweenSetsSec());
            spRestAfterGroup.getValueFactory().setValue(existing.getRestAfterGroupSec());
            tfNotes.setText(existing.getNotes() == null ? "" : existing.getNotes());
        }

        GridPane gp = new GridPane();
        gp.setHgap(8);
        gp.setVgap(8);
        gp.setPadding(new Insets(10));

        int r = 0;
        gp.add(new Label("Group name:"), 0, r); gp.add(tfName, 1, r++);
        gp.add(new Label("Repetitions:"), 0, r); gp.add(spReps, 1, r++);
        gp.add(new Label("Rest between sets (sec):"), 0, r); gp.add(spRestBetweenSets, 1, r++);
        gp.add(new Label("Rest between group repeats (sec):"), 0, r); gp.add(spRestAfterGroup, 1, r++);
        gp.add(new Label("Notes:"), 0, r); gp.add(tfNotes, 1, r++);

        d.getDialogPane().setContent(gp);

        d.setResultConverter(bt -> {
            if (bt == BT_SAVE) {
                SetGroup g = existing == null ? new SetGroup() : existing;
                g.setName(tfName.getText() == null ? "" : tfName.getText().trim());
                g.setReps(safe(spReps.getValue()));
                g.setRestBetweenSetsSec(safe(spRestBetweenSets.getValue()));
                g.setRestAfterGroupSec(safe(spRestAfterGroup.getValue()));
                g.setNotes(tfNotes.getText());
                return g;
            }
            return null;
        });

        return d.showAndWait().orElse(null);
    }

    private static int safe(Integer v) { return v == null ? 0 : Math.max(0, v); }
}