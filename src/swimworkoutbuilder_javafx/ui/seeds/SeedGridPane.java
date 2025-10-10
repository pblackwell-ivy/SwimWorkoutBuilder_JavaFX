package swimworkoutbuilder_javafx.ui.seeds;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.model.enums.Course;
import swimworkoutbuilder_javafx.model.enums.StrokeType;
import swimworkoutbuilder_javafx.model.pacing.SeedPace;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.model.units.TimeSpan;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.store.LocalStore;


import java.util.EnumMap;
import java.util.Map;

public final class SeedGridPane extends BorderPane {

    private enum Unit { YD, M }

    private static final double YARD_TO_METER = 0.9144;
    private static final double METER_TO_YARD = 1.0 / 0.9144;

    private final GridPane grid = new GridPane();
    private final Label title = new Label("Seed Times");

    private final ToggleGroup unitGroup = new ToggleGroup();
    private final RadioButton rbYd = new RadioButton("yd");
    private final RadioButton rbM  = new RadioButton("m");
    private final ObjectProperty<Unit> displayUnit = new SimpleObjectProperty<>(Unit.YD);

    private final Button btnEdit = new Button("Edit");
    private final Button btnSave = new Button("Save");
    private final Button btnCancel = new Button("Cancel");

    private final Map<StrokeType, TextField> fields = new EnumMap<>(StrokeType.class);

    private final SeedTimesPresenter presenter = new SeedTimesPresenter(AppState.get());
    private final BooleanProperty hasSwimmer = new SimpleBooleanProperty(false);

    private Swimmer boundSwimmer;
    private Runnable onSeedsSaved;

    public SeedGridPane() {
        buildUI();
        initUnitsFromWorkout();
        wireState();
        setEditable(false);
    }

    public void bindSwimmer(Swimmer s) {
        boundSwimmer = s;
        hasSwimmer.set(s != null);
        loadFromSwimmer();
    }

    public void setOnSeedsSaved(Runnable r) { this.onSeedsSaved = r; }

    private void buildUI() {
        setPadding(new Insets(8));

        // Title
        title.getStyleClass().add("section-title");

        // Units toggle (display-only)
        rbYd.setToggleGroup(unitGroup);
        rbM.setToggleGroup(unitGroup);
        rbYd.setFocusTraversable(false);
        rbM.setFocusTraversable(false);

        HBox unitBox = new HBox(6, new Label("Units:"), rbYd, rbM);
        unitBox.setAlignment(Pos.CENTER_LEFT);

        // Grid
        grid.setHgap(10);
        grid.setVgap(6);
        grid.setPadding(new Insets(6));

        // Grid header row
        Label colStroke = new Label("Stroke");
        Label colTime = new Label();
        colTime.textProperty().bind(Bindings.createStringBinding(
                () -> "100 " + (displayUnit.get() == Unit.YD ? "yd" : "m") + " seed", displayUnit));
        colStroke.getStyleClass().add("label-column-header");
        colTime.getStyleClass().add("label-column-header");

        grid.add(colStroke, 0, 0);
        grid.add(colTime, 1, 0);
        GridPane.setHalignment(colStroke, HPos.LEFT);
        GridPane.setHalignment(colTime, HPos.CENTER);

        // Add each stroke starting at row = 1
        int row = 1;
        for (StrokeType st : StrokeType.values()) {
            addRow(row++, st);
        }
        // Column sizing is compact as possible
        ColumnConstraints c0 = new ColumnConstraints(); // label
        ColumnConstraints c1 = new ColumnConstraints(); // input
        c0.setHgrow(Priority.NEVER);
        c1.setHgrow(Priority.NEVER);
        c1.setHalignment(HPos.CENTER);
        grid.getColumnConstraints().setAll(c0, c1);

        HBox buttonBox = new HBox(8, btnEdit, btnSave, btnCancel);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(6, 0, 0, 0));

        // Root VBox (stack everything vertically)
        VBox root = new VBox(8, buttonBox, title, unitBox, grid);
        root.setFillWidth(false);   // grid keeps its compact width
        root.setAlignment(Pos.TOP_CENTER);
        setCenter(root);

        // initial visual (bindings take over later)
        btnSave.setDisable(true);
        btnCancel.setDisable(true);
    }

    private void addRow(int row, StrokeType stroke) {
        var lbl = new Label(stroke.getLabel());

        var tf = new TextField();
        tf.setPromptText("m:ss.hh");
        tf.getStyleClass().add("seed-time-input");
        tf.setPrefColumnCount(6);
        tf.setMaxWidth(60);
        tf.textProperty().addListener((o, a, b) -> {
            if (presenter.editingProperty().getValue()) presenter.markDirty();
        });


        fields.put(stroke, tf);

        grid.add(lbl, 0, row);
        grid.add(tf, 1, row);
        GridPane.setHalignment(lbl, HPos.LEFT);
    }

    private void initUnitsFromWorkout() {
        Workout w = AppState.get().getCurrentWorkout();
        Course c = (w != null ? w.getCourse() : Course.SCY);
        boolean yards = (c == Course.SCY);
        (yards ? rbYd : rbM).setSelected(true);
        displayUnit.set(yards ? Unit.YD : Unit.M);

        final ObjectProperty<Unit> previous = new SimpleObjectProperty<>(displayUnit.get());
        unitGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            Unit newUnit = (newT == rbYd) ? Unit.YD : Unit.M;
            Unit oldUnit = previous.get();
            if (oldUnit != newUnit) {
                convertAllFields(oldUnit, newUnit);
                displayUnit.set(newUnit);
                previous.set(newUnit);
            }
        });

        AppState.get().currentWorkoutProperty().addListener((o, oldW, newW) -> {
            if (newW != null && !rbYd.isFocused() && !rbM.isFocused()) {
                boolean yd = (newW.getCourse() == Course.SCY);
                Unit newU = yd ? Unit.YD : Unit.M;
                Unit oldU = displayUnit.get();
                if (newU != oldU) {
                    convertAllFields(oldU, newU);
                    (yd ? rbYd : rbM).setSelected(true);
                    displayUnit.set(newU);
                }
            }
        });
    }

    private void wireState() {
        btnEdit.setOnAction(e -> {
            if (!hasSwimmer.getValue()) return;
            presenter.beginEdit();
            setEditable(true);
        });

        btnCancel.setOnAction(e -> {
            presenter.cancel();
            loadFromSwimmer();
            setEditable(false);
        });

        btnSave.setOnAction(e -> {
            if (!hasSwimmer.getValue()) return;
            if (!saveIntoSwimmer(boundSwimmer)) return;
            try { LocalStore.saveSwimmer(boundSwimmer); } catch (Exception ignored) {}
            presenter.save();
            setEditable(false);
            if (onSeedsSaved != null) onSeedsSaved.run();
        });

        btnEdit.disableProperty().bind( hasSwimmer.not().or(presenter.editingProperty()) );
        btnSave.disableProperty().bind( presenter.canSaveProperty().not() );
        btnCancel.disableProperty().bind( presenter.editingProperty().not() );
    }

    private void setEditable(boolean editable) {
        fields.values().forEach(tf -> tf.setEditable(editable));
    }

    private void loadFromSwimmer() {
        for (var e : fields.entrySet()) {
            StrokeType st = e.getKey();
            TimeSpan canonical100m = readCanonical100m(boundSwimmer, st);
            String txt = "";
            if (canonical100m != null) {
                double canonSec = canonical100m.toMillis() / 1000.0;
                double dispSec = convertCanonicalToDisplaySeconds(canonSec, displayUnit.get());
                txt = formatTimeSeconds(dispSec);
            }
            e.getValue().setText(txt);
        }
        presenter.cancel();
        presenter.save();
        presenter.editingProperty().set(false);
    }

    // ===== Conversions =====

    private static double convertDisplayToCanonicalSeconds(double sec, Unit disp) {
        return (disp == Unit.YD) ? sec * METER_TO_YARD : sec;
    }

    private static double convertCanonicalToDisplaySeconds(double sec, Unit disp) {
        return (disp == Unit.YD) ? sec * YARD_TO_METER : sec;
    }

    private void convertAllFields(Unit from, Unit to) {
        if (from == to) return;
        for (var e : fields.entrySet()) {
            String cur = e.getValue().getText();
            if (cur == null || cur.isBlank()) continue;
            TimeSpan t = parseTime(cur);
            if (t == null) continue;
            double seconds = t.toMillis() / 1000.0;
            double canonSec = convertDisplayToCanonicalSeconds(seconds, from);
            double newDisplaySec = convertCanonicalToDisplaySeconds(canonSec, to);
            e.getValue().setText(formatTimeSeconds(newDisplaySec));
        }
    }

    // ===== Model I/O specialized for SeedPace =====

    /** Derive canonical 100m time from existing SeedPace by using speed (m/s). */
    private static TimeSpan readCanonical100m(Swimmer s, StrokeType st) {
        if (s == null) return null;
        try {
            Map<StrokeType, SeedPace> map = s.getSeedPaces();
            if (map == null) return null;
            SeedPace sp = map.get(st);
            if (sp == null) return null;
            double v = sp.speedMps();
            if (v <= 0) return null;
            double sec100m = 100.0 / v;
            return TimeSpan.ofMillis(Math.round(sec100m * 1000.0));
        } catch (Throwable t) {
            return null;
        }
    }

    /** Save as SeedPace(100m, canonicalTime) using Swimmer#setAllSeedPaces. */
    private boolean saveIntoSwimmer(Swimmer s) {
        try {
            // Build a brand-new mutable map from the UI fields
            EnumMap<StrokeType, SeedPace> next = new EnumMap<>(StrokeType.class);

            for (var e : fields.entrySet()) {
                StrokeType st = e.getKey();
                String text = e.getValue().getText();
                TimeSpan parsed = parseTime(text);

                if (parsed == null) {
                    // blank field = clear this seed
                    continue;
                }

                // Convert display seconds (yd/m as chosen) -> canonical seconds (per 100m)
                double dispSec   = parsed.toMillis() / 1000.0;
                double canonSec  = convertDisplayToCanonicalSeconds(dispSec, displayUnit.get());
                TimeSpan canonTs = TimeSpan.ofMillis(Math.round(canonSec * 1000.0));

                // Store as “100 meters in canonTs”
                next.put(st, new SeedPace(Distance.ofMeters(100), canonTs));
            }

            // Atomically replace all seeds on the swimmer
            s.setAllSeedPaces(next);

            return true;
        } catch (Throwable t) {
            new Alert(Alert.AlertType.ERROR,
                    "Unable to save seeds — unexpected swimmer API.")
                    .showAndWait();
            return false;
        }
    }

    // ===== Time helpers =====

    private static TimeSpan parseTime(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;

        int minutes = 0, seconds;
        int hundredths = 0;

        String work = s;
        if (work.contains(":")) {
            String[] parts = work.split(":");
            if (!parts[0].isBlank()) minutes = parseIntSafe(parts[0], 0);
            work = (parts.length >= 2) ? parts[1] : "";
        }
        if (work.contains(".")) {
            String[] p2 = work.split("\\.");
            seconds = parseIntSafe(p2[0], 0);
            String h = (p2.length >= 2) ? p2[1] : "0";
            if (h.length() == 1) h = h + "0";
            hundredths = parseIntSafe(h.substring(0, Math.min(2, h.length())), 0);
        } else {
            seconds = parseIntSafe(work, 0);
        }

        return TimeSpan.ofMinutesSecondsMillis(minutes, seconds, hundredths * 10);
    }

    private static String formatTimeSeconds(double seconds) {
        long total = Math.max(0, Math.round(seconds));
        long m = total / 60;
        long s2 = total % 60;
        return String.format("%d:%02d", m, s2);
    }

    private static int parseIntSafe(String v, int def) {
        try { return Integer.parseInt(v.trim()); } catch (Exception e) { return def; }
    }
}