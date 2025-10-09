package swimworkoutbuilder_javafx.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.store.LocalStore;
import swimworkoutbuilder_javafx.ui.dialogs.SwimmerFormDialog;

import java.util.ArrayList;
import java.util.List;

public class SwimmerChooserPane extends HBox {

    private final Label lbl = new Label("Swimmer:");
    private final ComboBox<Swimmer> cb = new ComboBox<>();

    // Synthetic "add new" sentinel (never saved)
    private final Swimmer ADD_NEW = new Swimmer("➕ Add New Swimmer…", " ", null, null);

    public SwimmerChooserPane() {
        super(8);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(6, 8, 6, 8));

        lbl.setPadding(new Insets(0, 6, 0, 0));
        cb.setPrefWidth(300);

        cb.setConverter(new StringConverter<>() {
            @Override public String toString(Swimmer s) {
                if (s == null) return "";
                if (s == ADD_NEW) return "➕ Add New Swimmer…";
                String team = (s.getTeamName()==null || s.getTeamName().isBlank()) ? "" : " ("+s.getTeamName()+")";
                return s.getFirstName() + " " + s.getLastName() + team;
            }
            @Override public Swimmer fromString(String s) { return null; }
        });

        // Button cell text
        cb.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Swimmer s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? "" : cb.getConverter().toString(s));
            }
        });

        // List cell with robust context menu
        cb.setCellFactory(v -> {
            ListCell<Swimmer> cell = new ListCell<>() {
                @Override protected void updateItem(Swimmer s, boolean empty) {
                    super.updateItem(s, empty);
                    if (empty || s == null) { setText(null); setContextMenu(null); return; }
                    setText(cb.getConverter().toString(s));

                    if (s == ADD_NEW) { setContextMenu(null); return; }

                    MenuItem miEdit = new MenuItem("Edit swimmer…");
                    miEdit.setOnAction(e -> editSwimmerInPlace(s));

                    MenuItem miDelete = new MenuItem("Delete swimmer…");
                    miDelete.setOnAction(e -> confirmDeleteSwimmer(s));

                    ContextMenu cm = new ContextMenu(miEdit, miDelete);
                    setContextMenu(cm);
                }
            };

            // Left click selects
            cell.setOnMouseClicked(me -> {
                if (me.getButton() == MouseButton.PRIMARY && !cell.isEmpty()) {
                    cb.getSelectionModel().select(cell.getItem());
                }
            });

            // Right-click / context request: ensure row is selected, then show menu
            cell.setOnContextMenuRequested(ev -> {
                if (cell.isEmpty()) return;
                cb.getSelectionModel().select(cell.getItem());
                if (cell.getContextMenu() != null) {
                    cell.getContextMenu().show(cell, ev.getScreenX(), ev.getScreenY());
                    ev.consume();
                }
            });

            return cell;
        });

        // Populate list from disk (ADD_NEW first)
        List<Swimmer> swimmers = new ArrayList<>();
        swimmers.add(ADD_NEW);
        swimmers.addAll(LocalStore.listSwimmers());
        cb.getItems().setAll(swimmers);

        // Selection behavior
        cb.setOnAction(e -> {
            Swimmer sel = cb.getSelectionModel().getSelectedItem();
            if (sel == null) return;

            if (sel == ADD_NEW) {
                var created = SwimmerFormDialog.show();
                if (created != null) {
                    try { LocalStore.saveSwimmer(created); } catch (Exception ignored) {}
                    var items = new ArrayList<>(cb.getItems());
                    items.add(1, created);
                    cb.getItems().setAll(items);
                    cb.getSelectionModel().select(created);
                    AppState.get().setCurrentSwimmer(created);
                } else {
                    var current = AppState.get().getCurrentSwimmer();
                    if (current != null) cb.getSelectionModel().select(current);
                    else cb.getSelectionModel().clearSelection();
                }
                return;
            }
            AppState.get().setCurrentSwimmer(sel);
        });

        // Keep chooser synced if current swimmer changes elsewhere
        AppState.get().currentSwimmerProperty().addListener((obs, o, s) -> {
            if (s == null) return;
            if (!cb.getItems().contains(s)) cb.getItems().add(s);
            cb.getSelectionModel().select(s);
        });

        // Initial selection: use resumed state if present
        var cur = AppState.get().getCurrentSwimmer();
        if (cur != null) cb.getSelectionModel().select(cur);
        else if (swimmers.size() > 1) cb.getSelectionModel().select(1);
        else cb.getSelectionModel().select(ADD_NEW);

        getChildren().addAll(lbl, cb);
    }

    private void editSwimmerInPlace(Swimmer s) {
        var updated = SwimmerFormDialog.show(s);
        if (updated != null) {
            try { LocalStore.saveSwimmer(updated); } catch (Exception ignored) {}
            // Replace list item text by re-setting the items (forces cell refresh)
            var items = new ArrayList<>(cb.getItems());
            int idx = items.indexOf(s);
            if (idx >= 0) items.set(idx, updated);
            cb.getItems().setAll(items);
            if (s.equals(AppState.get().getCurrentSwimmer())) {
                AppState.get().setCurrentSwimmer(updated);
            }
            cb.getSelectionModel().select(updated);
        }
    }

    private void confirmDeleteSwimmer(Swimmer s) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete swimmer \"" + s.getFirstName() + " " + s.getLastName() + "\"?\n" +
                        "This will also delete their saved workouts.",
                ButtonType.CANCEL, ButtonType.OK);
        a.setHeaderText("Delete swimmer");
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                LocalStore.deleteSwimmer(s.getId());
                if (s.equals(AppState.get().getCurrentSwimmer())) {
                    AppState.get().setCurrentSwimmer(null);
                    AppState.get().setCurrentWorkout(null);
                }
                cb.getItems().remove(s);
                if (cb.getItems().size() > 1) {
                    cb.getSelectionModel().select(
                            cb.getItems().get(0) == ADD_NEW ? cb.getItems().get(1) : cb.getItems().get(0)
                    );
                } else {
                    cb.getSelectionModel().select(ADD_NEW);
                }
            }
        });
    }
}