package swimworkoutbuilder_javafx.ui.seeds;


import java.util.EnumMap;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.model.enums.StrokeType;
import swimworkoutbuilder_javafx.model.pacing.SeedPace;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.model.units.TimeSpan;
/**
 * [Dialog] SeedFormDialog for the "dialogs" feature.
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *   <li>Collect user input with clear primary/secondary actions</li>
 *   <li>Validate inputs and surface errors accessibly</li>
 *   <li>Return a result to the caller</li>
 * </ul>
 *
 * <p><b>Design Notes:</b>
 * <ul>
 *   <li>Follows canonical roles (primary/secondary/tertiary/destructive)</li>
 *   <li>ESC/Enter keys match platform expectations</li>
 *   <li>Validation separated from presentation</li>
 * </ul>
 *
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * // Typical usage for SeedFormDialog
 * SeedFormDialog obj = new SeedFormDialog();
 * obj.toString(); // replace with real usage
 * }</pre>
 *
 * @author Parker Blackwell
 * @version 1.0
 * @since 2025-10-14
 */

public class SeedFormDialog {

    public static void show(Swimmer swimmer) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Seed Times");

        Label lblSwimmerName = new Label(swimmer != null
                ? "Swimmer: " + swimmer.getFirstName() + " " + swimmer.getLastName()
                : "Swimmer: —");

        ChoiceBox<StrokeType> cbStroke = new ChoiceBox<>();
        cbStroke.getItems().setAll(StrokeType.values());
        cbStroke.getSelectionModel().selectFirst();

        TextField tfDistance = new TextField("100");
        RadioButton rbYards = new RadioButton("Yards");
        RadioButton rbMeters = new RadioButton("Meters");
        ToggleGroup unitGroup = new ToggleGroup(); rbYards.setToggleGroup(unitGroup); rbMeters.setToggleGroup(unitGroup); rbYards.setSelected(true);

        TextField tfTime = new TextField("2:30");
        Button btnAdd = new Button("Add/Update");
        Button btnDelete = new Button("Delete");
        Button btnSave = new Button("Save");
        Button btnCancel = new Button("Cancel");
        btnSave.getStyleClass().addAll("button","primary");     // new
        btnCancel.getStyleClass().addAll("button","secondary"); // new
        btnSave.setDefaultButton(true);
        btnCancel.setCancelButton(true);
        btnDelete.setDisable(true);


        TableView<Row> table = new TableView<>();
        TableColumn<Row, String> colStroke = new TableColumn<>("Stroke");
        TableColumn<Row, String> colDistance = new TableColumn<>("Distance");
        TableColumn<Row, String> colPace = new TableColumn<>("Pace");
        colStroke.setPrefWidth(110);
        colDistance.setPrefWidth(90);
        colPace.setPrefWidth(100);
        table.getColumns().addAll(java.util.List.of(colStroke, colDistance, colPace));

        final Map<StrokeType, SeedPace> working = new EnumMap<>(StrokeType.class);
        if (swimmer != null && swimmer.getSeedPaces() != null) {
            working.putAll(swimmer.getSeedPaces());
        }
        final ObservableList<Row> rows = FXCollections.observableArrayList();

        colStroke.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().stroke.name()));
        colDistance.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(fmtDistanceShort(data.getValue().pace.getOriginalDistance())));
        colPace.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(fmtTime(data.getValue().pace.getTime())));
        table.setItems(rows);
        refreshRows(working, rows);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldR, r) -> {
            boolean has = r != null;
            btnDelete.setDisable(!has);
            if (has) {
                cbStroke.setValue(r.stroke);
                Distance d = r.pace.getOriginalDistance();
                int yards = (int)Math.round(d.toYards());
                int meters = (int)Math.round(d.toMeters());
                if (yards % 25 == 0) { tfDistance.setText(Integer.toString(yards)); rbYards.setSelected(true); }
                else { tfDistance.setText(Integer.toString(meters)); rbMeters.setSelected(true); }
                tfTime.setText(fmtTime(r.pace.getTime()));
            }
        });

        btnAdd.setOnAction(e -> {
            StrokeType stroke = cbStroke.getValue();
            String distTxt = tfDistance.getText().trim();
            String timeTxt = tfTime.getText().trim();
            if (distTxt.isEmpty() || timeTxt.isEmpty()) { new Alert(Alert.AlertType.WARNING, "Distance and time are required.").showAndWait(); return; }
            int amount;
            try { amount = Integer.parseInt(distTxt); } catch (NumberFormatException ex) { new Alert(Alert.AlertType.WARNING, "Distance must be an integer.").showAndWait(); return; }
            Distance d = rbYards.isSelected() ? Distance.ofYards(amount) : Distance.ofMeters(amount);
            TimeSpan t = parseTime(timeTxt);
            working.put(stroke, new SeedPace(d, t));
            refreshRows(working, rows);
        });

        btnDelete.setOnAction(e -> {
            Row sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            working.remove(sel.stroke);
            refreshRows(working, rows);
        });

        btnSave.setOnAction(e -> {
            if (swimmer == null) { dialog.close(); return; }
            swimmer.setAllSeedPaces(working);
            dialog.close();
        });

        btnCancel.setOnAction(e -> dialog.close());

        GridPane form = new GridPane();
        form.setHgap(8); form.setVgap(8); form.setPadding(new Insets(8));
        form.addRow(0, new Label("Stroke:"), cbStroke);
        form.addRow(1, new Label("Distance:"), tfDistance);
        form.addRow(2, new Label("Units:"), new HBox(8, rbYards, rbMeters));
        form.addRow(3, new Label("Time (m:ss.HH):"), tfTime);
        form.addRow(4, btnAdd, btnDelete);

        BorderPane root = new BorderPane();
        root.setTop(lblSwimmerName);
        BorderPane.setMargin(lblSwimmerName, new Insets(8));
        root.setCenter(table);
        root.setBottom(new HBox(8, form, new HBox(12, btnCancel, btnSave)));
        BorderPane.setMargin(root.getBottom(), new Insets(8));

        dialog.setScene(new Scene(root, 560, 440));
        dialog.setResizable(false);
        dialog.showAndWait();
    }

    // ----- helpers -----

    public static final class Row { final StrokeType stroke; final SeedPace pace; Row(StrokeType s, SeedPace p) { this.stroke = s; this.pace = p; } }

    private static void refreshRows(Map<StrokeType, SeedPace> working, ObservableList<Row> rows) {
        rows.setAll(working.entrySet().stream().map(e -> new Row(e.getKey(), e.getValue())).toList());
    }

    private static String fmtDistanceShort(Distance d) {
        int y = (int)Math.round(d.toYards());
        int m = (int)Math.round(d.toMeters());
        if (y % 25 == 0) return y + " yd";
        return m + " m";
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
        if (s.contains(":")) { String[] parts = s.split(":"); minutes = Integer.parseInt(parts[0]); s = parts[1]; }
        if (s.contains(".")) { String[] parts = s.split("\\."); seconds = Integer.parseInt(parts[0]); String h = parts[1]; if (h.length()==1) h = h + "0"; hundredths = Integer.parseInt(h.substring(0,2)); }
        else { seconds = Integer.parseInt(s); }
        return TimeSpan.ofMinutesSecondsMillis(minutes, seconds, hundredths * 10);
    }
}
