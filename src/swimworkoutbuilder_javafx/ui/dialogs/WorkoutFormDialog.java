package swimworkoutbuilder_javafx.ui.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.model.enums.Course;
import swimworkoutbuilder_javafx.ui.Theme;
import swimworkoutbuilder_javafx.ui.UiUtil;

import java.net.URL;
import java.util.Objects;
import java.util.UUID;

public class WorkoutFormDialog {

    /**
     * @param swimmerId swimmer the workout belongs to
     * @param initial   if non-null, dialog edits this workout in place
     * @return the created/edited workout, or null if cancelled
     */
    public static Workout show(UUID swimmerId, Workout initial) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(initial == null ? "New Workout" : "Edit Workout");

        // Labels/controls
        Label lblName = new Label("Name:");
        TextField tfName = new TextField();

        Label lblCourse = new Label("Course:");
        ChoiceBox<Course> cbCourse = new ChoiceBox<>();
        cbCourse.getItems().addAll(Course.SCY, Course.SCM, Course.LCM);
        cbCourse.setValue(Course.SCY);

        Label lblNotes = new Label("Notes:");
        TextArea taNotes = new TextArea();
        taNotes.setTextFormatter(UiUtil.maxLen(100));
        taNotes.setPrefRowCount(8);
        taNotes.setWrapText(true);
        // QUIETER FORM LABELS (theme style)
        lblName.getStyleClass().add("label-column-header");    // NEW
        lblCourse.getStyleClass().add("label-column-header");  // NEW
        lblNotes.getStyleClass().add("label-column-header");   // NEW

        Button btnSave = new Button("Save");
        Button btnCancel = new Button("Cancel");
        btnSave.getStyleClass().addAll("button","primary");     // new
        btnCancel.getStyleClass().addAll("button","secondary"); // new
        btnSave.setDefaultButton(true);
        btnCancel.setCancelButton(true);
        btnSave.setMinWidth(90);
        btnCancel.setMinWidth(90);

        // Pre-fill on edit
        if (initial != null) {
            tfName.setText(initial.getName());
            taNotes.setText(initial.getNotes() == null ? "" : initial.getNotes());
            cbCourse.setValue(initial.getCourse());
        }

        // Layout
        GridPane gp = new GridPane();
        gp.setHgap(10);
        gp.setVgap(10);
        gp.setPadding(new Insets(16));

        // THEMED FORM + CARD SURFACE
        gp.getStyleClass().addAll("grid-pane", "surface");     // NEW

        // Column sizing: labels fixed width, fields grow
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(90);
        c0.setPrefWidth(110);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        gp.getColumnConstraints().addAll(c0, c1);

        // Rows
        gp.add(lblName,   0, 0); gp.add(tfName,   1, 0);
        gp.add(lblCourse, 0, 1); gp.add(cbCourse, 1, 1);
        gp.add(lblNotes,  0, 2); gp.add(taNotes,  1, 2);

        HBox buttons = new HBox(10, btnSave, btnCancel);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.getStyleClass().add("toolbar");
        gp.add(buttons, 1, 3);

        // Field growth
        GridPane.setFillWidth(tfName, true);
        tfName.setMaxWidth(Double.MAX_VALUE);
        cbCourse.setMaxWidth(Double.MAX_VALUE);
        taNotes.setMaxWidth(Double.MAX_VALUE);

        final Workout[] result = new Workout[1];

        btnSave.setOnAction(e -> {
            String name = tfName.getText().trim();
            if (name.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Workout name is required.").showAndWait();
                return;
            }
            // Create or update in place
            if (initial == null) {
                Workout w = new Workout(swimmerId, name, cbCourse.getValue());
                String notes = taNotes.getText();
                if (notes != null && !notes.isBlank()) w.setNotes(notes);
                result[0] = w;
            } else {
                initial.setName(name);
                initial.setCourse(cbCourse.getValue());
                String notes = taNotes.getText();
                initial.setNotes((notes == null || notes.isBlank()) ? "" : notes);
                result[0] = initial;
            }
            dialog.close();
        });

        btnCancel.setOnAction(e -> { result[0] = null; dialog.close(); });

        Scene scene = new Scene(gp, 520, 360);
        Theme.apply(scene, WorkoutFormDialog.class);
        dialog.setScene(scene);

        dialog.setResizable(false);
        dialog.showAndWait();
        return result[0];
    }
}