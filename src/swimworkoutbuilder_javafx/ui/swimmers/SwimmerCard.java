package swimworkoutbuilder_javafx.ui.swimmers;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.store.LocalStore;
import swimworkoutbuilder_javafx.ui.DateFmt;
import swimworkoutbuilder_javafx.ui.Icons;

/**
 * SwimmerCard — shows/edit the current swimmer.
 * Contains the form (First, Last, Team, Created, Updated) and the action row.
 * Self-contained UI + behavior. Host calls node() to embed.
 */
public final class SwimmerCard {

    private final VBox root = new VBox(10);

    // --- fields/labels ---
    private final TextField tfFirst = new TextField();
    private final TextField tfLast  = new TextField();
    private final TextField tfTeam  = new TextField();
    private final Label lblCreated  = new Label();
    private final Label lblUpdated  = new Label();

    // --- actions (icon only) ---
    private final Button btnEdit   = new Button();
    private final Button btnSave   = new Button();
    private final Button btnCancel = new Button();
    private final Button btnDelete = new Button();

    // local mode
    private final BooleanProperty editing = new SimpleBooleanProperty(false);

    public SwimmerCard() {
        buildUI();
        wireState();
        wireActions();
    }

    public Node node() { return root; }

    /** Allow host to refresh the Updated label (e.g., after seed save). */
    public void refreshUpdatedFromApp() {
        var s = AppState.get().getCurrentSwimmer();
        lblUpdated.setText(s != null ? DateFmt.local(s.getUpdatedAt()) : "—");
    }

    // ---------------------------------------------------------------------
    // UI
    // ---------------------------------------------------------------------
    private void buildUI() {

        // Header: "Current swimmer" + actions to the right
        Label cardTitle = new Label("Current Swimmer");
        cardTitle.getStyleClass().add("card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Buttons: classes + icons + tooltips
        btnEdit.getStyleClass().setAll("button","secondary","sm","icon");
        btnSave.getStyleClass().setAll("button","primary","sm","icon");
        btnCancel.getStyleClass().setAll("button","ghost","sm","icon");
        btnDelete.getStyleClass().setAll("button","danger","sm","icon");

        btnEdit.setGraphic(Icons.make("square-pen", 16));
        btnDelete.setGraphic(Icons.make("trash-2", 16));
        btnSave.setGraphic(Icons.make("save", 16));
        btnCancel.setGraphic(Icons.make("circle-x", 16));

        btnEdit.setTooltip(new Tooltip("Edit swimmer"));
        btnSave.setTooltip(new Tooltip("Save changes"));
        btnCancel.setTooltip(new Tooltip("Cancel edits"));
        btnDelete.setTooltip(new Tooltip("Delete swimmer"));

        double iconW = 36;
        for (Button b : new Button[]{btnEdit, btnSave, btnCancel, btnDelete}) {
            b.setMinWidth(iconW);
            b.setPrefWidth(iconW);
        }
        HBox header = new HBox(8, cardTitle, spacer, btnEdit, btnDelete, btnCancel, btnSave);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("card-header");


        // Form grid
        GridPane swimmerForm = new GridPane();
        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(60);
        // c0.setPrefWidth(100);
        c0.setHgrow(Priority.NEVER);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setFillWidth(true);
        swimmerForm.getColumnConstraints().setAll(c0, c1);

        swimmerForm.getStyleClass().add("form-grid");
        swimmerForm.setHgap(8);
        swimmerForm.setVgap(8);

        Label lFirst   = new Label("First name:");
        Label lLast    = new Label("Last name:");
        Label lTeam    = new Label("Team:");
        Label lCreated = new Label("Created:");
        Label lUpdated = new Label("Updated:");

        lFirst.getStyleClass().add("table-row-label");
        lLast.getStyleClass().add("table-row-label");
        lTeam.getStyleClass().add("table-row-label");
        lCreated.getStyleClass().add("table-row-label");
        lUpdated.getStyleClass().add("table-row-label");

        lFirst.setMinWidth(Region.USE_PREF_SIZE);
        lLast.setMinWidth(Region.USE_PREF_SIZE);
        lTeam.setMinWidth(Region.USE_PREF_SIZE);
        lCreated.setMinWidth(Region.USE_PREF_SIZE);
        lUpdated.setMinWidth(Region.USE_PREF_SIZE);

        swimmerForm.addRow(0, lFirst,   tfFirst);
        swimmerForm.addRow(1, lLast,    tfLast);
        swimmerForm.addRow(2, lTeam,    tfTeam);
        swimmerForm.addRow(3, lCreated, lblCreated);
        swimmerForm.addRow(4, lUpdated, lblUpdated);

        GridPane.setHgrow(tfFirst, Priority.ALWAYS);
        GridPane.setHgrow(tfLast,  Priority.ALWAYS);
        GridPane.setHgrow(tfTeam,  Priority.ALWAYS);
        tfFirst.setMaxWidth(Double.MAX_VALUE);
        tfLast.setMaxWidth(Double.MAX_VALUE);
        tfTeam.setMaxWidth(Double.MAX_VALUE);



        // Visibility by mode (managed follows visible to prevent gaps)
        btnEdit.visibleProperty().bind(editing.not());
        btnEdit.managedProperty().bind(btnEdit.visibleProperty());
        btnDelete.visibleProperty().bind(editing.not());
        btnDelete.managedProperty().bind(btnDelete.visibleProperty());
        btnSave.visibleProperty().bind(editing);
        btnSave.managedProperty().bind(btnSave.visibleProperty());
        btnCancel.visibleProperty().bind(editing);
        btnCancel.managedProperty().bind(btnCancel.visibleProperty());

        root.getStyleClass().add("card");
        root.setFillWidth(true);
        root.setPadding(new Insets(12));
        root.getChildren().setAll(header, swimmerForm);

        btnSave.setDefaultButton(true);
        btnCancel.setCancelButton(true);
        setFormEditable(false);
        editing.set(false);
    }

    // ---------------------------------------------------------------------
    // State & actions
    // ---------------------------------------------------------------------
    private void wireState() {
        var app = AppState.get();

        app.currentSwimmerProperty().addListener((o, oldV, s) -> {
            loadToForm(s);
            lblCreated.setText(s != null ? DateFmt.local(s.getCreatedAt()) : "—");
            lblUpdated.setText(s != null ? DateFmt.local(s.getUpdatedAt()) : "—");

            boolean has = (s != null);
            btnEdit.setDisable(!has);
            btnDelete.setDisable(!has);

            if (!has) {
                setFormEditable(false);
                editing.set(false);
            }
        });

        // Initial
        var s0 = app.getCurrentSwimmer();
        if (s0 == null && !app.getSwimmers().isEmpty()) {
            s0 = app.getSwimmers().get(0);
            app.setCurrentSwimmer(s0);
        }
        loadToForm(s0);
        lblCreated.setText(s0 != null ? DateFmt.local(s0.getCreatedAt()) : "—");
        lblUpdated.setText(s0 != null ? DateFmt.local(s0.getUpdatedAt()) : "—");
        btnEdit.setDisable(s0 == null);
        btnDelete.setDisable(s0 == null);
    }

    private void wireActions() {
        btnEdit.setOnAction(e -> {
            if (AppState.get().getCurrentSwimmer() == null) return;
            setFormEditable(true);
            editing.set(true);
        });

        btnCancel.setOnAction(e -> {
            loadToForm(AppState.get().getCurrentSwimmer());
            setFormEditable(false);
            editing.set(false);
        });

        btnSave.setOnAction(e -> {
            var app = AppState.get();
            var cur = app.getCurrentSwimmer();
            if (cur == null) return;

            var updated = applyEdits(cur);
            try { LocalStore.saveSwimmer(updated); } catch (Exception ignored) {}

            var list = app.getSwimmers();
            int idx = list.indexOf(cur);
            if (idx >= 0) list.set(idx, updated);
            app.setCurrentSwimmer(updated);

            setFormEditable(false);
            editing.set(false);
        });

        btnDelete.setOnAction(e -> {
            var app = AppState.get();
            var sel = app.getCurrentSwimmer();
            if (sel == null) return;

            var conf = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete swimmer “" + sel.getFirstName() + " " + sel.getLastName() + "”?",
                    ButtonType.OK, ButtonType.CANCEL);
            conf.setHeaderText("Confirm delete");
            conf.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.OK) {
                    app.getSwimmers().remove(sel);
                    try { LocalStore.deleteSwimmer(sel.getId()); } catch (Exception ignored) {}
                    app.setCurrentSwimmer(app.getSwimmers().isEmpty() ? null : app.getSwimmers().get(0));
                }
            });
        });
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------
    private void setFormEditable(boolean editable) {
        tfFirst.setEditable(editable);
        tfLast.setEditable(editable);
        tfTeam.setEditable(editable);
    }

    private void loadToForm(Swimmer s) {
        if (s == null) {
            tfFirst.setText("");
            tfLast.setText("");
            tfTeam.setText("");
            return;
        }
        tfFirst.setText(s.getFirstName());
        tfLast.setText(s.getLastName());
        tfTeam.setText(s.getTeamName());
    }

    private Swimmer applyEdits(Swimmer base) {
        if (base == null) return null;
        return new Swimmer(
                base.getId(),
                tfFirst.getText().trim(),
                tfLast.getText().trim(),
                /* preferredName (MVP removed) */ null,
                tfTeam.getText().trim(),
                base.getCreatedAt(),
                java.time.Instant.now()
        );
    }
}