package swimworkoutbuilder_javafx.ui.dialogs;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import swimworkoutbuilder_javafx.model.SwimSet;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.model.enums.*;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.model.units.TimeSpan;
import swimworkoutbuilder_javafx.model.pacing.DefaultPacePolicy;
import swimworkoutbuilder_javafx.model.pacing.PacePolicy;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.state.AppState;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public final class SetFormDialog {

    private SetFormDialog() {}

    /**
     * @param workout  used for course (SCY=yards, SCM/LCM=meters)
     * @param existing null to create, non-null to edit
     */
    public static SwimSet show(Workout workout, SwimSet existing) {
        Dialog<SwimSet> swimSetDialog = new Dialog<>();
        swimSetDialog.initStyle(StageStyle.UTILITY);
        swimSetDialog.initModality(Modality.APPLICATION_MODAL);
        swimSetDialog.setTitle(existing == null ? "Add Set" : "Edit Set");

        ButtonType BT_SAVE = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        swimSetDialog.getDialogPane().getButtonTypes().addAll(BT_SAVE, ButtonType.CANCEL);

        // --- Controls ---
        ComboBox<StrokeType> cbStroke = new ComboBox<>();
        cbStroke.getItems().addAll(StrokeType.values());
        cbStroke.setPrefWidth(220);

        ComboBox<Effort> cbEffort = new ComboBox<>();
        cbEffort.getItems().addAll(Effort.values());
        cbEffort.setPrefWidth(220);

        Spinner<Integer> spReps = new Spinner<>(1, 999, 1);
        spReps.setEditable(true);

        // Distance spinner (lap = pool length)
        Course course = (workout != null ? workout.getCourse() : Course.SCY);
        Distance poolLen = course.getLength();

        // Determine lap size and units for display
        boolean isYards = (course.getUnit() == CourseUnit.YARDS);
        int lap = (int) Math.round(isYards ? poolLen.toYards() : poolLen.toMeters());
        int max = isYards ? 2000 : 2000; // allow for long distances (2000y/m)
        int initial = 100;

        // Spinner shows *only the numeric part*
        Spinner<Integer> spDistance = new Spinner<>(lap, max, initial, lap);
        spDistance.setEditable(true);
        spDistance.getEditor().setPrefColumnCount(5);

        // Only allow digits in the editor
        spDistance.getEditor().setTextFormatter(new TextFormatter<String>(change -> {
            String s = change.getControlNewText();
            return s.matches("\\d*") ? change : null;
        }));

        // Optional: label to show unit beside the spinner
        Label lblUnit = new Label(isYards ? "yd" : "m");
        lblUnit.setStyle("-fx-opacity: 0.7;");

        HBox distanceBox = new HBox(6, spDistance, lblUnit);
        distanceBox.setAlignment(Pos.CENTER_LEFT);

        // Calculated fields (read-only)
        TextField tfInterval = new TextField();
        tfInterval.setEditable(false);
        tfInterval.setPromptText("Interval @ (auto)");

        TextField tfGoal = new TextField();
        tfGoal.setEditable(false);
        tfGoal.setPromptText("Goal time (auto)");

        // equipment (multi-select via checkboxes)
        List<CheckBox> eqChecks = new ArrayList<>();
        for (Equipment eq : Equipment.values()) {
            CheckBox cb = new CheckBox(eq.getLabel());
            cb.setUserData(eq);
            eqChecks.add(cb);
        }

        TextField tfNotes = new TextField();
        tfNotes.setPromptText("Notes (≤100 chars)");
        tfNotes.textProperty().addListener((obs, o, n) -> {
            if (n != null && n.length() > 100) tfNotes.setText(n.substring(0, 100));
        });



        // --- Layout ---
        GridPane gp = new GridPane();
        gp.setHgap(8);
        gp.setVgap(8);
        gp.setPadding(new Insets(12));

        int r = 0;
        gp.add(new Label("Stroke:"), 0, r); gp.add(cbStroke, 1, r++);
        gp.add(new Label("Effort:"), 0, r); gp.add(cbEffort, 1, r++);
        gp.add(new Label("Reps:"), 0, r); gp.add(spReps, 1, r++);
        gp.add(new Label("Distance per rep:"), 0, r); gp.add(distanceBox, 1, r++);
        gp.add(new Label("Interval (@):"), 0, r); gp.add(tfInterval, 1, r++);
        gp.add(new Label("Goal time:"),    0, r); gp.add(tfGoal,     1, r++);

        gp.add(new Label("Equipment:"), 0, r);
        GridPane eqGrid = new GridPane();
        eqGrid.setHgap(10); eqGrid.setVgap(4);
        int er = 0, ec = 0;
        for (CheckBox cb : eqChecks) {
            eqGrid.add(cb, ec, er);
            ec++;
            if (ec == 3) { ec = 0; er++; }
        }
        gp.add(eqGrid, 1, r++);

        gp.add(new Label("Notes:"), 0, r); gp.add(tfNotes, 1, r++);
        swimSetDialog.getDialogPane().setContent(gp);

        // --- Live calculation wiring ---
        Runnable recalc = () -> {
            SwimSet tmp = new SwimSet();
            tmp.setStroke(cbStroke.getValue());
            tmp.setEffort(cbEffort.getValue());
            tmp.setReps(safeInt(spReps.getValue(), 1));

            int val = safeInt(spDistance.getValue(), lap);
            Distance dpr = isYards ? Distance.ofYards(val) : Distance.ofMeters(val);
            tmp.setDistancePerRep(dpr);

            // equipment
            EnumSet<Equipment> sel = EnumSet.noneOf(Equipment.class);
            for (CheckBox cb : eqChecks) if (cb.isSelected()) sel.add((Equipment) cb.getUserData());
            tmp.setEquipment(sel);

            // compute using current swimmer + policy
            Swimmer swimmer = AppState.get().getCurrentSwimmer();
            PacePolicy policy = new DefaultPacePolicy();

            if (swimmer == null || tmp.getStroke() == null) {
                tfInterval.setText("");
                tfGoal.setText("");
                return;
            }

            // We compute for rep #1 (policy can vary by rep if needed)
            double goalSec = policy.goalSeconds(workout, tmp, swimmer, 1);
            int intervalSec = policy.intervalSeconds(workout, tmp, swimmer, 1);

            tfGoal.setText(mmss(goalSec));
            tfInterval.setText(mmss(intervalSec));
        };
        // Distance spinner: snap/clamp on value change
        spDistance.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            var vf = (SpinnerValueFactory.IntegerSpinnerValueFactory) spDistance.getValueFactory();
            int minAllowed = vf.getMin();
            int maxAllowed = vf.getMax();

            int current = Math.min(Math.max(newVal, minAllowed), maxAllowed);
            int snapped = Math.max(lap, (int) Math.ceil(current / (double) lap) * lap);

            if (snapped != newVal) {
                vf.setValue(snapped);
            }
            recalc.run(); // now visible
        });

        // Also snap/clamp on focus loss from the editor
        spDistance.getEditor().focusedProperty().addListener((obs, was, isNow) -> {
            if (!isNow) {
                var vf = (SpinnerValueFactory.IntegerSpinnerValueFactory) spDistance.getValueFactory();
                int minAllowed = vf.getMin();
                int maxAllowed = vf.getMax();

                int typed = parseIntSafe(spDistance.getEditor().getText(), spDistance.getValue());
                typed = Math.min(Math.max(typed, minAllowed), maxAllowed);

                int snapped = Math.max(lap, (int) Math.ceil(typed / (double) lap) * lap);
                if (snapped != spDistance.getValue()) {
                    vf.setValue(snapped);
                }
                recalc.run(); // now visible
            }
        });
        // --- Prefill if editing ---
        if (existing != null) {
            if (existing.getEquipment() != null) {
                for (CheckBox cb : eqChecks) {
                    Equipment eq = (Equipment) cb.getUserData();
                    cb.setSelected(existing.getEquipment().contains(eq));
                }
            }
            if (existing.getStroke() != null) cbStroke.setValue(existing.getStroke());
            if (existing.getEffort() != null) cbEffort.setValue(existing.getEffort());
            spReps.getValueFactory().setValue(Math.max(1, existing.getReps()));

            if (existing.getDistancePerRep() != null) {
                int displayVal = isYards
                        ? (int) Math.round(existing.getDistancePerRep().toYards())
                        : (int) Math.round(existing.getDistancePerRep().toMeters());
                int snapped = Math.max(lap, Math.round(displayVal / (float) lap) * lap);
                spDistance.getValueFactory().setValue(Math.max(lap, snapped));
            }
            tfNotes.setText(existing.getNotes() == null ? "" : existing.getNotes());
            // interval/goal are auto; we’ll recompute via listeners below
        } else {
            cbEffort.setValue(Effort.EASY);
        }
        recalc.run();

        // trigger on any input change
        cbStroke.valueProperty().addListener((o, a, b) -> recalc.run());
        cbEffort.valueProperty().addListener((o, a, b) -> recalc.run());
        spReps.valueProperty().addListener((o, a, b) -> recalc.run());

        for (CheckBox cb : eqChecks) cb.selectedProperty().addListener((o, a, b) -> recalc.run());

        // initial calc
        recalc.run();

        // --- Result ---
        swimSetDialog.setResultConverter(bt -> {
            if (bt != BT_SAVE) return null;

            SwimSet out = existing == null ? new SwimSet() : existing;

            out.setStroke(cbStroke.getValue());
            out.setEffort(cbEffort.getValue());
            out.setReps(safeInt(spReps.getValue(), 1));
            Distance dpr = isYards
                    ? Distance.ofYards(safeInt(spDistance.getValue(), lap))
                    : Distance.ofMeters(safeInt(spDistance.getValue(), lap));
            out.setDistancePerRep(dpr);

            EnumSet<Equipment> sel = EnumSet.noneOf(Equipment.class);
            for (CheckBox cb : eqChecks) if (cb.isSelected()) sel.add((Equipment) cb.getUserData());
            out.setEquipment(sel);

            out.setNotes(tfNotes.getText());

            // commit calculated times
            TimeSpan interval = parseMmSs(tfInterval.getText());
            TimeSpan goal = parseMmSs(tfGoal.getText());
            out.setInterval(interval);
            out.setGoalTime(goal);

            return out;
        });

        return swimSetDialog.showAndWait().orElse(null);
    }

    // ---------- helpers ----------

    private static int safeInt(Integer v, int min) {
        if (v == null) return min;
        return Math.max(min, v);
    }

    private static String mmss(double seconds) {
        long s = Math.max(0, Math.round(seconds));
        return String.format("%d:%02d", s / 60, s % 60);
    }

    private static TimeSpan parseMmSs(String s) {
        if (s == null || s.isBlank()) return null;
        String[] parts = s.split(":");
        int m = (parts.length > 0) ? parseInt(parts[0]) : 0;
        int sec = (parts.length > 1) ? parseInt(parts[1]) : 0;
        return TimeSpan.ofMinutesSecondsMillis(m, sec, 0);
    }

    private static Integer parseIntSafe(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return fallback; }
    }

    private static int parseInt(String v) {
        try { return Integer.parseInt(v.trim()); } catch (Exception e) { return 0; }
    }
}