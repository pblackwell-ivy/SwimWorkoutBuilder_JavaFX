package swimworkoutbuilder_javafx.ui.seeds;

import java.util.EnumMap;
import java.util.Map;
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
import swimworkoutbuilder_javafx.ui.Icons;

/**
 * SeedGridPane — compact card showing/editing 100-distance seed times by stroke.
 * View mode shows an edit icon; edit mode shows cancel/save icons.
 * Buttons: ✎ (edit), ↩ (cancel), 💾 (save)
 */
public final class SeedGridPane extends BorderPane {

    // Displayed unit only (model stays canonical = per 100m)
    private enum Unit { YD, M }

    private static final double YARD_TO_METER = 0.9144;
    private static final double METER_TO_YARD = 1.0 / 0.9144;

    private final GridPane seedGrid = new GridPane();
    private final Label title = new Label("Seed Times");

    // Header action buttons (need to be fields because wireState() binds to them)
    private Button btnEdit;
    private Button btnSave;
    private Button btnCancel;

    private final ToggleGroup unitGroup = new ToggleGroup();
    private final RadioButton rbYd = new RadioButton("yd");
    private final RadioButton rbM  = new RadioButton("m");
    private final ObjectProperty<Unit> displayUnit = new SimpleObjectProperty<>(Unit.YD);

    private final Map<StrokeType, TextField> fields = new EnumMap<>(StrokeType.class);

    private final SeedTimesPresenter presenter = new SeedTimesPresenter(AppState.get());
    private final BooleanProperty hasSwimmer = new SimpleBooleanProperty(false);

    private Swimmer boundSwimmer;
    private Runnable onSeedsSaved;

    public SeedGridPane() {
        getStyleClass().addAll("card", "compact");

        buildUI();
        initUnitsFromWorkout();
        wireState();
        var app = AppState.get();
        bindSwimmer(app.getCurrentSwimmer()); // initial
        app.currentSwimmerProperty().addListener((obs, oldS, newS) -> bindSwimmer(newS));
        setEditable(false);
    }

    public void bindSwimmer(Swimmer s) {
        boundSwimmer = s;
        hasSwimmer.set(s != null);
        loadFromSwimmer();
    }

    public void setOnSeedsSaved(Runnable r) { this.onSeedsSaved = r; }

    private void buildUI() {
        setPadding(new Insets(6));

        // — Title + actions in a single header row
        title.getStyleClass().add("card-title");

        btnEdit = new Button();
        btnEdit.getStyleClass().setAll("button","secondary","sm","icon");
        btnEdit.setGraphic(Icons.make("pencil-swim-text", 16));
        btnEdit.setTooltip(new Tooltip("Edit seed times"));

        btnSave = new Button();
        btnSave.getStyleClass().setAll("button","secondary","sm","icon");
        btnSave.setGraphic(Icons.make("save-swim-text", 16));
        btnSave.setTooltip(new Tooltip("Save seed times"));

        btnCancel = new Button();
        btnCancel.getStyleClass().setAll("button","secondary","sm","icon");
        btnCancel.setGraphic(Icons.make("circle-x-swim-text", 16));
        btnCancel.setTooltip(new Tooltip("Cancel edits"));

        // ensure small consistent square hit targets
        for (Button b : new Button[]{btnEdit, btnCancel, btnSave}) {
            b.setMinWidth(32); b.setPrefWidth(32);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(8, title, spacer, btnEdit, btnCancel, btnSave);
        header.getStyleClass().add("card-header");
        header.setAlignment(Pos.CENTER_LEFT);

        // — Units toggle directly under header
        rbYd.setToggleGroup(unitGroup);
        rbM.setToggleGroup(unitGroup);
        rbYd.setFocusTraversable(false);
        rbM.setFocusTraversable(false);
        HBox unitBox = new HBox(4, new Label("Units:"), rbYd, rbM);
        unitBox.setAlignment(Pos.CENTER_LEFT);

        // — Grid
        seedGrid.getStyleClass().addAll("form-grid", "header-gap"); // header-gap = smaller top gap
        // column headers
        Label colStroke = new Label("Stroke");
        Label colTime   = new Label();
        colStroke.getStyleClass().setAll("table-column-header");
        colTime.getStyleClass().setAll("table-column-header");
        colTime.textProperty().bind(Bindings.createStringBinding(
                () -> "100 " + (displayUnit.get() == Unit.YD ? "yd" : "m") + " seed", displayUnit));

        seedGrid.add(colStroke, 0, 0);
        seedGrid.add(colTime,   1, 0);
        GridPane.setHalignment(colStroke, HPos.LEFT);
        GridPane.setHalignment(colTime,   HPos.CENTER);

        int row = 1;
        for (StrokeType st : StrokeType.values()) addRow(row++, st);

        ColumnConstraints c0 = new ColumnConstraints(); // labels
        ColumnConstraints c1 = new ColumnConstraints(); // inputs
        c0.setHgrow(Priority.NEVER);
        c1.setHgrow(Priority.NEVER);
        c1.setHalignment(HPos.CENTER);
        seedGrid.getColumnConstraints().setAll(c0, c1);

        // — Stack content vertically (no bottom button row needed anymore)
        VBox content = new VBox(6, header, unitBox, seedGrid);
        content.setFillWidth(false);
        content.setAlignment(Pos.TOP_CENTER);
        setCenter(content);

        // initial visual (bindings take over later)
        btnSave.setDisable(true);
        btnCancel.setDisable(true);
    }

    // --- helper ---
    private static void setRoles(Button b, String... roles) {
        // keep "button" + role; remove previous role(s)
        b.getStyleClass().removeAll("primary","secondary","ghost","success","danger","sm","icon");
        b.getStyleClass().add("button");
        b.getStyleClass().addAll(roles);
    }

    private void addRow(int row, StrokeType stroke) {
        var lbl = new Label(stroke.getLabel());
        lbl.getStyleClass().add("table-row-label");

        var tf = new TextField();
        tf.setPromptText("m:ss.hh");
        tf.getStyleClass().add("seed-time-input");
        tf.setPrefColumnCount(6);
        tf.setMaxWidth(54);
        tf.textProperty().addListener((o, a, b) -> {
            if (presenter.editingProperty().get()) presenter.markDirty();
        });

        fields.put(stroke, tf);

        seedGrid.add(lbl, 0, row);
        seedGrid.add(tf, 1, row);
        GridPane.setHalignment(lbl, HPos.LEFT);
    }

    /**
     * Initialize display unit from current workout; on unit/workout change we
     * re-render fields from canonical seeds instead of converting text in place.
     */
    private void initUnitsFromWorkout() {
        Workout w = AppState.get().getCurrentWorkout();
        Course c = (w != null ? w.getCourse() : Course.SCY);
        boolean yards = (c == Course.SCY);
        (yards ? rbYd : rbM).setSelected(true);
        displayUnit.set(yards ? Unit.YD : Unit.M);

        // Unit toggle: just change the flag and reload from swimmer
        unitGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            Unit newUnit = (newT == rbYd) ? Unit.YD : Unit.M;
            if (displayUnit.get() != newUnit) {
                displayUnit.set(newUnit);
                loadFromSwimmer();
            }
        });

        // Workout course changed externally (SCY/SCM/LCM) → update display unit and reload
        AppState.get().currentWorkoutProperty().addListener((o, oldW, newW) -> {
            if (newW != null && !rbYd.isFocused() && !rbM.isFocused()) {
                boolean yd = (newW.getCourse() == Course.SCY);
                Unit newU = yd ? Unit.YD : Unit.M;
                if (displayUnit.get() != newU) {
                    (yd ? rbYd : rbM).setSelected(true);
                    displayUnit.set(newU);
                    loadFromSwimmer();
                }
            }
        });
    }

    private void wireState() {
        presenter.editingProperty().addListener((obs, wasEditing, isEditing) -> {
            if (isEditing) {
                setRoles(btnEdit, "ghost", "sm", "icon");     // fades while editing
                setRoles(btnSave, "primary", "sm", "icon");   // main action
                setRoles(btnCancel, "secondary", "sm", "icon");
            } else {
                setRoles(btnEdit, "secondary", "sm", "icon"); // single action visible
                setRoles(btnSave, "ghost", "sm", "icon");
                setRoles(btnCancel, "ghost", "sm", "icon");
            }
        });

        btnEdit.setOnAction(e -> {
            if (!hasSwimmer.get()) return;
            presenter.beginEdit();
            setEditable(true);
        });

        btnCancel.setOnAction(e -> {
            presenter.cancel();
            loadFromSwimmer();
            setEditable(false);
        });

        btnSave.setOnAction(e -> {
            if (!hasSwimmer.get()) return;
            if (!saveIntoSwimmer(boundSwimmer)) return;
            try { LocalStore.saveSwimmer(boundSwimmer); } catch (Exception ignored) {}
            presenter.save();
            setEditable(false);
            if (onSeedsSaved != null) onSeedsSaved.run();
        });

        // Enable/disable by state
        btnEdit.disableProperty().bind(
                hasSwimmer.not().or(presenter.editingProperty())  // disable Edit while editing or no swimmer
        );

        btnSave.disableProperty().bind(
                presenter.editingProperty().not()
                        .or(presenter.canSaveProperty().not())    // Save only when editing AND canSave
        );

        btnCancel.disableProperty().bind(
                presenter.editingProperty().not()                 // Cancel only when editing
        );

        // Visibility to match card: Edit in view; Cancel/Save in edit
        btnEdit.visibleProperty().bind(presenter.editingProperty().not());
        btnEdit.managedProperty().bind(btnEdit.visibleProperty());
        btnSave.visibleProperty().bind(presenter.editingProperty());
        btnSave.managedProperty().bind(btnSave.visibleProperty());
        btnCancel.visibleProperty().bind(presenter.editingProperty());
        btnCancel.managedProperty().bind(btnCancel.visibleProperty());
    }

    private void setEditable(boolean editable) {
        fields.values().forEach(tf -> tf.setEditable(editable));
    }

    private void loadFromSwimmer() {
        for (var e : fields.entrySet()) {
            StrokeType st = e.getKey();
            TimeSpan canonical100m = readCanonical100m(boundSwimmer, st); // from model (m canonical)
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
        // Display → canonical (per 100m). Yards should be larger when converted to meters.
        return (disp == Unit.YD) ? sec * METER_TO_YARD : sec;
    }

    private static double convertCanonicalToDisplaySeconds(double sec, Unit disp) {
        // Canonical (100m) → display. Yards should be smaller (× 0.9144).
        return (disp == Unit.YD) ? sec * YARD_TO_METER : sec;
    }

    // (Kept for possible future use, but no longer called during toggles)
    @SuppressWarnings("unused")
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
            EnumMap<StrokeType, SeedPace> next = new EnumMap<>(StrokeType.class);
            for (var e : fields.entrySet()) {
                StrokeType st = e.getKey();
                String text = e.getValue().getText();
                TimeSpan parsed = parseTime(text);
                if (parsed == null) continue; // blank = clear

                double dispSec   = parsed.toMillis() / 1000.0;
                double canonSec  = convertDisplayToCanonicalSeconds(dispSec, displayUnit.get());
                TimeSpan canonTs = TimeSpan.ofMillis(Math.round(canonSec * 1000.0));
                next.put(st, new SeedPace(Distance.ofMeters(100), canonTs));
            }
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
