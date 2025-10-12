package swimworkoutbuilder_javafx.ui.swimmers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import swimworkoutbuilder_javafx.model.Swimmer;

/**
 * Simple, self-contained view for managing swimmers.
 * - Left: list of swimmers
 * - Right: form fields + actions (Add / Edit / Save / Cancel / Delete)
 *
 * This view is "dumb": it binds to {@link SwimmerPresenter} properties and
 * calls its methods. No persistence or domain logic lives here.
 */
public final class SwimmerPane {

    private final SwimmerPresenter presenter;

    // UI
    private final BorderPane root = new BorderPane();
    private final ListView<Swimmer> list = new ListView<>();

    private final TextField tfFirst   = new TextField();
    private final TextField tfLast    = new TextField();
    private final TextField tfPref    = new TextField();
    private final TextField tfTeam    = new TextField();

    private final Button btnAdd    = new Button("Add");
    private final Button btnEdit   = new Button("Edit");
    private final Button btnSave   = new Button("Save");
    private final Button btnCancel = new Button("Cancel");
    private final Button btnDelete = new Button("Delete");

    public SwimmerPane(SwimmerPresenter presenter) {
        this.presenter = presenter;
        buildUI();
        wireBindings();
        wireActions();
    }

    private void buildUI() {
        root.setPadding(new Insets(8));

        // Left list
        list.setPrefWidth(220);
        list.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(Swimmer s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); return; }
                String display = s.getFirstName() + " " + s.getLastName();
                if (s.getPreferredName() != null && !s.getPreferredName().isBlank()) {
                    display += "  (“" + s.getPreferredName() + "”)";
                }
                setText(display);
            }
        });
        root.setLeft(list);
        BorderPane.setMargin(list, new Insets(0, 8, 0, 0));

        // Right form
        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.addRow(0, new Label("First name:"), tfFirst);
        form.addRow(1, new Label("Last name:"),  tfLast);
        form.addRow(2, new Label("Preferred:"),  tfPref);
        form.addRow(3, new Label("Team:"),       tfTeam);

        HBox actions = new HBox(8, btnAdd, btnEdit, new Region(), btnCancel, btnSave, btnDelete);
        ((Region)actions.getChildren().get(2)).setMinWidth(0);
        HBox.setHgrow(actions.getChildren().get(2), Priority.ALWAYS);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox right = new VBox(10, form, actions);
        right.setFillWidth(true);
        root.setCenter(right);

        // Initial states
        btnSave.setDefaultButton(true);
        btnCancel.setCancelButton(true);
        setFormEditable(false);
    }

    private void wireBindings() {
        // List <-> presenter
        list.setItems(presenter.swimmers());
        presenter.selectedProperty().bind(list.getSelectionModel().selectedItemProperty());
        // Keep list selection in sync when presenter updates it programmatically
        presenter.selectedProperty().addListener((obs, o, n) -> {
            if (n != list.getSelectionModel().getSelectedItem()) {
                list.getSelectionModel().select(n);
            }
        });

        // Form fields
        tfFirst.textProperty().bindBidirectional(presenter.firstNameProperty());
        tfLast.textProperty().bindBidirectional(presenter.lastNameProperty());
        tfPref.textProperty().bindBidirectional(presenter.preferredNameProperty());
        tfTeam.textProperty().bindBidirectional(presenter.teamNameProperty());

        // Enable/disable
        btnSave.disableProperty().bind(presenter.canSaveProperty().not());
        btnDelete.disableProperty().bind(presenter.canDeleteProperty().not());

        // Toggle editability by mode
        presenter.modeProperty().addListener((obs, oldM, m) -> {
            boolean editing = (m == SwimmerPresenter.Mode.ADDING || m == SwimmerPresenter.Mode.EDITING);
            setFormEditable(editing);
            btnAdd.setDisable(editing);
            btnEdit.setDisable(editing || presenter.selectedProperty().get() == null);
            btnCancel.setDisable(!editing);
        });
        // Initial
        boolean hasSel = presenter.selectedProperty().get() != null;
        btnEdit.setDisable(!hasSel);
        btnCancel.setDisable(true);
    }

    private void wireActions() {
        btnAdd.setOnAction(e -> presenter.beginAdd());
        btnEdit.setOnAction(e -> presenter.beginEdit());
        btnCancel.setOnAction(e -> presenter.cancel());
        btnSave.setOnAction(e -> presenter.save());
        btnDelete.setOnAction(e -> {
            var sel = presenter.getSelected();
            if (sel == null) return;
            var conf = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete swimmer “" + sel.getFirstName() + " " + sel.getLastName() + "”?",
                    ButtonType.OK, ButtonType.CANCEL);
            conf.setHeaderText("Confirm delete");
            conf.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.OK) presenter.deleteSelected();
            });
        });
    }

    private void setFormEditable(boolean editable) {
        tfFirst.setEditable(editable);
        tfLast.setEditable(editable);
        tfPref.setEditable(editable);
        tfTeam.setEditable(editable);
    }

    /** Returns the root node to embed in any scene/layout. */
    public Node node() { return root; }
}