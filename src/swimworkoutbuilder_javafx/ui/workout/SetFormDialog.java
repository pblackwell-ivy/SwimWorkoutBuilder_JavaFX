package swimworkoutbuilder_javafx.ui.workout;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.model.SwimSet;
import swimworkoutbuilder_javafx.model.enums.*;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.model.pacing.DefaultPacePolicy;
import swimworkoutbuilder_javafx.model.pacing.PacePolicy;
import swimworkoutbuilder_javafx.ui.common.DialogUtil;

import java.util.EnumSet;

public final class SetFormDialog {
    private SetFormDialog() { }

    /** Redesigned dialog: compact multi-row layout with live interval/goal. */
    public static SwimSet show(Workout workout, SwimSet existing) {
        Stage dialog = new Stage();

        // Row 1: reps × distance
        Spinner<Integer> spReps = new Spinner<>(1, 999, 1);
        spReps.setEditable(true);
        spReps.setPrefWidth(90);

        Course course = workout != null ? workout.getCourse() : Course.SCY;
        boolean isYards = (course == Course.SCY);
        boolean isMeters = (course == Course.SCM || course == Course.LCM);
        int lap = (int)Math.round(isYards ? course.getLength().toYards() : course.getLength().toMeters());

        Spinner<Integer> spDist = new Spinner<>(lap, 2000, 100, lap);
        spDist.setEditable(true);
        spDist.setPrefWidth(110);

        HBox row1 = new HBox(8,
                new Label("Reps:"), spReps,
                new Label("×"),
                new Label("Distance/rep:"), spDist, new Label(isYards ? "yd" : "m"));
        row1.setAlignment(Pos.CENTER_LEFT);

        // Row 2: stroke radios
        ToggleGroup strokeGroup = new ToggleGroup();
        FlowPane strokeFlow = new FlowPane(8, 6);
        for (StrokeType st : StrokeType.values()) {
            RadioButton rb = new RadioButton(st.getLabel());
            rb.setUserData(st);
            rb.setToggleGroup(strokeGroup);
            strokeFlow.getChildren().add(rb);
        }
        VBox row2 = new VBox(new Label("Stroke:"), strokeFlow);

        // Row 3: interval + goal (read-only)
        TextField tfInterval = new TextField(); tfInterval.setEditable(false);
        TextField tfGoal     = new TextField(); tfGoal.setEditable(false);
        HBox row3 = new HBox(8, new Label("Interval (@):"), tfInterval, new Label("Goal:"), tfGoal);
        row3.setAlignment(Pos.CENTER_LEFT);

        // Row 4: effort radios
        ToggleGroup effortGroup = new ToggleGroup();
        HBox effortRow = new HBox(10);
        for (Effort e : Effort.values()) {
            RadioButton rb = new RadioButton(e.name());
            rb.setUserData(e);
            rb.setToggleGroup(effortGroup);
            effortRow.getChildren().add(rb);
        }
        VBox row4 = new VBox(new Label("Effort:"), effortRow);

        // Row 5: equipment toggle chips
        FlowPane eqFlow = new FlowPane(8, 6);
        EnumSet<Equipment> eqSelected = EnumSet.noneOf(Equipment.class);
        for (Equipment eq : Equipment.values()) {
            ToggleButton b = new ToggleButton(eq.getLabel());
            b.getStyleClass().addAll("button","ghost","sm");
            b.setUserData(eq);
            b.setOnAction(ev -> {
                Equipment e = (Equipment) b.getUserData();
                if (b.isSelected()) eqSelected.add(e); else eqSelected.remove(e);
            });
            eqFlow.getChildren().add(b);
        }
        VBox row5 = new VBox(new Label("Equipment:"), eqFlow);

        // Prefill on edit
        if (existing != null) {
            spReps.getValueFactory().setValue(Math.max(1, existing.getReps()));
            int disp = isYards ? (int) Math.round(existing.getDistancePerRep().toYards())
                               : (int) Math.round(existing.getDistancePerRep().toMeters());
            spDist.getValueFactory().setValue(disp);
            if (existing.getStroke() != null) {
                for (var n : strokeFlow.getChildren()) {
                    RadioButton rb = (RadioButton)n;
                    if (rb.getUserData() == existing.getStroke()) { rb.setSelected(true); break; }
                }
            }
            if (existing.getEffort() != null) {
                for (var n : effortRow.getChildren()) {
                    RadioButton rb = (RadioButton)n;
                    if (rb.getUserData() == existing.getEffort()) { rb.setSelected(true); break; }
                }
            }
            if (existing.getEquipment() != null) {
                for (var n : eqFlow.getChildren()) {
                    ToggleButton b = (ToggleButton)n;
                    if (existing.getEquipment().contains((Equipment)b.getUserData())) b.setSelected(true);
                }
                eqSelected.addAll(existing.getEquipment());
            }
        } else {
            spReps.getValueFactory().setValue(1);
            spDist.getValueFactory().setValue(100);
            for (var n : strokeFlow.getChildren()) { // default Free
                RadioButton rb = (RadioButton)n;
                if (rb.getText().toLowerCase().contains("free")) { rb.setSelected(true); break; }
            }
            for (var n : effortRow.getChildren()) {
                RadioButton rb = (RadioButton)n;
                if (rb.getText().equalsIgnoreCase("EASY")) { rb.setSelected(true); break; }
            }
        }

        // live calculation
        Runnable recalc = () -> {
            SwimSet tmp = new SwimSet();
            tmp.setReps(spReps.getValue());
            int val = spDist.getValue();
            Distance dpr = isYards ? Distance.ofYards(val) : Distance.ofMeters(val);
            tmp.setDistancePerRep(dpr);

            var stToggle = strokeGroup.getSelectedToggle();
            var efToggle = effortGroup.getSelectedToggle();
            if (stToggle != null) tmp.setStroke((StrokeType)((RadioButton)stToggle).getUserData());
            if (efToggle != null) tmp.setEffort((Effort)((RadioButton)efToggle).getUserData());
            tmp.setEquipment(eqSelected);

            var swimmer = AppState.get().getCurrentSwimmer();
            PacePolicy policy = new DefaultPacePolicy();

            if (swimmer == null || tmp.getStroke() == null) { 
                tfInterval.setText(""); tfGoal.setText(""); return; 
            }

            double goalSec = policy.goalSeconds(workout, tmp, swimmer, 1);
            int intervalSec = policy.intervalSeconds(workout, tmp, swimmer, 1);
            tfGoal.setText(mmss(goalSec));
            tfInterval.setText(mmss(intervalSec));
        };

        spReps.valueProperty().addListener((o,a,b)->recalc.run());
        spDist.valueProperty().addListener((o,a,b)->recalc.run());
        strokeGroup.selectedToggleProperty().addListener((o,a,b)->recalc.run());
        effortGroup.selectedToggleProperty().addListener((o,a,b)->recalc.run());
        for (var n : eqFlow.getChildren()) ((ToggleButton)n).selectedProperty().addListener((o,a,b)->recalc.run());

        Button btnSave = new Button("Save");
        Button btnCancel = new Button("Cancel");
        btnSave.getStyleClass().addAll("button","primary");
        btnCancel.getStyleClass().addAll("button","secondary");
        btnSave.setDefaultButton(true);
        btnCancel.setCancelButton(true);

        final SwimSet[] out = new SwimSet[1];
        btnSave.setOnAction(e -> {
            SwimSet result = (existing == null ? new SwimSet() : existing);
            result.setReps(spReps.getValue());
            int val = spDist.getValue();
            Distance dpr = isYards ? Distance.ofYards(val) : Distance.ofMeters(val);
            result.setDistancePerRep(dpr);
            var stToggle = strokeGroup.getSelectedToggle();
            var efToggle = effortGroup.getSelectedToggle();
            if (stToggle != null) result.setStroke((StrokeType)((RadioButton)stToggle).getUserData());
            if (efToggle != null) result.setEffort((Effort)((RadioButton)efToggle).getUserData());
            result.setEquipment(eqSelected);
            result.setInterval(parseMmSs(tfInterval.getText()));
            result.setGoalTime(parseMmSs(tfGoal.getText()));
            out[0] = result;
            dialog.close();
        });
        btnCancel.setOnAction(e -> { out[0] = null; dialog.close(); });

        VBox content = new VBox(10,
                row1,
                row2,
                row3,
                row4,
                row5,
                new HBox(10, btnCancel, btnSave)
        );
        content.setPadding(new Insets(12));
        content.getStyleClass().add("form-grid");

        Scene scene = new Scene(content, 560, 440);
        DialogUtil.prime(dialog, scene, null, 560, 440, (existing==null? "Add Set" : "Edit Set"));
        dialog.showAndWait();
        return out[0];
    }

    private static String mmss(double seconds) {
        long s = Math.max(0, Math.round(seconds));
        return String.format("%d:%02d", s / 60, s % 60);
    }
    private static swimworkoutbuilder_javafx.model.units.TimeSpan parseMmSs(String s) {
        if (s == null || s.isBlank()) return null;
        String[] p = s.split(":");
        int m = (p.length>0? safeInt(p[0]) : 0);
        int sec = (p.length>1? safeInt(p[1]) : 0);
        return swimworkoutbuilder_javafx.model.units.TimeSpan.ofMinutesSecondsMillis(m, sec, 0);
    }
    private static int safeInt(String v) {
        try { return Integer.parseInt(v.trim()); } catch (Exception e) { return 0; }
    }
}
