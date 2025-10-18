package swimworkoutbuilder_javafx.ui.workout;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.application.Platform;

import swimworkoutbuilder_javafx.model.SetGroup;
import swimworkoutbuilder_javafx.model.SwimSet;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.model.enums.Course;
import swimworkoutbuilder_javafx.model.units.TimeSpan;
import swimworkoutbuilder_javafx.model.pacing.PacePolicy;
import swimworkoutbuilder_javafx.model.pacing.DefaultPacePolicy;
import swimworkoutbuilder_javafx.model.enums.Equipment;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ScrollPane;

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

    private final VBox root = new VBox(10);
    private final VBox groupsBox = new VBox(12);
    private final ScrollPane groupsScroll = new ScrollPane();

    public WorkoutBuilderPane(WorkoutBuilderPresenter presenter) {
        this.presenter = java.util.Objects.requireNonNull(presenter, "presenter");
        buildUI();
        wire();
        refresh();
    }

    public Node node() { return root; }

    private void buildUI() {
        root.setPadding(new Insets(4, 10, 6, 10));
        root.setSpacing(2);
        root.setFillWidth(true);
        root.getStyleClass().add("surface");

        // Configure scroll area to keep header fixed while sets scroll
        groupsBox.setFillWidth(true);
        groupsScroll.setContent(groupsBox);
        groupsScroll.setFitToWidth(true);
        groupsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        groupsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        groupsScroll.setPannable(true);
        groupsScroll.getStyleClass().add("column-transparent");
        // Remove scroll chrome/borders and make background transparent
        groupsScroll.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        Platform.runLater(() -> {
            Node vp = groupsScroll.lookup(".viewport");
            if (vp != null) {
                vp.setStyle("-fx-background-color: transparent;");
            }
        });

        Button btnAddGroup = new Button("+ Add Group");
        btnAddGroup.getStyleClass().setAll("button","primary","sm");

        btnAddGroup.setOnAction(e -> {
            SetGroup created = SetGroupFormDialog.show(null);
            if (created != null) {
                presenter.addGroup(created.getName(), created.getReps(), created.getNotes());
            }
        });

        HBox header = new HBox(8, btnAddGroup);
        header.getStyleClass().add("wb-header-compact");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 4, 0));

        root.getChildren().addAll(header, groupsScroll);
        VBox.setVgrow(groupsScroll, Priority.ALWAYS);
    }

    private void wire() {
        ReadOnlyIntegerProperty tick = presenter.refreshTickProperty();
        if (tick != null) {
            tick.addListener((obs, o, n) -> refresh());
        }

        presenter.groups().addListener(
                (javafx.collections.ListChangeListener<SetGroup>) change -> refresh()
        );
    }

    private void refresh() {
        groupsBox.getChildren().clear();
        groupsBox.setSpacing(10);

        java.util.List<SetGroup> groups = presenter.groups();
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
            SetGroup edited = SetGroupFormDialog.show(g);
            if (edited != null) {
                g.setName(edited.getName());
                g.setReps(edited.getReps());
                g.setNotes(edited.getNotes());
                refresh();
            }
        });

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
        btnUp.setOnAction(e -> presenter.moveGroupUp(gi));

        Button btnDown = new Button();
        btnDown.getStyleClass().setAll("button","secondary","sm","icon");
        btnDown.setGraphic(Icons.make("move-down-swim-text", 16));
        btnDown.setTooltip(new Tooltip("Move group down"));
        btnDown.setOnAction(e -> presenter.moveGroupDown(gi));

        btnUp.managedProperty().bind(btnUp.visibleProperty());
        btnDown.managedProperty().bind(btnDown.visibleProperty());
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

   // Render a single formatted set row
    private Node renderSetRow(int gi, int si, SwimSet set) {
        Label lbl = new Label(formatSetMain(set));
        lbl.getStyleClass().add("label-set-primary");

        String notesText = formatSetNotes(set);
        Label notesLbl = new Label(notesText);
        notesLbl.getStyleClass().add("set-notes");
        notesLbl.setVisible(!notesText.isBlank());
        notesLbl.setManaged(!notesText.isBlank());

        Node eqIcons = equipmentIcons(set.getEquipment());

        Button btnEdit = new Button();
        btnEdit.getStyleClass().setAll("button","secondary","sm","icon");
        btnEdit.setGraphic(Icons.make("pencil-swim-text", 16));
        btnEdit.setTooltip(new Tooltip("Edit set"));
        btnEdit.setOnAction(e ->
                SetFormDialog.show(set).ifPresent(edited -> presenter.replaceSet(gi, si, edited)));

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

        btnUp.managedProperty().bind(btnUp.visibleProperty());
        btnDown.managedProperty().bind(btnDown.visibleProperty());

        int setCount = 0;
        java.util.List<SetGroup> allGroups = presenter.groups();
        if (allGroups != null && gi >= 0 && gi < allGroups.size()) {
            java.util.List<SwimSet> sets = allGroups.get(gi).getSets();
            if (sets != null) setCount = sets.size();
        }
        applyNavVisibility(btnUp, btnDown, si, setCount);

        HBox row = new HBox(6, lbl, notesLbl, spacer(), btnEdit, btnUp, btnDown, btnDelete);
        row.getStyleClass().addAll("wb-set-row","row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static Region spacer() { Region r = new Region(); HBox.setHgrow(r, Priority.ALWAYS); return r; }

    private static void applyNavVisibility(Button up, Button down, int index, int size) {
        boolean showUp   = index > 0;
        boolean showDown = index < size - 1;
        up.setVisible(showUp);
        down.setVisible(showDown);
        if (!up.managedProperty().isBound()) up.setManaged(showUp);
        if (!down.managedProperty().isBound()) down.setManaged(showDown);
    }

    private static String formatGroupTitle(SetGroup g) {
        String name = (g.getName() == null || g.getName().isBlank()) ? "Group" : g.getName().trim();
        int reps = Math.max(1, g.getReps());
        String notes = (g.getNotes() == null || g.getNotes().isBlank()) ? "" : " — " + g.getNotes().trim();
        return name + (reps > 1 ? " ×" + reps : "") + notes;
    }

    private static String mmss(int seconds) {
        int m = Math.max(0, seconds) / 60;
        int s = Math.max(0, seconds) % 60;
        return String.format("%d:%02d", m, s);
    }

    private Node equipmentIcons(java.util.Set<Equipment> eq) {
        if (eq == null || eq.isEmpty()) {
            Region spacer = new Region();
            spacer.setManaged(false);
            spacer.setVisible(false);
            return spacer;
        }
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        for (Equipment e : eq) {
            String file = iconFileFor(e);
            if (file == null) continue;
            String path = "/images/" + file;
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is == null) continue;
                Image img = new Image(is);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(20);
                iv.setFitHeight(20);
                iv.setPreserveRatio(true);
                iv.getStyleClass().add("equipment-icon");
                Tooltip.install(iv, new Tooltip(e.getLabel()));
                box.getChildren().add(iv);
            } catch (Exception ignored) { }
        }
        return box;
    }

    private String iconFileFor(Equipment e) {
        switch (e) {
            case FINS:        return "fins.png";
            case PADDLES:     return "paddles.png";
            case KICK_BOARD:  return "kick_board.png";
            case PULL_BUOY:   return "pull_buoy.png";
            case SNORKEL:     return "snorkel.png";
            case PARACHUTE:   return "parachute.png";
            case DRAG_SOCKS:  return "Drag_Socks.png";
            default:          return null;
        }
    }

    // Main line: "1 x 400 yd Freestyle on 9:40 (goal: 8:16) - EASY"
    private static String formatSetMain(SwimSet s) {
        int reps = Math.max(1, s.getReps());
        boolean meters = (s.getCourse() == Course.SCM || s.getCourse() == Course.LCM);
        long amount = meters
                ? Math.round(s.getDistancePerRep().toMeters())
                : Math.round(s.getDistancePerRep().toYards());
        String unit   = meters ? " m" : " yd";
        String stroke = (s.getStroke() == null) ? "—" : s.getStroke().getLabel();
        String effort = (s.getEffort() == null) ? "" : " - " + s.getEffort().name();

        // Compute Int/Goal (fall back to blanks if unavailable)
        String onTxt = "";
        String goalTxt = "";
        Workout w = AppState.get().getCurrentWorkout();
        Swimmer swimmer = AppState.get().getCurrentSwimmer();
        if (w != null && swimmer != null && s.getStroke() != null && s.getEffort() != null) {
            try {
                int goalSec     = (s.getGoalTime() != null)
                        ? (int) Math.round(s.getGoalTime().toSeconds())
                        : (int) Math.round(POLICY.goalSeconds(w, s, swimmer, 0));
                int intervalSec = POLICY.intervalSeconds(w, s, swimmer, 0);
                onTxt   = " on " + mmss(intervalSec);
                goalTxt = " (goal: " + mmss(goalSec) + ")";
            } catch (Throwable ignored) { }
        }

        // e.g., "1 x 400 yd Freestyle on 9:40 (goal: 8:16) - EASY"
        return reps + " x " + amount + unit + " " + stroke + onTxt + goalTxt + effort;
    }

    // Notes only (no leading/trailing spaces if empty)
    private static String formatSetNotes(SwimSet s) {
        if (s.getNotes() == null || s.getNotes().isBlank()) return "";
        return "- " + s.getNotes().trim();
    }
}
