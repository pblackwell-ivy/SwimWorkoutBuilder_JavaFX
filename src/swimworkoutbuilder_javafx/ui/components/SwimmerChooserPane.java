package swimworkoutbuilder_javafx.ui.components;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import javafx.util.StringConverter;

import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.ui.swimmers.SwimmerBarPresenter;

public final class SwimmerChooserPane extends HBox {

    private final ComboBox<Swimmer> combo = new ComboBox<>();
    private final Button btnAdd    = new Button("Add");
    private final Button btnEdit   = new Button("Edit");
    private final Button btnDelete = new Button("Delete");

    private final SwimmerBarPresenter presenter;

    public SwimmerChooserPane() {
        this(new SwimmerBarPresenter(AppState.get()));
    }

    public SwimmerChooserPane(SwimmerBarPresenter presenter) {
        this.presenter = presenter;
        buildUI();
        bind();
        wire();
        prettyPrintNames(); // <- ensure human-readable names
    }

    private void buildUI() {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(8);
        setPadding(new Insets(8));

        Label lbl = new Label("Swimmer:");
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setVisibleRowCount(10);

        // stop buttons shrinking to “...”
        btnAdd.setMinWidth(60);
        btnEdit.setMinWidth(60);
        btnDelete.setMinWidth(70);

        HBox.setHgrow(combo, Priority.ALWAYS);
        getChildren().addAll(lbl, combo, btnAdd, btnEdit, btnDelete);
    }

    private void bind() {
        combo.setItems(presenter.swimmers());

        // Safer than bindBidirectional for some SDK/IDE combos
        presenter.selectedProperty().addListener((o, oldS, newS) -> {
            if (combo.getValue() != newS) combo.setValue(newS);
        });
        combo.valueProperty().addListener((o, oldS, newS) -> {
            if (presenter.selectedProperty().get() != newS) presenter.selectedProperty().set(newS);
        });
        // (If you prefer, this utility also works:)
        // Bindings.bindBidirectional(combo.valueProperty(), presenter.selectedProperty());

        btnEdit.disableProperty().bind(combo.valueProperty().isNull());
        btnDelete.disableProperty().bind(combo.valueProperty().isNull());
    }

    private void wire() {
        btnAdd.setOnAction(e -> presenter.addNew());
        btnEdit.setOnAction(e -> presenter.editSelected());
        btnDelete.setOnAction(e -> presenter.deleteSelected());
    }

    /** Force human-readable names in BOTH the drop-down and the closed button cell. */
    private void prettyPrintNames() {
        // When the combo is closed
        combo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Swimmer s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? "" : displayName(s));
            }
        });

        // In the drop-down list
        combo.setCellFactory(new Callback<>() {
            @Override public ListCell<Swimmer> call(ListView<Swimmer> lv) {
                return new ListCell<>() {
                    @Override protected void updateItem(Swimmer s, boolean empty) {
                        super.updateItem(s, empty);
                        setText(empty || s == null ? "" : displayName(s));
                    }
                };
            }
        });

        // Extra belt-and-suspenders for some JavaFX builds
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(Swimmer s) { return s == null ? "" : displayName(s); }
            @Override public Swimmer fromString(String str) { return combo.getItems().stream()
                    .filter(sw -> displayName(sw).equals(str)).findFirst().orElse(null); }
        });
    }

    private static String displayName(Swimmer s) {
        String base = (s.getFirstName() + " " + s.getLastName()).trim();
        String pref = s.getPreferredName();
        return (pref == null || pref.isBlank()) ? base : pref + " (" + base + ")";
    }
}