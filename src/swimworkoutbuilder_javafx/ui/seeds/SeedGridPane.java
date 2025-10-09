package swimworkoutbuilder_javafx.ui.seeds;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.model.enums.StrokeType;
import swimworkoutbuilder_javafx.model.pacing.SeedPace;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.model.units.TimeSpan;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.store.LocalStore;

import java.util.EnumMap;
import java.util.Map;

public class SeedGridPane extends VBox {

    private final ToggleGroup unitGroup = new ToggleGroup();
    private final RadioButton rbYards = new RadioButton("100 yards");
    private final RadioButton rbMeters = new RadioButton("100 meters");

    private final Map<StrokeType, TextField> timeFields = new EnumMap<>(StrokeType.class);

    private final Label titleLabel = new Label("Seed Times");

    private final Button btnEdit = new Button("Edit");
    private final Button btnCancel = new Button("Cancel");
    private final Button btnSave = new Button("Save & Exit");

    private Swimmer swimmer;
    private Runnable onSeedsSaved = () -> {};

    public SeedGridPane() {
        setAlignment(Pos.TOP_CENTER);
        setSpacing(10);
        setPadding(new Insets(10));

        titleLabel.getStyleClass().add("label-header");

        HBox radios = new HBox(20, rbYards, rbMeters);
        radios.setAlignment(Pos.CENTER);
        rbYards.setToggleGroup(unitGroup);
        rbMeters.setToggleGroup(unitGroup);
        rbYards.setSelected(true);

        GridPane grid = buildGrid();

        HBox buttons = new HBox(15, btnEdit, btnCancel, btnSave);
        buttons.setAlignment(Pos.CENTER);

        btnEdit.getStyleClass().add("button-secondary");
        btnCancel.getStyleClass().add("button-secondary");
        btnSave.getStyleClass().add("button-primary");

        // initial mode = view (not editing)
        setEditing(false);

        btnEdit.setOnAction(e -> setEditing(true));
        btnCancel.setOnAction(e -> {
            if (swimmer != null) loadFromSwimmer(swimmer);
            setEditing(false);
        });

        btnSave.setOnAction(e -> {
            if (applyToModel()) {
                // persist + nudge listeners
                Swimmer cur = AppState.get().getCurrentSwimmer();
                if (cur != null) {
                    try { LocalStore.saveSwimmer(cur); } catch (Exception ignored) {}
                    AppState.get().setCurrentSwimmer(cur);
                }
                setEditing(false);
                if (swimmer != null) loadFromSwimmer(swimmer);
                onSeedsSaved.run();
            }
        });

        getChildren().addAll(titleLabel, radios, grid, buttons);

        // keep in sync if current swimmer changes elsewhere
        AppState.get().currentSwimmerProperty().addListener((obs, o, s) -> {
            bindSwimmer(s);
        });
    }

    public void bindSwimmer(Swimmer s) {
        this.swimmer = s;
        loadFromSwimmer(s);
        setEditing(false);
    }

    public void setOnSeedsSaved(Runnable r) {
        this.onSeedsSaved = (r == null ? () -> {} : r);
    }

    private GridPane buildGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("grid-pane");
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(10));

        ColumnConstraints c0 = new ColumnConstraints();
        c0.setHalignment(HPos.RIGHT);
        c0.setMinWidth(80);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPrefWidth(110);
        c1.setHgrow(Priority.NEVER);
        grid.getColumnConstraints().addAll(c0, c1);

        Label hStroke = new Label("Stroke");
        hStroke.getStyleClass().add("label-column-header");
        Label hTime = new Label("Seed Time");
        hTime.getStyleClass().add("label-column-header");
        GridPane.setMargin(hStroke, new Insets(2,2,2,10));
        GridPane.setMargin(hTime, new Insets(2,2,2,10));
        grid.add(hStroke, 0, 0);
        grid.add(hTime,   1, 0);

        StrokeType[] rows = {
                StrokeType.FREESTYLE,
                StrokeType.BUTTERFLY,
                StrokeType.BACKSTROKE,
                StrokeType.BREASTSTROKE,
                StrokeType.INDIVIDUAL_MEDLEY,
                StrokeType.DRILL,
                StrokeType.KICK
        };

        for (int i = 0; i < rows.length; i++) {
            int row = i + 1;
            StrokeType stroke = rows[i];

            Label lbl = new Label(pretty(stroke));
            lbl.setFont(Font.font(14));
            TextField tf = new TextField();
            tf.setPromptText("m:ss.hh");
            tf.setTextFormatter(timeFormatter());

            GridPane.setMargin(lbl, new Insets(2,2,2,10));
            grid.add(lbl, 0, row);
            grid.add(tf,  1, row);

            timeFields.put(stroke, tf);
        }
        return grid;
    }

    private void setEditing(boolean editing) {
        rbYards.setDisable(!editing);
        rbMeters.setDisable(!editing);
        timeFields.values().forEach(tf -> tf.setEditable(editing));

        btnEdit.setDisable(editing);
        btnCancel.setDisable(!editing);
        btnSave.setDisable(!editing);
    }

    private void loadFromSwimmer(Swimmer s) {
        if (s == null) {
            timeFields.values().forEach(tf -> tf.setText(""));
            return;
        }
        boolean useYards = true;
        for (var e : s.getSeedPaces().entrySet()) {
            double m = e.getValue().getOriginalDistance().toMeters();
            if (m >= 99.5 && m <= 100.5) { useYards = false; break; }
        }
        rbYards.setSelected(useYards);
        rbMeters.setSelected(!useYards);

        // clear first
        timeFields.values().forEach(tf -> tf.setText(""));
        s.getSeedPaces().forEach((stroke, pace) -> {
            timeFields.get(stroke).setText(fmtTime(pace.getTime()));
        });
    }

    /** Read UI → write into bound swimmer. Returns true if success. */
    private boolean applyToModel() {
        if (swimmer == null) return false;
        Map<StrokeType, SeedPace> paces = readPacesFromFields();
        swimmer.clearAllSeeds();
        paces.forEach(swimmer::updateSeedTime);
        return true;
    }

    private Map<StrokeType, SeedPace> readPacesFromFields() {
        Map<StrokeType, SeedPace> out = new EnumMap<>(StrokeType.class);
        boolean yards = rbYards.isSelected();
        Distance d = yards ? Distance.ofYards(100) : Distance.ofMeters(100);

        for (var e : timeFields.entrySet()) {
            String txt = e.getValue().getText().trim();
            if (txt.isEmpty()) continue;
            TimeSpan t = parseTime(txt);
            out.put(e.getKey(), new SeedPace(d, t));
        }
        return out;
    }

    // --- helpers ---

    private static String pretty(StrokeType st) {
        return switch (st) {
            case FREESTYLE -> "Freestyle";
            case BUTTERFLY -> "Butterfly";
            case BACKSTROKE -> "Backstroke";
            case BREASTSTROKE -> "Breaststroke";
            case INDIVIDUAL_MEDLEY -> "Individual Medley";
            case DRILL -> "Drill";
            case KICK -> "Kick";
        };
    }

    private static TextFormatter<String> timeFormatter() {
        return new TextFormatter<>(change -> {
            String s = change.getControlNewText();
            if (!s.matches("[0-9:.]*")) return null;
            if (s.length() > 7) return null; // m:ss.hh
            return change;
        });
    }

    private static String fmtTime(TimeSpan t) {
        long ms = t.toMillis();
        long minutes = ms / 60000;
        long seconds = (ms % 60000) / 1000;
        long hundredths = ((ms % 1000) + 5) / 10;
        if (hundredths == 100) { hundredths = 0; seconds++; }
        if (seconds == 60) { seconds = 0; minutes++; }
        return String.format("%d:%02d.%02d", minutes, seconds, hundredths);
    }

    private static TimeSpan parseTime(String s) {
        s = s.trim();
        int minutes = 0; int seconds; int hundredths = 0;
        if (s.contains(":")) {
            String[] parts = s.split(":");
            minutes = Integer.parseInt(parts[0]);
            s = parts[1];
        }
        if (s.contains(".")) {
            String[] parts = s.split("\\.");
            seconds = Integer.parseInt(parts[0]);
            String h = parts[1];
            if (h.length()==1) h = h + "0";
            hundredths = Integer.parseInt(h.substring(0,2));
        } else {
            seconds = Integer.parseInt(s);
        }
        return TimeSpan.ofMinutesSecondsMillis(minutes, seconds, hundredths * 10);
    }
}