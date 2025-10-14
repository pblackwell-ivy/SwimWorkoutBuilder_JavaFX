package swimworkoutbuilder_javafx.ui.dialogs;

import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.state.AppState;

import java.util.Optional;

public final class ManageSwimmersDialog {

    private ManageSwimmersDialog() {}

    public static Optional<Swimmer> show() {
        var app = AppState.get();

        Dialog<Swimmer> dlg = new Dialog<>();
        dlg.setTitle("Manage Swimmers");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        // Search
        TextField tfSearch = new TextField();
        tfSearch.setPromptText("Search swimmers…");

        // List
        ListView<Swimmer> list = new ListView<>();
        list.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(Swimmer s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); return; }
                String name = displayName(s);
                String team = (s.getTeamName() == null || s.getTeamName().isBlank()) ? "" : "  •  " + s.getTeamName();
                setText(name + team);
            }
        });

        FilteredList<Swimmer> filtered = new FilteredList<>(app.getSwimmers(), s -> true);
        list.setItems(filtered);

        tfSearch.textProperty().addListener((o, a, b) -> {
            String q = b == null ? "" : b.trim().toLowerCase();
            filtered.setPredicate(s -> {
                if (q.isEmpty()) return true;
                return displayName(s).toLowerCase().contains(q)
                        || safe(s.getTeamName()).toLowerCase().contains(q);
            });
        });

        // Select current swimmer by default
        if (app.getCurrentSwimmer() != null) {
            list.getSelectionModel().select(app.getCurrentSwimmer());
            list.scrollTo(app.getCurrentSwimmer());
        } else if (!filtered.isEmpty()) {
            list.getSelectionModel().selectFirst();
        }

        // Double-click to OK
        list.setOnMouseClicked(ev -> {
            if (ev.getClickCount() == 2 && list.getSelectionModel().getSelectedItem() != null) {
                dlg.setResult(list.getSelectionModel().getSelectedItem());
                dlg.close();
            }
        });

        // Enter to OK, Esc handled by CANCEL button type
        list.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.ENTER && list.getSelectionModel().getSelectedItem() != null) {
                dlg.setResult(list.getSelectionModel().getSelectedItem());
                dlg.close();
            }
        });

        // Layout
        Label lbl = new Label("Choose a swimmer");
        lbl.getStyleClass().add("section-title");

        HBox searchRow = new HBox(tfSearch);
        HBox.setHgrow(tfSearch, Priority.ALWAYS);

        VBox content = new VBox(10, lbl, searchRow, list);
        content.setPadding(new Insets(12));

        // Optional: give the dialog a “surface” look if your theme supports it
        content.getStyleClass().add("surface");

        dlg.getDialogPane().setContent(new BorderPane(content));
        dlg.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            return list.getSelectionModel().getSelectedItem();
        });

        // Focus search by default
        dlg.setOnShown(e -> tfSearch.requestFocus());

        return dlg.showAndWait();
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

    private static String safe(String x) { return x == null ? "" : x; }
}