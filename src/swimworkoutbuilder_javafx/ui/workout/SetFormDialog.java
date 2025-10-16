package swimworkoutbuilder_javafx.ui.workout;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import swimworkoutbuilder_javafx.model.SwimSet;
import swimworkoutbuilder_javafx.model.enums.Course;
import swimworkoutbuilder_javafx.model.enums.Effort;
import swimworkoutbuilder_javafx.model.enums.StrokeType;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.model.units.TimeSpan;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.ui.Theme;
import java.util.EnumSet;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.model.enums.Equipment;
import swimworkoutbuilder_javafx.model.pacing.DefaultPacePolicy;
import swimworkoutbuilder_javafx.model.pacing.PacePolicy;

import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

/**
 * Modal dialog for creating or editing a single SwimSet.
 * Pure view: returns a SwimSet or empty if cancelled.
 */
public final class SetFormDialog {

    // Pace policy used for goal/interval suggestions
    private static final swimworkoutbuilder_javafx.model.pacing.PacePolicy POLICY =
            new swimworkoutbuilder_javafx.model.pacing.DefaultPacePolicy();

    private SetFormDialog() {}

    /** Show the dialog (ownerless). Pass existing to edit, or null to add. */
    public static Optional<SwimSet> show(SwimSet existing) {
        Stage dialog = new Stage();

        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "Add Set" : "Edit Set");

        // ----------------------------
        // Row 1: REPS × DISTANCE (centered, large)
        // ----------------------------
        Spinner<Integer> spReps = new Spinner<>(1, 999, 1);
        spReps.setEditable(true);
        spReps.getEditor().setPrefColumnCount(3);

        Label xLabel = new Label("×");
        xLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 700;");

        // set increment to pool length set in workout header
        Course courseForDialog = resolveCourse();
        int lapLen = (courseForDialog == Course.LCM) ? 50 : 25; // if it's not LCM, it's 25
        SpinnerValueFactory.IntegerSpinnerValueFactory distFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(25, 9999, 100, lapLen);

        Spinner<Integer> spDist = new Spinner<>(distFactory);
        spDist.setEditable(true);
        spDist.getEditor().setPrefColumnCount(5);

        spReps.setStyle("-fx-font-size: 18px;");
        spDist.setStyle("-fx-font-size: 18px;");

        HBox repsDistance = new HBox(10, spReps, xLabel, spDist);
        repsDistance.setAlignment(Pos.CENTER);

        Label repsLbl = new Label("REPS");
        Label distLbl = new Label("DISTANCE");
        repsLbl.getStyleClass().add("section-label");
        distLbl.getStyleClass().add("section-label");

        // labels under each spinner, still centered as a unit
        HBox rdLabels = new HBox(60, repsLbl, distLbl);
        rdLabels.setAlignment(Pos.CENTER);

        VBox row1 = new VBox(6, repsDistance, rdLabels);
        row1.setAlignment(Pos.CENTER);

        // ----------------------------
        // STROKE (4 + 3, indented)
        // ----------------------------
        ToggleGroup tgStroke = new ToggleGroup();
        RadioButton rbFree  = mkStroke(tgStroke, StrokeType.FREESTYLE);
        RadioButton rbBack  = mkStroke(tgStroke, StrokeType.BACKSTROKE);
        RadioButton rbBreast= mkStroke(tgStroke, StrokeType.BREASTSTROKE);
        RadioButton rbFly   = mkStroke(tgStroke, StrokeType.BUTTERFLY);
        RadioButton rbIM    = mkStroke(tgStroke, StrokeType.INDIVIDUAL_MEDLEY);
        RadioButton rbKick  = mkStroke(tgStroke, StrokeType.KICK);
        RadioButton rbDrill = mkStroke(tgStroke, StrokeType.DRILL);

        HBox strokeRow1 = new HBox(16, rbFree, rbBack, rbBreast, rbFly);
        HBox strokeRow2 = new HBox(16, rbIM, rbKick, rbDrill);
        strokeRow1.setAlignment(Pos.CENTER_LEFT);
        strokeRow2.setAlignment(Pos.CENTER_LEFT);
        strokeRow1.setPadding(new Insets(0,0,0,24));
        strokeRow2.setPadding(new Insets(0,0,0,24));

        Label strokeLabel = new Label("STROKE");
        strokeLabel.getStyleClass().add("section-label");

        VBox strokeBox = new VBox(6, strokeLabel, strokeRow1, strokeRow2);

        // ----------------------------
        // EFFORT LEVEL (slider)
        // ----------------------------
        Label effortLabel = new Label("EFFORT LEVEL");
        effortLabel.getStyleClass().add("section-label");

        Slider effortSlider = new Slider(0, Effort.values().length - 1, 0);
        effortSlider.setMajorTickUnit(1);
        effortSlider.setMinorTickCount(0);
        effortSlider.setSnapToTicks(true);
        effortSlider.setShowTickMarks(true);
        effortSlider.setShowTickLabels(true);
        effortSlider.setLabelFormatter(new StringConverterEffort());

        VBox effortBox = new VBox(6, effortLabel, effortSlider);

        // ----------------------------
        // Interval & Goal (two rows, compact fields)
        // ----------------------------
        TextField tfInterval = new TextField();
        tfInterval.setPromptText("e.g., 1:30");
        tfInterval.setPrefColumnCount(8);

        TextField tfGoal = new TextField();
        tfGoal.setPromptText("e.g., 1:20");
        tfGoal.setPrefColumnCount(8);

        Label intervalTitle = new Label("INTERVAL TIME");
        intervalTitle.getStyleClass().add("section-label");
        Label intervalSub = new Label("(swim + rest)");
        intervalSub.getStyleClass().add("section-sub");
        Label goalLbl     = new Label("GOAL TIME");
        goalLbl.getStyleClass().add("section-label");
        VBox intervalBox = new VBox(0, intervalTitle, intervalSub);

        GridPane timeGrid = new GridPane();
        timeGrid.setHgap(12);
        timeGrid.setVgap(8);
        ColumnConstraints c0 = new ColumnConstraints(); // interval label box
        ColumnConstraints c1 = new ColumnConstraints(); // interval field
        ColumnConstraints c2 = new ColumnConstraints(); // goal label
        ColumnConstraints c3 = new ColumnConstraints(); // goal field
        c1.setHgrow(Priority.NEVER);
        c3.setHgrow(Priority.NEVER);
        timeGrid.getColumnConstraints().addAll(c0, c1, c2, c3);

        timeGrid.add(intervalBox, 0, 0);
        timeGrid.add(tfInterval, 1, 0);
        timeGrid.add(goalLbl, 2, 0);
        timeGrid.add(tfGoal, 3, 0);


        // ----------------------------
        // EQUIPMENT (icons w/ tooltips; visual only)
        // ----------------------------
        Label equipLabel = new Label("EQUIPMENT");
        equipLabel.getStyleClass().add("section-label");

        CheckBox cbPaddles   = iconCheck("paddles.png", 48, "Paddles");
        CheckBox cbPullBuoy  = iconCheck("pull_buoy.png", 48, "Pull buoy");
        CheckBox cbSnorkel   = iconCheck("snorkel.png", 48, "Snorkel");
        CheckBox cbKickboard = iconCheck("kick_board.png", 48, "Kick board");
        CheckBox cbDragSocks = iconCheck("Drag_Socks.png", 48, "Drag socks");
        CheckBox cbParachute = iconCheck("parachute.png", 48, "Parachute");
        CheckBox cbFins      = iconCheck("fins.png", 48, "Fins");

        FlowPane equipPane = new FlowPane(12, 10,
                cbFins, cbKickboard, cbPullBuoy, cbPaddles, cbSnorkel, cbParachute, cbDragSocks);
        equipPane.setAlignment(Pos.CENTER_LEFT);
        equipPane.setPrefWrapLength(480);

        VBox equipBox = new VBox(6, equipLabel, equipPane);
        equipBox.setAlignment(Pos.CENTER_LEFT);
        equipPane.setPadding(new Insets(0,0,0,24));

        equipBox.setMinHeight(56);
        equipBox.setPrefHeight(56);
        equipBox.setMaxHeight(56);



        // ----------------------------
        // NOTES (multi-line, half-ish width)
        // ----------------------------
        Label notesLabel = new Label("NOTES");
        notesLabel.getStyleClass().add("section-label");
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Notes (optional)");
        notesArea.setWrapText(true);
        notesArea.setPrefRowCount(3);
        notesArea.setPrefHeight(80);
        notesArea.setMinHeight(80);
        notesArea.setMaxHeight(80);

        VBox notesBox = new VBox(6, notesLabel, notesArea);

        // ----------------------------
        // Prefill defaults / existing
        // ----------------------------
        if (existing != null) {
            spReps.getValueFactory().setValue(Math.max(1, existing.getReps()));
            int dist = (isMeters(existing.getCourse()))
                    ? (int) Math.round(existing.getDistancePerRep().toMeters())
                    : (int) Math.round(existing.getDistancePerRep().toYards());
            spDist.getValueFactory().setValue(Math.max(25, dist));
            selectStrokeRadio(tgStroke, existing.getStroke());
            effortSlider.setValue(indexOfEffort(existing.getEffort()));
            if (existing.getGoalTime() != null) tfGoal.setText(formatTime(existing.getGoalTime()));
            if (existing.getNotes() != null) notesArea.setText(existing.getNotes());
        } else {
            // Defaults for new
            spReps.getValueFactory().setValue(1);
            spDist.getValueFactory().setValue(100);
            rbFree.setSelected(true);
            effortSlider.setValue(indexOfEffort(Effort.EASY));
        }

        // ----------------------------
        // Buttons
        // ----------------------------
        ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType btnAdd    = new ButtonType(existing == null ? "Add" : "Save", ButtonBar.ButtonData.OK_DONE);

        DialogPane pane = new DialogPane();
        VBox content = new VBox(14, row1, new Separator(), strokeBox, effortBox, timeGrid, equipBox, notesBox);
        content.getStyleClass().add("set-dialog");
        content.setPrefHeight(520);
        content.setMinHeight(520);
        content.setPadding(new Insets(14));
        content.setFillWidth(true);
        pane.setContent(content);
        pane.getButtonTypes().addAll(btnCancel, btnAdd);

        // Style the actual Button nodes (fix dim appearance)
        Button okBtn = (Button) pane.lookupButton(btnAdd);
        okBtn.getStyleClass().setAll("button", "primary");
        okBtn.setDefaultButton(true);
        okBtn.setDisable(false);

        Button cancelBtn = (Button) pane.lookupButton(btnCancel);
        cancelBtn.getStyleClass().setAll("button", "ghost");
        cancelBtn.setCancelButton(true);
        cancelBtn.setDisable(false);

        pane.setMinHeight(Region.USE_PREF_SIZE);


        // --- Policy + “suggested” interval/goal live recompute -------------------

        final Workout workout = AppState.get().getCurrentWorkout();
        final Swimmer swimmer = AppState.get().getCurrentSwimmer();

        // Build equipment set from the seven icon checkboxes
        java.util.function.Supplier<java.util.Set<Equipment>> eqSupplier = () -> {
            EnumSet<Equipment> eq = EnumSet.noneOf(Equipment.class);
            if (cbFins.isSelected())      eq.add(Equipment.FINS);
            if (cbKickboard.isSelected()) eq.add(Equipment.PULL_BUOY);   // If you model Kick as PULL_BUOY keep; else change
            if (cbPullBuoy.isSelected())  eq.add(Equipment.PULL_BUOY);
            if (cbPaddles.isSelected())   eq.add(Equipment.PADDLES);
            if (cbSnorkel.isSelected())   eq.add(Equipment.SNORKEL);
            if (cbParachute.isSelected()) eq.add(Equipment.PARACHUTE);
            if (cbDragSocks.isSelected()) eq.add(Equipment.DRAG_SOCKS);
            return eq;
        };

        Runnable recompute = () -> {
            try {
                if (workout == null || swimmer == null) return;

                int reps = spReps.getValue();
                int distance = spDist.getValue();

                StrokeType stroke = (StrokeType) tgStroke.getSelectedToggle().getUserData();
                Effort effort = Effort.values()[(int) Math.round(effortSlider.getValue())];
                Course course = resolveCourse();

                // Build a TEMP SwimSet that mirrors the form state (so policy sees the real inputs)
                Distance distObj = (course == Course.SCY) ? Distance.ofYards(distance) : Distance.ofMeters(distance);
                SwimSet temp = new SwimSet(stroke, Math.max(1, reps), distObj, effort, course, ""); // notes ignored for math
                temp.setEquipment(eqSupplier.get()); // assumes SwimSet has setEquipment(Set<Equipment>)

                // Use YOUR pacing policy
                int goalSec     = (int) Math.round(POLICY.goalSeconds(workout, temp, swimmer, /*repIndex*/ 0));
                int intervalSec = POLICY.intervalSeconds(workout, temp, swimmer, /*repIndex*/ 0);

                tfGoal.setText(TimeSpan.ofSeconds(goalSec).toString());
                tfInterval.setText(TimeSpan.ofSeconds(intervalSec).toString());
            } catch (Throwable ignored) { }
        };

        // Hook recompute to all inputs that affect pace
        spReps.valueProperty().addListener((o,a,b) -> recompute.run());
        spDist.valueProperty().addListener((o,a,b) -> recompute.run());
        tgStroke.selectedToggleProperty().addListener((o,a,b) -> recompute.run());
        effortSlider.valueProperty().addListener((o,a,b) -> recompute.run());
        cbFins.selectedProperty().addListener((o,a,b) -> recompute.run());
        cbKickboard.selectedProperty().addListener((o,a,b) -> recompute.run());
        cbPullBuoy.selectedProperty().addListener((o,a,b) -> recompute.run());
        cbPaddles.selectedProperty().addListener((o,a,b) -> recompute.run());
        cbSnorkel.selectedProperty().addListener((o,a,b) -> recompute.run());
        cbParachute.selectedProperty().addListener((o,a,b) -> recompute.run());
        cbDragSocks.selectedProperty().addListener((o,a,b) -> recompute.run());

        // Prime once after defaults/prefill are set
        recompute.run();

        Scene scene = new Scene(pane);                      // no fixed W×H
        Theme.apply(scene, SetFormDialog.class);
        dialog.setScene(scene);

        // let content and dialog rely on preferred sizes
        content.setMinWidth(Region.USE_PREF_SIZE);
        content.setMinHeight(Region.USE_PREF_SIZE);
        pane.setMinHeight(Region.USE_PREF_SIZE);

        dialog.setResizable(false);

        // do the sizing *after* CSS/layout have run to avoid flicker
        dialog.sizeToScene();
        javafx.application.Platform.runLater(dialog::sizeToScene);

        dialog.centerOnScreen();

        final SwimSet[] result = new SwimSet[1];

        // Handle OK
        ((Button) pane.lookupButton(btnAdd)).setOnAction(e -> {
            try {
                int reps = spReps.getValue();
                int distance = spDist.getValue();

                StrokeType stroke = (StrokeType) tgStroke.getSelectedToggle().getUserData();
                Effort effort = Effort.values()[(int) Math.round(effortSlider.getValue())];

                Course course = resolveCourse();
                Distance distObj = isMeters(course)
                        ? Distance.ofMeters(distance)
                        : Distance.ofYards(distance);

                String notes = notesArea.getText() == null ? "" : notesArea.getText().trim();

                SwimSet s = new SwimSet(stroke, reps, distObj, effort, course, notes);

                // Carry equipment selections into the saved set (if your SwimSet supports it)
                s.setEquipment(eqSupplier.get());

                // Parse any user-entered goal first
                String goalTxt = tfGoal.getText();
                TimeSpan goal = parseFlexible(goalTxt);
                if (goal != null) {
                    s.setGoalTime(goal);
                } else if (workout != null && swimmer != null) {
                    // Otherwise, fill from policy suggestion so rows/header stay consistent
                    int goalSec = (int) Math.round(POLICY.goalSeconds(workout, s, swimmer, 0));
                    s.setGoalTime(TimeSpan.ofSeconds(goalSec));
                }

                // (Interval is not wired in model per current code base; keep UI only.)
                result[0] = s;
                dialog.close();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Please check your inputs.").showAndWait();
            }
        });

        ((Button) pane.lookupButton(btnCancel)).setOnAction(e -> {
            result[0] = null;
            dialog.close();
        });

        dialog.showAndWait();
        return Optional.ofNullable(result[0]);
    }

    // ----------------------------
    // Helpers
    // ----------------------------

    private static boolean isMeters(Course c) {
        return c == Course.SCM || c == Course.LCM;
    }

    private static Course resolveCourse() {
        try {
            var w = AppState.get().getCurrentWorkout();
            return (w != null && w.getCourse() != null) ? w.getCourse() : Course.SCY;
        } catch (Throwable t) {
            return Course.SCY;
        }
    }

    private static RadioButton mkStroke(ToggleGroup g, StrokeType st) {
        RadioButton rb = new RadioButton(st.getLabel());
        rb.setUserData(st);
        rb.setToggleGroup(g);
        return rb;
    }

    private static void selectStrokeRadio(ToggleGroup g, StrokeType st) {
        if (st == null) return;
        for (Toggle t : g.getToggles()) {
            if (Objects.equals(t.getUserData(), st)) {
                g.selectToggle(t);
                return;
            }
        }
    }

    /** Image checkbox with tooltip (visual only). */
    private static CheckBox iconCheck(String fileName, int size, String tooltip) {
        ImageView iv = new ImageView(loadImage("/resources/images/" + fileName));
        iv.setFitWidth(40);
        iv.setFitHeight(40);
        iv.setPreserveRatio(true);
        CheckBox cb = new CheckBox();
        cb.setGraphic(iv);
        cb.setTooltip(new Tooltip(tooltip));
        cb.getStyleClass().add("equipment-icon");
        return cb;
    }

    private static Image loadImage(String path) {
        try (InputStream is = SetFormDialog.class.getResourceAsStream(path)) {
            if (is != null) return new Image(is);
        } catch (Exception ignored) { }
        return new Image( // tiny fallback 1×1
                "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMB/UmQK9kAAAAASUVORK5CYII=");
    }

    // "m:ss", "m:ss.hh", "ss", "ss.hh"
    private static TimeSpan parseFlexible(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;

        int minutes = 0, seconds, hundredths = 0;
        String work = s;

        if (work.contains(":")) {
            String[] parts = work.split(":");
            if (!parts[0].isBlank()) minutes = safeInt(parts[0], 0);
            work = (parts.length >= 2) ? parts[1] : "";
        }
        if (work.contains(".")) {
            String[] p2 = work.split("\\.");
            seconds = safeInt(p2[0], 0);
            String h = (p2.length >= 2) ? p2[1] : "0";
            if (h.length() == 1) h = h + "0";
            hundredths = safeInt(h.substring(0, Math.min(2, h.length())), 0);
        } else {
            seconds = safeInt(work, 0);
        }
        return TimeSpan.ofMinutesSecondsMillis(minutes, seconds, hundredths * 10);
    }

    private static int safeInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private static String formatTime(TimeSpan t) {
        long total = Math.max(0, Math.round(t.toMillis() / 1000.0));
        long m = total / 60;
        long s = total % 60;
        return String.format("%d:%02d", m, s);
    }

    /**
     * Map Effort enum to a 0-based index used by the slider ticks.
     */
    private static int indexOfEffort(Effort e) {
        if (e == null) return 0;
        Effort[] vals = Effort.values();
        for (int i = 0; i < vals.length; i++) {
            if (vals[i] == e) return i;
        }
        return 0;
    }

    /** Show Effort names along the slider ticks. */
    private static final class StringConverterEffort extends javafx.util.StringConverter<Double> {
        @Override public String toString(Double value) {
            int idx = (int)Math.round(value == null ? 0 : value);
            Effort[] vals = Effort.values();
            if (idx < 0 || idx >= vals.length) idx = 0;
            return vals[idx].name();
        }
        @Override public Double fromString(String string) { return 0d; }
    }
}