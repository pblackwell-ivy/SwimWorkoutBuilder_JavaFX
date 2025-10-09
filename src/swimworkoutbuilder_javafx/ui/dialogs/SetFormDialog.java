package swimworkoutbuilder_javafx.ui.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import swimworkoutbuilder_javafx.model.SwimSet;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.model.enums.Course;
import swimworkoutbuilder_javafx.model.enums.Effort;
import swimworkoutbuilder_javafx.model.enums.StrokeType;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.ui.UiUtil;

public class SetFormDialog {
    public static SwimSet show(Workout workout, SwimSet initial) {
        if (workout == null) {
            new Alert(Alert.AlertType.ERROR, "Internal error: workout is null").showAndWait();
            return null;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(initial == null ? "New Set" : "Edit Set");

        Label lblStroke = new Label("Stroke:");
        ChoiceBox<StrokeType> cbStroke = new ChoiceBox<>();
        cbStroke.getItems().setAll(StrokeType.values());
        cbStroke.setValue(StrokeType.FREESTYLE);

        Label lblEffort = new Label("Effort:");
        ChoiceBox<Effort> cbEffort = new ChoiceBox<>();
        cbEffort.getItems().setAll(Effort.values());
        cbEffort.setValue(Effort.ENDURANCE);

        Label lblReps = new Label("Reps:");
        Spinner<Integer> spReps = new Spinner<>(1, 999, 1);

        Course course = workout.getCourse();
        Label lblDist = new Label(course == Course.SCY ? "Distance per rep (yards):" : "Distance per rep (meters):");
        Spinner<Integer> spDistance = new Spinner<>(course == Course.SCY ? 25 : 25, 5000, course == Course.SCY ? 50 : 50, 25);

        Label lblNotes = new Label("Notes:");
        TextArea taNotes = new TextArea();
        taNotes.setTextFormatter(UiUtil.maxLen(100));
        taNotes.setPrefRowCount(5);
        taNotes.setWrapText(true);

        Button btnSave = new Button("Save");
        Button btnCancel = new Button("Cancel");
        btnSave.setDefaultButton(true);
        btnCancel.setCancelButton(true);
        btnSave.setMinWidth(90);
        btnCancel.setMinWidth(90);

        if (initial != null) {
            if (initial.getStroke() != null) cbStroke.setValue(initial.getStroke());
            if (initial.getEffort() != null) cbEffort.setValue(initial.getEffort());
            spReps.getValueFactory().setValue(initial.getReps());
            int dist = (course == Course.SCY)
                    ? (int)Math.round(initial.getDistancePerRep().toYards())
                    : (int)Math.round(initial.getDistancePerRep().toMeters());
            spDistance.getValueFactory().setValue(Math.max(1, dist));
            taNotes.setText(initial.getNotes() == null ? "" : initial.getNotes());
        }

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(10); gp.setPadding(new Insets(16));
        ColumnConstraints c0 = new ColumnConstraints(); c0.setMinWidth(160); c0.setPrefWidth(200);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setHgrow(Priority.ALWAYS);
        gp.getColumnConstraints().addAll(c0, c1);

        int row = 0;
        gp.add(lblStroke, 0, row); gp.add(cbStroke, 1, row++);
        gp.add(lblEffort, 0, row); gp.add(cbEffort, 1, row++);
        gp.add(lblReps,   0, row); gp.add(spReps,   1, row++);
        gp.add(lblDist,   0, row); gp.add(spDistance, 1, row++);
        gp.add(lblNotes,  0, row); gp.add(taNotes,  1, row++);

        HBox buttons = new HBox(10, btnSave, btnCancel);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        gp.add(buttons, 1, row);

        final SwimSet[] result = new SwimSet[1];
        btnSave.setOnAction(e -> {
            int reps = spReps.getValue();
            int amount = spDistance.getValue();
            if (reps < 1 || amount <= 0) {
                new Alert(Alert.AlertType.WARNING, "Reps and distance must be positive.").showAndWait();
                return;
            }
            Distance perRep = (course == Course.SCY) ? Distance.ofYards(amount) : Distance.ofMeters(amount);
            SwimSet s = (initial == null)
                    ? new SwimSet(cbStroke.getValue(), reps, perRep, cbEffort.getValue(), course, taNotes.getText().trim())
                    : new SwimSet(cbStroke.getValue(), reps, perRep, cbEffort.getValue(), course, taNotes.getText().trim());
            result[0] = s;
            dialog.close();
        });
        btnCancel.setOnAction(e -> { result[0] = null; dialog.close(); });

        dialog.setScene(new Scene(gp, 560, 380));
        dialog.setResizable(false);
        dialog.showAndWait();
        return result[0];
    }
}