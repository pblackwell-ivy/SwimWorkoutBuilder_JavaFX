package swimworkoutbuilder_javafx.ui.workout;


import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import swimworkoutbuilder_javafx.model.SwimSet;
import swimworkoutbuilder_javafx.model.enums.Course;
import swimworkoutbuilder_javafx.model.enums.Effort;
import swimworkoutbuilder_javafx.model.enums.StrokeType;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.model.units.TimeSpan;

/**
 * Modal dialog for creating or editing a single {@link SwimSet}.
 *
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *   <li>Displays a form for reps, distance, stroke, effort, course, goal time, and notes.</li>
 *   <li>Parses user input into a {@link SwimSet} object.</li>
 *   <li>Handles both “add” and “edit” modes with prefilled values when applicable.</li>
 * </ul>
 *
 * <p><b>Design Notes:</b></p>
 * <ul>
 *   <li>Implements a static {@code show()} method for simple, modal use.</li>
 *   <li>Follows MVP: the dialog is a dumb view returning a domain object.</li>
 *   <li>Local helper {@code parseFlexible()} handles time formats like “1:05.23” or “45.8”.</li>
 * </ul>
 *
 * @author Parker Blackwell
 * @version 1.0
 * @since 2025-10-13
 * @see SwimSet
 * @see WorkoutBuilderPane
 */
public final class SetFormDialog {

    private SetFormDialog() {}

    /**
     * Shows a blocking modal dialog for editing or creating a SwimSet.
     * @param existing may be null (new set) or an existing set to edit
     * @return Optional of new SwimSet if user pressed Save; empty if cancelled
     */
    public static Optional<SwimSet> show(SwimSet existing) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "Add Set" : "Edit Set");

        // --- Controls ------------------------------------------------------
        TextField tfReps = new TextField("1");
        TextField tfDist = new TextField("50");

        ChoiceBox<StrokeType> cbStroke = new ChoiceBox<>();
        cbStroke.getItems().setAll(StrokeType.values());
        cbStroke.getSelectionModel().select(StrokeType.FREESTYLE);

        ChoiceBox<Effort> cbEffort = new ChoiceBox<>();
        cbEffort.getItems().setAll(Effort.values());
        cbEffort.getSelectionModel().select(Effort.EASY);

        ChoiceBox<Course> cbCourse = new ChoiceBox<>();
        cbCourse.getItems().setAll(Course.values());
        cbCourse.getSelectionModel().select(Course.SCY);

        TextField tfGoal = new TextField("");  // optional goal time
        TextArea taNotes = new TextArea();
        taNotes.setPrefRowCount(2);

        // Prefill from existing
        if (existing != null) {
            tfReps.setText(String.valueOf(existing.getReps()));
            tfDist.setText(String.valueOf(Math.round(existing.getDistancePerRep().toYards())));
            if (existing.getStroke() != null) cbStroke.setValue(existing.getStroke());
            if (existing.getEffort() != null) cbEffort.setValue(existing.getEffort());
            if (existing.getCourse() != null) cbCourse.setValue(existing.getCourse());
            if (existing.getGoalTime() != null) tfGoal.setText(existing.getGoalTime().toString());
            if (existing.getNotes() != null) taNotes.setText(existing.getNotes());
        }

        Button btnSave = new Button("Save");
        Button btnCancel = new Button("Cancel");
        btnSave.setDefaultButton(true);
        btnCancel.setCancelButton(true);

        // --- Layout --------------------------------------------------------
        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.setPadding(new Insets(12));
        int r = 0;
        form.addRow(r++, new Label("Reps:"), tfReps);
        form.addRow(r++, new Label("Distance (yd):"), tfDist);
        form.addRow(r++, new Label("Stroke:"), cbStroke);
        form.addRow(r++, new Label("Effort:"), cbEffort);
        form.addRow(r++, new Label("Course:"), cbCourse);
        form.addRow(r++, new Label("Goal Time (optional):"), tfGoal);
        form.addRow(r++, new Label("Notes:"), taNotes);

        HBox buttons = new HBox(10, btnCancel, btnSave);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(10, 12, 12, 12));

        BorderPane root = new BorderPane(form);
        root.setBottom(buttons);

        dialog.setScene(new Scene(root, 380, 400));
        dialog.setResizable(false);

        final SwimSet[] result = new SwimSet[1];

        btnSave.setOnAction(e -> {
            try {
                int reps = Integer.parseInt(tfReps.getText().trim());
                int dist = Integer.parseInt(tfDist.getText().trim());
                StrokeType stroke = cbStroke.getValue();
                Effort effort = cbEffort.getValue();
                Course course = cbCourse.getValue();
                TimeSpan goal = null;
                String goalText = tfGoal.getText().trim();
                if (!goalText.isEmpty()) {
                    goal = parseFlexible(goalText);
                }
                String notes = taNotes.getText().trim();

                SwimSet s = new SwimSet(stroke, reps, Distance.ofYards(dist), effort, course, notes);
                if (goal != null) s.setGoalTime(goal);

                result[0] = s;
                dialog.close();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Invalid input: " + ex.getMessage()).showAndWait();
            }
        });

        btnCancel.setOnAction(e -> {
            result[0] = null;
            dialog.close();
        });

        dialog.showAndWait();
        return Optional.ofNullable(result[0]);
    }

    // --- helper: parse "m:ss", "m:ss.hh", "ss", "ss.hh" into TimeSpan ----------
    private static TimeSpan parseFlexible(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;

        int minutes = 0;
        int seconds;
        int hundredths = 0;

        String work = s;
        if (work.contains(":")) {
            String[] parts = work.split(":");
            minutes = parts[0].isBlank() ? 0 : Integer.parseInt(parts[0]);
            work = (parts.length >= 2) ? parts[1] : "0";
        }

        if (work.contains(".")) {
            String[] p2 = work.split("\\.");
            seconds = p2[0].isBlank() ? 0 : Integer.parseInt(p2[0]);
            String h = (p2.length >= 2) ? p2[1] : "0";
            if (h.length() == 1) h = h + "0"; // "3.5" → ".50"
            hundredths = Integer.parseInt(h.substring(0, Math.min(2, h.length())));
        } else {
            seconds = work.isBlank() ? 0 : Integer.parseInt(work);
        }

        return TimeSpan.ofMinutesSecondsMillis(minutes, seconds, hundredths * 10);
    }
}
