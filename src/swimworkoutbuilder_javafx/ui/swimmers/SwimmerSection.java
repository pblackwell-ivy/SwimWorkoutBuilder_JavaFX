package swimworkoutbuilder_javafx.ui.swimmers;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.store.LocalStore;
import swimworkoutbuilder_javafx.ui.DateFmt;

import java.util.Objects;

public final class SwimmerSection {

    private final VBox root = new VBox(10);

    private final Label lblSelector = new Label("Swimmer");
    private final ComboBox<Swimmer> cbSwimmer = new ComboBox<>();
    private final Button btnAdd = new Button("+");
    private final Button btnEdit = new Button("✎");
    private final Button btnDelete = new Button("🗑");

    private final Label lblPreferred = new Label();
    private final Label lblTeam = new Label();
    private final Label lblCreated = new Label();
    private final Label lblUpdated = new Label();

    public SwimmerSection() {
        buildUI();
        wireState();
    }

    public Node node() { return root; }

    // ---------------- UI ----------------

    private void buildUI() {
        root.setPadding(new Insets(8, 10, 8, 10));
        root.setFillWidth(true); // important so children can grow

        // Top label (on its own row to avoid squeezing the selector)
        lblSelector.getStyleClass().add("label-column-header");

        // Combo formatting
        cbSwimmer.setConverter(new StringConverter<>() {
            @Override public String toString(Swimmer s) { return displayName(s); }
            @Override public Swimmer fromString(String string) { return null; }
        });
        cbSwimmer.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(Swimmer s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : displayName(s));
            }
        });
        cbSwimmer.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Swimmer s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : displayName(s));
            }
        });

        // Sizing + roles
        cbSwimmer.setMaxWidth(Double.MAX_VALUE);
        btnAdd.getStyleClass().addAll("button","accent","icon","sm");
        btnEdit.getStyleClass().addAll("button","secondary","icon","sm");
        btnDelete.getStyleClass().addAll("button","danger","icon","sm");

        btnAdd.setTooltip(new Tooltip("Add swimmer"));
        btnEdit.setTooltip(new Tooltip("Edit swimmer"));
        btnDelete.setTooltip(new Tooltip("Delete swimmer"));

        HBox selectorRow = new HBox(8, cbSwimmer, btnAdd, btnEdit, btnDelete);
        HBox.setHgrow(cbSwimmer, Priority.ALWAYS);
        selectorRow.getStyleClass().add("toolbar");

        // Details grid (no “Name” row — the selector already shows it)
        GridPane info = new GridPane();
        info.getStyleClass().add("grid-pane");
        info.setHgap(8);
        info.setVgap(4);

        // Prevent header truncation: make col 0 wide enough, col 1 grows
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(90);               // enough for “Preferred Name”, “Created”, etc.
        c0.setPrefWidth(110);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        info.getColumnConstraints().setAll(c0, c1);

        int r = 0;
        info.add(rowLabel("Preferred Name:"), 0, r); info.add(lblPreferred, 1, r++);
        info.add(rowLabel("Team:"),            0, r); info.add(lblTeam,      1, r++);
        info.add(rowLabel("Created:"),         0, r); info.add(lblCreated,   1, r++);
        info.add(rowLabel("Updated:"),         0, r); info.add(lblUpdated,   1, r);

        // Assemble
        root.getChildren().setAll(lblSelector, selectorRow, info);

        // Initial disable
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);

        // Handlers (edit/delete are stubs for now)
        btnAdd.setOnAction(e -> onAddSwimmer());
        btnEdit.setOnAction(e ->
                new Alert(Alert.AlertType.INFORMATION, "Edit Swimmer (coming soon)").showAndWait());
        btnDelete.setOnAction(e ->
                new Alert(Alert.AlertType.INFORMATION, "Delete Swimmer (coming soon)").showAndWait());
    }

    private static Label rowLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("label-column-header");
        return l;
    }

    // ---------------- State wiring ----------------

    private void wireState() {
        var app = AppState.get();

        // Populate list
        cbSwimmer.setItems(app.getSwimmers());

        // Keep selection in sync (two-way)
        cbSwimmer.getSelectionModel().selectedItemProperty().addListener((o, oldV, s) -> {
            if (s != null && s != app.getCurrentSwimmer()) {
                app.setCurrentSwimmer(s);
            }
        });

        app.currentSwimmerProperty().addListener((o, oldV, s) -> {
            if (s != cbSwimmer.getValue()) cbSwimmer.setValue(s);
            refreshDetails(s);
            // Force a redraw so the button cell updates even if the same instance
            cbSwimmer.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Swimmer it, boolean empty) {
                    super.updateItem(it, empty);
                    setText(empty || it == null ? null : displayName(it));
                }
            });
        });

        // Initial display
        if (app.getCurrentSwimmer() != null) {
            cbSwimmer.setValue(app.getCurrentSwimmer());
            refreshDetails(app.getCurrentSwimmer());
        } else if (!app.getSwimmers().isEmpty()) {
            cbSwimmer.getSelectionModel().selectFirst();
        }
    }

    // ---------------- Actions ----------------

    private void onAddSwimmer() {
        Dialog<Swimmer> dlg = new Dialog<>();
        dlg.setTitle("New Swimmer");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        TextField tfFirst = new TextField();
        TextField tfLast  = new TextField();
        TextField tfPref  = new TextField();
        TextField tfTeam  = new TextField();

        GridPane gp = new GridPane();
        gp.getStyleClass().add("grid-pane");
        gp.addRow(0, rowLabel("First:"), tfFirst);
        gp.addRow(1, rowLabel("Last:"),  tfLast);
        gp.addRow(2, rowLabel("Preferred:"), tfPref);
        gp.addRow(3, rowLabel("Team:"), tfTeam);
        dlg.getDialogPane().setContent(gp);

        Node ok = dlg.getDialogPane().lookupButton(ButtonType.OK);
        ok.setDisable(true);
        tfFirst.textProperty().addListener((o,a,b)-> ok.setDisable(b.trim().isEmpty()));
        tfLast.textProperty().addListener((o,a,b)-> ok.setDisable(tfFirst.getText().trim().isEmpty() || b.trim().isEmpty()));

        dlg.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            return new Swimmer(
                    java.util.UUID.randomUUID(),
                    tfFirst.getText().trim(),
                    tfLast.getText().trim(),
                    tfPref.getText().trim(),
                    tfTeam.getText().trim(),
                    java.time.Instant.now(),
                    java.time.Instant.now()
            );
        });

        Swimmer created = dlg.showAndWait().orElse(null);
        if (created == null) return;

        try { LocalStore.saveSwimmer(created); } catch (Exception ignored) {}
        var app = AppState.get();
        app.getSwimmers().add(created);
        app.setCurrentSwimmer(created);
    }

    private void refreshDetails(Swimmer s) {
        boolean has = (s != null);
        btnEdit.setDisable(!has);
        btnDelete.setDisable(!has);

        lblPreferred.setText(has && notBlank(s.getPreferredName()) ? s.getPreferredName() : "—");
        lblTeam.setText(has && notBlank(s.getTeamName()) ? s.getTeamName() : "—");
        lblCreated.setText(has ? DateFmt.local(s.getCreatedAt()) : "—");
        lblUpdated.setText(has ? DateFmt.local(s.getUpdatedAt()) : "—");
    }

    // ---------------- Helpers ----------------

    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }

    private static String displayName(Swimmer s) {
        if (s == null) return "";
        String preferred = s.getPreferredName();
        String first = s.getFirstName();
        String last  = s.getLastName();
        String left  = (preferred != null && !preferred.isBlank()) ? preferred :
                (first != null ? first : "");
        return (left + ((last != null && !last.isBlank()) ? " " + last : "")).trim();
    }
}