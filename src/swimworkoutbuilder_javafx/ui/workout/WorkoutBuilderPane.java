package swimworkoutbuilder_javafx.ui.workout;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import swimworkoutbuilder_javafx.model.SetGroup;
import swimworkoutbuilder_javafx.model.SwimSet;

import java.util.List;
import java.util.Objects;

/**
 * Central “Workout Builder” pane.
 * Displays workout groups and their sets, allowing add/edit/delete operations.
 * Pure view layer; all logic is delegated to {@link WorkoutBuilderPresenter}.
 */
public final class WorkoutBuilderPane {

    private final WorkoutBuilderPresenter presenter;

    // Root layout containers
    private final VBox root = new VBox(10);
    private final VBox groupsBox = new VBox(12);

    public WorkoutBuilderPane(WorkoutBuilderPresenter presenter) {
        this.presenter = Objects.requireNonNull(presenter, "presenter");
        buildUI();
        wire();
        refresh();
    }

    public Node node() { return root; }

    // ---------------------------------------------------------------------
    // UI Construction
    // ---------------------------------------------------------------------
    private void buildUI() {
        root.setPadding(new Insets(10));
        root.setFillWidth(true);

        Button btnAddGroup = new Button("+ Add Group");
        btnAddGroup.setOnAction(e -> {
            TextInputDialog dlg = new TextInputDialog("New Group");
            dlg.setTitle("Add Group");
            dlg.setHeaderText(null);
            dlg.setContentText("Group name:");
            String name = dlg.showAndWait().orElse("").trim();
            if (!name.isEmpty()) presenter.addGroup(name);
        });

        HBox header = new HBox(8, btnAddGroup);
        header.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(header, new Separator(), groupsBox);
    }

    // ---------------------------------------------------------------------
    // Event Wiring
    // ---------------------------------------------------------------------
    private void wire() {
        ReadOnlyIntegerProperty tick = presenter.refreshTickProperty();
        if (tick != null) {
            tick.addListener((obs, o, n) -> refresh());
        }
    }

    // ---------------------------------------------------------------------
    // Rendering
    // ---------------------------------------------------------------------
    private void refresh() {
        groupsBox.getChildren().clear();

        List<SetGroup> groups = presenter.groups();
        if (groups == null || groups.isEmpty()) {
            Label empty = new Label("No groups yet. Click “+ Add Group” to begin.");
            groupsBox.getChildren().add(empty);
            return;
        }

        for (int gi = 0; gi < groups.size(); gi++) {
            SetGroup g = groups.get(gi);
            groupsBox.getChildren().add(renderGroup(gi, g));
        }
    }

    private Node renderGroup(int gi, SetGroup g) {
        Label lbl = new Label(formatGroupTitle(g));

        Button btnAdd = new Button("+ Set");
        btnAdd.setOnAction(e -> {
            SetFormDialog.show(null).ifPresent(set -> presenter.addSet(gi, set));
        });

        Button btnDel = new Button("🗑");
        btnDel.setTooltip(new Tooltip("Delete group"));
        btnDel.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete group \"" + g.getName() + "\"?", ButtonType.OK, ButtonType.CANCEL);
            confirm.setHeaderText(null);
            confirm.setTitle("Confirm Delete");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK)
                presenter.deleteGroup(gi);
        });

        Button btnUp = new Button("↑");
        btnUp.setOnAction(e -> presenter.moveGroupUp(gi));
        Button btnDown = new Button("↓");
        btnDown.setOnAction(e -> presenter.moveGroupDown(gi));

        HBox titleBar = new HBox(8, lbl, spacer(), btnAdd, btnUp, btnDown, btnDel);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.getStyleClass().add("wb-group-header");

        VBox setsBox = new VBox(6);
        if (g.getSets() != null) {
            for (int si = 0; si < g.getSets().size(); si++) {
                SwimSet s = g.getSets().get(si);
                setsBox.getChildren().add(renderSetRow(gi, si, s));
            }
        }

        VBox groupBox = new VBox(6, titleBar, setsBox, new Separator());
        groupBox.setPadding(new Insets(4, 0, 4, 0));
        groupBox.getStyleClass().add("wb-group");
        return groupBox;
    }

    private Node renderSetRow(int gi, int si, SwimSet s) {
        Label lbl = new Label(formatSetLine(s));

        Button btnEdit = new Button("✎");
        btnEdit.setTooltip(new Tooltip("Edit set"));
        btnEdit.setOnAction(e -> {
            SetFormDialog.show(s).ifPresent(newSet -> presenter.replaceSet(gi, si, newSet));
        });

        Button btnUp = new Button("↑");
        btnUp.setTooltip(new Tooltip("Move set up"));
        btnUp.setOnAction(e -> presenter.moveSetUp(gi, si));

        Button btnDown = new Button("↓");
        btnDown.setTooltip(new Tooltip("Move set down"));
        btnDown.setOnAction(e -> presenter.moveSetDown(gi, si));

        Button btnDelete = new Button("🗑");
        btnDelete.setTooltip(new Tooltip("Delete set"));
        btnDelete.setOnAction(e -> presenter.deleteSet(gi, si));

        HBox row = new HBox(8, lbl, spacer(), btnEdit, btnUp, btnDown, btnDelete);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("wb-set-row");
        return row;
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------
    private static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    private static String formatGroupTitle(SetGroup g) {
        String name = (g.getName() == null || g.getName().isBlank()) ? "Group" : g.getName().trim();
        int reps = Math.max(1, g.getReps());
        String notes = (g.getNotes() == null || g.getNotes().isBlank()) ? "" : " — " + g.getNotes().trim();
        return name + (reps > 1 ? " ×" + reps : "") + notes;
    }

    private static String formatSetLine(SwimSet s) {
        int reps = Math.max(1, s.getReps());
        long yards = Math.round(s.getDistancePerRep().toYards());
        String stroke = (s.getStroke() == null) ? "—" : s.getStroke().getLabel();
        String effort = (s.getEffort() == null) ? "" : " @" + s.getEffort().name();
        String notes = (s.getNotes() == null || s.getNotes().isBlank()) ? "" : " — " + s.getNotes().trim();
        return reps + "×" + yards + " yd " + stroke + effort + notes;
    }
}