package swimworkoutbuilder_javafx.ui.swimmers;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.store.LocalStore;

public final class SwimmerSection {

    private final VBox root = new VBox(8);

    private final ComboBox<Swimmer> cbSwimmer = new ComboBox<>();
    private final Button btnAddSwimmer = makeIconButton("plus", "Add swimmer", this::onAddSwimmer);
    private final Button btnEdit       = makeIconButton("pencil", "Edit swimmer", () ->
            new Alert(Alert.AlertType.INFORMATION, "Edit Swimmer (coming soon)").showAndWait()
    );
    private final Button btnDelete     = makeIconButton("trash", "Delete swimmer", () ->
            new Alert(Alert.AlertType.INFORMATION, "Delete Swimmer (coming soon)").showAndWait()
    );

    private final Label lblName = new Label();
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
        root.setPadding(new Insets(6, 10, 6, 10));

        // Combo label formatting
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

        // Toolbar row
        HBox top = new HBox(8, new Label("Swimmer:"), cbSwimmer, btnAddSwimmer, btnEdit, btnDelete);
        HBox.setHgrow(cbSwimmer, Priority.ALWAYS);

        GridPane info = new GridPane();
        info.setHgap(8);
        info.setVgap(4);
        int r = 0;
        info.addRow(r++, new Label("Name:"), lblName);
        info.addRow(r++, new Label("Preferred:"), lblPreferred);
        info.addRow(r++, new Label("Team:"), lblTeam);
        info.addRow(r++, new Label("Created:"), lblCreated);
        info.addRow(r,   new Label("Updated:"), lblUpdated);

        root.getChildren().addAll(top, info);

        // initial disabled until swimmer selected
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);
    }

    // ---------------- State wiring ----------------

    private void wireState() {
        var app = AppState.get();

        cbSwimmer.setItems(app.getSwimmers());

        // two-way sync
        cbSwimmer.getSelectionModel().selectedItemProperty().addListener((o, oldV, s) -> {
            if (s != null && s != app.getCurrentSwimmer()) app.setCurrentSwimmer(s);
        });
        app.currentSwimmerProperty().addListener((o, oldV, s) -> {
            if (s != cbSwimmer.getValue()) cbSwimmer.setValue(s);
            refreshDetails(s);
        });

        // initial selection
        if (app.getCurrentSwimmer() != null) {
            cbSwimmer.setValue(app.getCurrentSwimmer());
            refreshDetails(app.getCurrentSwimmer());
        } else if (!app.getSwimmers().isEmpty()) {
            cbSwimmer.getSelectionModel().selectFirst();
        }

        // keep Edit/Delete disabled with no selection
        btnEdit.disableProperty().bind(cbSwimmer.valueProperty().isNull());
        btnDelete.disableProperty().bind(cbSwimmer.valueProperty().isNull());
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
        gp.setHgap(8); gp.setVgap(8); gp.setPadding(new Insets(10));
        gp.addRow(0, new Label("First:"), tfFirst);
        gp.addRow(1, new Label("Last:"),  tfLast);
        gp.addRow(2, new Label("Preferred:"), tfPref);
        gp.addRow(3, new Label("Team:"), tfTeam);
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
        lblName.setText(has ? joinNonBlank(s.getFirstName(), s.getLastName()) : "—");
        lblPreferred.setText(has && s.getPreferredName() != null && !s.getPreferredName().isBlank()
                ? s.getPreferredName() : "—");
        lblTeam.setText(has && s.getTeamName() != null && !s.getTeamName().isBlank()
                ? s.getTeamName() : "—");
        lblCreated.setText(has && s.getCreatedAt() != null ? s.getCreatedAt().toString() : "—");
        lblUpdated.setText(has && s.getUpdatedAt() != null ? s.getUpdatedAt().toString() : "—");
    }

    // ---------------- Helpers ----------------

    private static Button makeIconButton(String iconName, String tooltip, Runnable action) {
        Button b = new Button();
        b.getStyleClass().add("icon-button");   // style in CSS
        b.setMinSize(28, 28);
        b.setPrefSize(28, 28);
        b.setMaxSize(28, 28);

        ImageView iv = null;
        var url = SwimmerSection.class.getResource("/icons/" + iconName + ".png");
        if (url != null) {
            iv = new ImageView(new Image(url.toExternalForm(), 18, 18, true, true));
        }
        if (iv != null) b.setGraphic(iv); else b.setText("…"); // graceful fallback

        if (tooltip != null && !tooltip.isBlank()) {
            Tooltip.install(b, new Tooltip(tooltip));
            b.setAccessibleText(tooltip);
        }
        if (action != null) b.setOnAction(e -> action.run());
        return b;
    }

    private static String displayName(Swimmer s) {
        if (s == null) return "";
        String preferred = s.getPreferredName();
        String first = s.getFirstName();
        String last  = s.getLastName();
        String left  = (preferred != null && !preferred.isBlank()) ? preferred :
                (first != null ? first : "");
        return (left + ((last != null && !last.isBlank()) ? " " + last : "")).trim();
    }

    private static String joinNonBlank(String a, String b) {
        a = a == null ? "" : a.trim();
        b = b == null ? "" : b.trim();
        if (a.isEmpty()) return b;
        if (b.isEmpty()) return a;
        return a + " " + b;
    }
}