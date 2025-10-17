package swimworkoutbuilder_javafx.ui.workout;

import java.util.List;
import java.util.Objects;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import swimworkoutbuilder_javafx.model.SetGroup;
import swimworkoutbuilder_javafx.model.SwimSet;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.model.enums.Course;
import swimworkoutbuilder_javafx.model.units.TimeSpan;
import swimworkoutbuilder_javafx.model.pacing.PacePolicy;
import swimworkoutbuilder_javafx.model.pacing.DefaultPacePolicy;

import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.ui.Icons;

/**
 * Central “Workout Builder” pane.
 * Displays workout groups and their sets, allowing add/edit/delete operations.
 * Pure view layer; all logic is delegated to {@link WorkoutBuilderPresenter}.
 */
public final class WorkoutBuilderPane {

    private final WorkoutBuilderPresenter presenter;
    private static final PacePolicy POLICY = new DefaultPacePolicy();

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
        root.setSpacing(8);
        root.setFillWidth(true);
        root.getStyleClass().add("surface");

        Button btnAddGroup = new Button("+ Add Group");
        btnAddGroup.getStyleClass().addAll("button","primary");

        // + Add Group
        btnAddGroup.setOnAction(e -> {
            SetGroup created = SetGroupFormDialog.show(null);
            if (created != null) {
                presenter.addGroup(created.getName(), created.getReps(), created.getNotes());
            }
        });

        HBox header = new HBox(8, btnAddGroup);
        header.getStyleClass().add("toolbar");
        header.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(header, new Separator(), groupsBox);
    }

    // ---------------------------------------------------------------------
    // Event Wiring
    // ---------------------------------------------------------------------
    private void wire() {
        // EXISTING: listen for explicit refresh ticks (e.g., add/delete/move set)
        ReadOnlyIntegerProperty tick = presenter.refreshTickProperty();
        if (tick != null) {
            tick.addListener((obs, o, n) -> refresh());
        }

        presenter.groups().addListener(
                (javafx.collections.ListChangeListener<SetGroup>) change -> refresh()
        );
    }

    // ---------------------------------------------------------------------
    // Rendering
    // ---------------------------------------------------------------------
    private void refresh() {
        groupsBox.getChildren().clear();
        groupsBox.setSpacing(10);

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
        lbl.getStyleClass().add("label-setgroup-name");

        Button btnAddSet = new Button("+ Set");
        btnAddSet.getStyleClass().addAll("button","secondary","sm");

        Button btnEdit = new Button();
        btnEdit.getStyleClass().setAll("button","secondary","sm","icon");
        btnEdit.setGraphic(Icons.make("pencil-swim-text", 16));
        btnEdit.setTooltip(new Tooltip("Edit group"));


        btnEdit.setOnAction(e -> {
            // Open dialog prefilled with the current group
            SetGroup edited = SetGroupFormDialog.show(g);
            if (edited != null) {
                // Apply edits back into the existing group instance
                g.setName(edited.getName());
                g.setReps(edited.getReps());
                g.setNotes(edited.getNotes());
                // Redraw this pane so the title reflects new values
                refresh();
            }
        });

        // Removed stray/duplicate titleBar declaration referencing undefined btnAdd

        btnAddSet.setOnAction(e -> SetFormDialog.show(null)
                .ifPresent(created -> presenter.addSet(gi, created)));

        Button btnDel = new Button();
        btnDel.getStyleClass().setAll("button","danger","sm","icon");
        btnDel.setGraphic(Icons.make("trash-2-danger", 16));
        btnDel.setTooltip(new Tooltip("Delete group"));
        btnDel.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete group \"" + g.getName() + "\"?", ButtonType.OK, ButtonType.CANCEL);
            confirm.setHeaderText(null);
            confirm.setTitle("Confirm Delete");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK)
                presenter.deleteGroup(gi);
        });

        Button btnUp = new Button();
        btnUp.getStyleClass().setAll("button","secondary","sm","icon");
        btnUp.setGraphic(Icons.make("move-up-swim-text", 16));
        btnUp.setTooltip(new Tooltip("Move group up"));

        Button btnDown = new Button();
        btnDown.getStyleClass().setAll("button","secondary","sm","icon");
        btnDown.setGraphic(Icons.make("move-down-swim-text", 16));
        btnDown.setTooltip(new Tooltip("Move group down"));
        btnDown.setOnAction(e -> presenter.moveGroupDown(gi));

        // Ensure layout doesn't reserve space when hidden
        btnUp.managedProperty().bind(btnUp.visibleProperty());
        btnDown.managedProperty().bind(btnDown.visibleProperty());
        // Recompute visibility for first/last group
        int groupCount = (presenter.groups() == null) ? 0 : presenter.groups().size();
        applyNavVisibility(btnUp, btnDown, gi, groupCount);

        HBox titleBar = new HBox(8, lbl, spacer(), btnAddSet, btnEdit, btnUp, btnDown, btnDel);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.getStyleClass().add("wb-group-header");

        VBox setsBox = new VBox(4);
        if (g.getSets() != null) {
            for (int si = 0; si < g.getSets().size(); si++) {
                SwimSet s = g.getSets().get(si);
                setsBox.getChildren().add(renderSetRow(gi, si, s));
            }
        }

        VBox groupBox = new VBox(4, titleBar, setsBox, new Separator());
        groupBox.setPadding(new Insets(4, 0, 4, 0));
        groupBox.getStyleClass().add("wb-group");
        return groupBox;
    }

    private Node renderSetRow(int gi, int si, SwimSet s) {
        Label lbl = new Label(formatSetLine(s));
        lbl.getStyleClass().add("label-set-primary");

        // --- icon buttons (graphics only) ---
        // --- icon buttons (graphics only) ---
// Use the same style pattern as SwimmerCard
        Button btnEdit = new Button();
        btnEdit.getStyleClass().setAll("button","secondary","sm","icon");
        btnEdit.setGraphic(Icons.make("pencil-swim-text", 16));
        btnEdit.setTooltip(new Tooltip("Edit set"));
        btnEdit.setOnAction(e ->
                SetFormDialog.show(s).ifPresent(edited -> presenter.replaceSet(gi, si, edited)));

        Button btnUp = new Button();
        btnUp.getStyleClass().setAll("button","secondary","sm","icon");
        btnUp.setGraphic(Icons.make("move-up-swim-text", 16));
        btnUp.setTooltip(new Tooltip("Move set up"));
        btnUp.setOnAction(e -> presenter.moveSetUp(gi, si));

        Button btnDown = new Button();
        btnDown.getStyleClass().setAll("button","secondary","sm","icon");
        btnDown.setGraphic(Icons.make("move-down-swim-text", 16));
        btnDown.setTooltip(new Tooltip("Move set down"));
        btnDown.setOnAction(e -> presenter.moveSetDown(gi, si));

        Button btnDelete = new Button();
        btnDelete.getStyleClass().setAll("button","danger","sm","icon");
        btnDelete.setGraphic(Icons.make("trash-2-danger", 16));
        btnDelete.setTooltip(new Tooltip("Delete set"));
        btnDelete.setOnAction(e -> presenter.deleteSet(gi, si));

        // Do not let layout reserve space when hidden
        btnUp.managedProperty().bind(btnUp.visibleProperty());
        btnDown.managedProperty().bind(btnDown.visibleProperty());

        // Determine how many sets are in this group right now
        int setCount = 0;
        List<SetGroup> allGroups = presenter.groups();
        if (allGroups != null && gi >= 0 && gi < allGroups.size()) {
            List<SwimSet> sets = allGroups.get(gi).getSets();
            if (sets != null) setCount = sets.size();
        }
        applyNavVisibility(btnUp, btnDown, si, setCount); // first/last visibility

        HBox row = new HBox(6, lbl, spacer(), btnEdit, btnUp, btnDown, btnDelete);
        row.getStyleClass().addAll("wb-set-row","row");
        row.setAlignment(Pos.CENTER_LEFT);
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

    // Show/hide and manage() in sync so layout doesn’t reserve space.
    private static void applyNavVisibility(Button up, Button down, int index, int size) {
        boolean showUp   = index > 0;
        boolean showDown = index < size - 1;

        // Set visibility always
        up.setVisible(showUp);
        down.setVisible(showDown);

        // Only set managed if it's not already bound (in some places we bind managed->visible)
        if (!up.managedProperty().isBound()) {
            up.setManaged(showUp);
        }
        if (!down.managedProperty().isBound()) {
            down.setManaged(showDown);
        }
    }

    private static String formatGroupTitle(SetGroup g) {
        String name = (g.getName() == null || g.getName().isBlank()) ? "Group" : g.getName().trim();
        int reps = Math.max(1, g.getReps());
        String notes = (g.getNotes() == null || g.getNotes().isBlank()) ? "" : " — " + g.getNotes().trim();
        return name + (reps > 1 ? " ×" + reps : "") + notes;
    }

    // Uses m/yd based on the set's course
    private static String formatSetLine(SwimSet s) {
        int reps = Math.max(1, s.getReps());
        boolean meters = (s.getCourse() == Course.SCM || s.getCourse() == Course.LCM);
        long amount = meters
                ? Math.round(s.getDistancePerRep().toMeters())
                : Math.round(s.getDistancePerRep().toYards());
        String unit = meters ? " m" : " yd";
        String stroke = (s.getStroke() == null) ? "—" : s.getStroke().getLabel();
        String effort = (s.getEffort() == null) ? "" : " @" + s.getEffort().name();

        // Compute Int/Goal with your policy
        Workout w = AppState.get().getCurrentWorkout();
        Swimmer swimmer = AppState.get().getCurrentSwimmer();
        String intervalTxt = "";
        String goalTxt     = "";

        if (w != null && swimmer != null && s.getStroke() != null && s.getEffort() != null) {
            try {
                int goalSec     = (s.getGoalTime() != null)
                        ? (int) Math.round(s.getGoalTime().toSeconds())
                        : (int) Math.round(POLICY.goalSeconds(w, s, swimmer, 0));
                int intervalSec = POLICY.intervalSeconds(w, s, swimmer, 0);

                intervalTxt = " – Int "  + TimeSpan.ofSeconds(intervalSec).toString();
                goalTxt     = " – Goal " + TimeSpan.ofSeconds(goalSec).toString();
            } catch (Throwable ignored) { /* leave blank if any seed missing */ }
        }

        String notes = (s.getNotes() == null || s.getNotes().isBlank()) ? "" : " — " + s.getNotes().trim();
        return reps + "×" + amount + unit + " " + stroke + effort + intervalTxt + goalTxt + notes;
    }
}
