package swimworkoutbuilder_javafx.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.util.Callback;

import swimworkoutbuilder_javafx.model.SetGroup;
import swimworkoutbuilder_javafx.model.SwimSet;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.model.enums.Course;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.model.units.TimeSpan;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.store.LocalStore;
import swimworkoutbuilder_javafx.ui.components.SwimmerChooserPane;
import swimworkoutbuilder_javafx.ui.dialogs.SetFormDialog;
import swimworkoutbuilder_javafx.ui.dialogs.SetGroupFormDialog;
import swimworkoutbuilder_javafx.ui.dialogs.SwimmerFormDialog;
import swimworkoutbuilder_javafx.ui.dialogs.WorkoutFormDialog;
import swimworkoutbuilder_javafx.ui.seeds.SeedGridPane;
import swimworkoutbuilder_javafx.ui.shell.ActionBar;

import java.util.List;
import java.util.Objects;

public class MainView extends BorderPane {

    // Left: header + tree + toolbar
    private final VBox leftPane = new VBox(8);
    private final Label hdrSummaryDistance = new Label("-");
    private final Label hdrSummaryDuration = new Label("Duration:");
    private final Label hdrSummarySwim = new Label("- swimming");
    private final Label hdrSummaryRest = new Label("- rest");
    private final Label hdrSummaryTotal = new Label("- total");
    private final Label hdrPool = new Label("Pool Length: -");

    private final TreeView<Object> workoutTree = new TreeView<>();
    private final Button btAddGroup = new Button("Add Group...");
    private final Button btAddSet = new Button("Add Set...");

    private final TitledPane leftHeader = new TitledPane();
    private final Label hdrWorkoutName = new Label("Workout: -");
    private final Label hdrWorkoutNotes = new Label("");

    // Course radios in the header
    private final ToggleGroup courseGroup = new ToggleGroup();
    private final RadioButton rbScy = new RadioButton("SCY");
    private final RadioButton rbScm = new RadioButton("SCM");
    private final RadioButton rbLcm = new RadioButton("LCM");

    // Right panel: seed editor
    private SeedGridPane seedPane;

    // Center placeholder (unused at the moment)
    private final StackPane centerPane = new StackPane(new Label(""));

    // Model state
    private Workout workout;
    private Swimmer currentSwimmer;

    public MainView() {
        buildLeftPane();
        buildRightPane();

        // Top action bar
        ActionBar ab = new ActionBar();
        setTop(ab.node());

        // React to global state changes
        AppState.get().currentSwimmerProperty().addListener((obs, o, s) -> setCurrentSwimmer(s));
        AppState.get().currentWorkoutProperty().addListener((obs, o, w) -> setWorkout(w));

        setLeft(leftPane);
        setCenter(centerPane);
        setRight(seedPane);

        setPadding(new Insets(8));
        refreshHeader();
        rebuildTree();

        // --- Autosave: persist last used swimmer/workout + the entities themselves ---
        AppState.get().currentSwimmerProperty().addListener((obs, o, s) -> {
            if (s != null) {
                try { LocalStore.saveSwimmer(s); } catch (Exception ignored) {}
                LocalStore.saveLast(
                        s.getId(),
                        AppState.get().getCurrentWorkout() == null ? null : AppState.get().getCurrentWorkout().getId()
                );
            }
        });
        AppState.get().currentWorkoutProperty().addListener((obs, o, w) -> {
            if (w != null) {
                try { LocalStore.saveWorkout(w); } catch (Exception ignored) {}
                LocalStore.saveLast(
                        AppState.get().getCurrentSwimmer() == null ? null : AppState.get().getCurrentSwimmer().getId(),
                        w.getId()
                );
            }
        });

        // --- Resume last session ---
        try {
            var lastS = LocalStore.lastSwimmer();
            if (lastS.isPresent()) {
                var s = LocalStore.loadSwimmer(lastS.get());
                AppState.get().setCurrentSwimmer(s);   // also push into global state
                setCurrentSwimmer(s);
            }
            var lastW = LocalStore.lastWorkout();
            if (lastW.isPresent()) {
                var w = LocalStore.loadWorkout(lastW.get());
                AppState.get().setCurrentWorkout(w);   // also push into global state
                setWorkout(w);
            }
        } catch (Exception ignored) {}
    }

    private void buildLeftPane() {
        // swimmer chooser above the summary card
        leftPane.getChildren().add(0, new SwimmerChooserPane());

        // header content
        VBox content = new VBox(6,
                hdrWorkoutName,
                hdrWorkoutNotes,
                new Label("Workout summary:"),
                hdrSummaryDistance,
                hdrSummaryDuration,
                hdrSummarySwim,
                hdrSummaryRest,
                hdrSummaryTotal,
                hdrPool
        );

        // course radios row
        rbScy.setToggleGroup(courseGroup);
        rbScm.setToggleGroup(courseGroup);
        rbLcm.setToggleGroup(courseGroup);
        rbScy.setSelected(true);
        HBox courseRow = new HBox(10, new Label("Course:"), rbScy, rbScm, rbLcm);
        courseRow.setPadding(new Insets(0, 0, 4, 0));
        content.getChildren().add(1, courseRow);

        // change handler -> update workout course
        courseGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (workout == null || newT == null) return;
            if (newT == rbScy)      workout.setCourse(Course.SCY);
            else if (newT == rbScm) workout.setCourse(Course.SCM);
            else if (newT == rbLcm) workout.setCourse(Course.LCM);
            refreshHeader();
            rebuildTree();
        });

        content.setPadding(new Insets(6));
        leftHeader.setText("Swimmer: -"); // title updated in refreshHeader()
        leftHeader.setContent(content);
        leftHeader.setExpanded(true);

        // tree
        workoutTree.setShowRoot(false);
        workoutTree.setCellFactory(makeCellFactory());
        workoutTree.setOnKeyPressed(ke -> {
            if (ke.getCode() == KeyCode.DELETE || ke.getCode() == KeyCode.BACK_SPACE) {
                var sel = workoutTree.getSelectionModel().getSelectedItem();
                if (sel == null) return;
                Object v = sel.getValue();
                if (v instanceof SwimSet s) deleteSet(s);
                else if (v instanceof SetGroup g) deleteGroup(g);
            }
        });

        HBox toolbar = new HBox(8, btAddGroup, btAddSet);
        btAddGroup.setOnAction(e -> handleAddGroup());
        btAddSet.setOnAction(e -> handleAddSet());

        VBox.setVgrow(workoutTree, Priority.ALWAYS);
        leftPane.getChildren().addAll(leftHeader, new Label("Workout"), workoutTree, toolbar);
        leftPane.setPadding(new Insets(8));
    }

    private void buildRightPane() {
        seedPane = new SeedGridPane();
        seedPane.setOnSeedsSaved(() -> {
            // when seeds change, recompute header timings
            refreshHeader();
            rebuildTree();
        });
        seedPane.setPadding(new Insets(8));
        seedPane.setPrefWidth(320);
        setRight(seedPane);
    }

    // ======= Tree building & formatting =======

    private void rebuildTree() {
        TreeItem<Object> root = new TreeItem<>("ROOT");
        if (workout != null) {
            for (SetGroup g : workout.getGroups()) {
                TreeItem<Object> gItem = new TreeItem<>(g);
                for (SwimSet s : g.getSets()) {
                    gItem.getChildren().add(new TreeItem<>(s));
                }
                gItem.setExpanded(true);
                root.getChildren().add(gItem);
            }
        }
        workoutTree.setRoot(root);
    }

    private Callback<TreeView<Object>, TreeCell<Object>> makeCellFactory() {
        return tv -> new TreeCell<>() {
            private final VBox card   = new VBox(2);
            private final HBox header = new HBox(6);
            private final Label title  = new Label();
            private final Label sub    = new Label();
            private final Region spacer = new Region();
            private final Button btnEdit     = new Button("Edit");
            private final Button btnAddSet   = new Button("Add Set");
            private final Button btnAddGroup = new Button("Add Group");
            private final ContextMenu cmenu = new ContextMenu();
            private final MenuItem miEdit          = new MenuItem("Edit...");
            private final MenuItem miAddSet        = new MenuItem("Add Set...");
            private final MenuItem miAddGroupAfter = new MenuItem("Add Group After...");
            private final MenuItem miDelete        = new MenuItem("Delete");

            {
                HBox.setHgrow(spacer, Priority.ALWAYS);
                header.getChildren().addAll(title, spacer, btnEdit);
                card.getChildren().setAll(header);

                btnEdit.setOnAction(e -> onEditClicked());
                btnAddSet.setOnAction(e -> onAddSetClicked());
                btnAddGroup.setOnAction(e -> onAddGroupClicked());

                miEdit.setOnAction(e -> onEditClicked());
                miAddSet.setOnAction(e -> onAddSetClicked());
                miAddGroupAfter.setOnAction(e -> onAddGroupClicked());
                miDelete.setOnAction(e -> onDeleteClicked());
            }

            private void onEditClicked() {
                Object v = getItem();
                if (v instanceof SetGroup g) {
                    SetGroup edited = SetGroupFormDialog.show(g);
                    if (edited != null) {
                        g.setName(edited.getName());
                        g.setReps(edited.getReps());
                        g.setNotes(edited.getNotes());
                        g.setRestAfterGroupSec(edited.getRestAfterGroupSec());
                        try { // if dialog exposes rest-between-sets, keep it; otherwise ignore
                            var m = SetGroup.class.getMethod("setRestBetweenSetsSec", int.class);
                            var gm = SetGroup.class.getMethod("getRestBetweenSetsSec");
                            m.invoke(g, (int)gm.invoke(edited));
                        } catch (Exception ignored) {}
                        rebuildTree();
                        refreshHeader();
                    }
                } else if (v instanceof SwimSet s) {
                    SwimSet edited = SetFormDialog.show(workout, s);
                    if (edited != null) {
                        s.setStroke(edited.getStroke());
                        s.setEffort(edited.getEffort());
                        s.setReps(edited.getReps());
                        s.setDistancePerRep(edited.getDistancePerRep());
                        s.setNotes(edited.getNotes());
                        s.setEquipment(edited.getEquipment());
                        s.setInterval(edited.getInterval());
                        s.setGoalTime(edited.getGoalTime());
                        rebuildTree();
                        refreshHeader();
                    }
                }
            }

            private void onAddSetClicked() {
                Object v = getItem();
                if (!(v instanceof SetGroup g)) return;
                SwimSet created = SetFormDialog.show(workout, null);
                if (created == null) return;
                g.addSet(created);
                rebuildTree();
                refreshHeader();
            }

            private void onAddGroupClicked() {
                Object v = getItem();
                if (!(v instanceof SetGroup anchor)) return;
                SetGroup created = SetGroupFormDialog.show(null);
                if (created == null) return;
                List<SetGroup> groups = workout.getGroups();
                int anchorIdx = groups.indexOf(anchor);
                groups.add(anchorIdx >= 0 ? anchorIdx + 1 : groups.size(), created);
                normalizeGroupOrders();
                rebuildTree();
                refreshHeader();
            }

            private void onDeleteClicked() {
                Object v = getItem();
                if (v instanceof SwimSet s) deleteSet(s);
                else if (v instanceof SetGroup g) deleteGroup(g);
            }

            @Override
            protected void updateItem(Object value, boolean empty) {
                super.updateItem(value, empty);
                setText(null);
                setGraphic(null);
                setContextMenu(null);
                if (empty || value == null) return;

                header.getChildren().setAll(title, spacer, btnEdit);
                card.getChildren().setAll(header);
                sub.setText("");

                if (value instanceof SetGroup g) {
                    String reps = (g.getReps() > 1) ? " (x" + g.getReps() + ")" : "";
                    title.setText(g.getName() + reps);
                    if (g.getNotes() != null && !g.getNotes().isBlank()) {
                        sub.setText(g.getNotes());
                        card.getChildren().add(sub);
                    }
                    header.getChildren().addAll(btnAddSet, btnAddGroup);
                    cmenu.getItems().setAll(miEdit, miAddSet, miAddGroupAfter, new SeparatorMenuItem(), miDelete);
                    setContextMenu(cmenu);
                    setGraphic(card);

                } else if (value instanceof SwimSet s) {
                    String strokeShort = (s.getStroke() == null) ? "" : s.getStroke().getShortLabel();
                    String effort = (s.getEffort() == null) ? "" : s.getEffort().getLabel();
                    String tl = s.getReps() + "x" + displayDistance(s) + " " + strokeShort;
                    if (!effort.isEmpty()) tl += ", " + effort;

                    // interval/goal snippet if present
                    String timing = "";
                    if (s.getInterval() != null || s.getGoalTime() != null) {
                        String at = (s.getInterval() == null) ? "?" : fmtMMSS(s.getInterval());
                        String goal = (s.getGoalTime() == null) ? "?" : fmtMMSS(s.getGoalTime());
                        timing = " @ " + at + " (goal " + goal + ")";
                    }
                    title.setText(tl + timing);

                    if (s.getNotes() != null && !s.getNotes().isBlank()) {
                        sub.setText(s.getNotes());
                        card.getChildren().add(sub);
                    }
                    cmenu.getItems().setAll(miEdit, new SeparatorMenuItem(), miDelete);
                    setContextMenu(cmenu);
                    setGraphic(card);
                } else {
                    setText(value.toString());
                }
            }
        };
    }

    private void refreshHeader() {
        String swimmerText = (currentSwimmer == null)
                ? "Swimmer: -"
                : "Swimmer: " + nameOrPreferred(currentSwimmer);

        leftHeader.setText(swimmerText);

        if (workout == null) {
            hdrWorkoutName.setText("Workout: -");
            hdrWorkoutNotes.setText("");
            hdrSummaryDistance.setText("-");
            hdrSummaryDuration.setText("Duration:");
            hdrSummarySwim.setText("- swimming");
            hdrSummaryRest.setText("- rest");
            hdrSummaryTotal.setText("- total");
            hdrPool.setText("Pool Length: -");
            return;
        }

        hdrWorkoutName.setText("Workout: " + workout.getName());
        hdrWorkoutNotes.setText(workout.getNotes() == null ? "" : workout.getNotes());

        Course c = workout.getCourse();
        Distance total = computeTotalDistance(workout);
        long restSec = estimateRestSeconds(workout);
        long swimSec = (currentSwimmer != null) ? estimateSwimSeconds(workout, currentSwimmer) : 0L;
        long totalSec = swimSec + restSec;

        hdrSummaryDistance.setText(fmtDistanceForCourse(total, c));
        hdrSummaryDuration.setText("Duration:");
        hdrSummarySwim.setText(fmtHMS(swimSec) + " swimming");
        hdrSummaryRest.setText(fmtHMS(restSec) + " rest");
        hdrSummaryTotal.setText(fmtHMS(totalSec) + " total");

        String pool = switch (c) { case SCY -> "25 yds"; case SCM -> "25 m"; case LCM -> "50 m"; };
        hdrPool.setText("Pool Length: " + pool);

        // sync radios to workout
        switch (c) {
            case SCY -> rbScy.setSelected(true);
            case SCM -> rbScm.setSelected(true);
            case LCM -> rbLcm.setSelected(true);
        }
    }

    // === Actions ===

    private void handleAddGroup() {
        if (workout == null) {
            new Alert(Alert.AlertType.WARNING, "No active workout. Create a workout first.").showAndWait();
            return;
        }
        SetGroup created = SetGroupFormDialog.show(null);
        if (created == null) return;
        workout.getGroups().add(created);
        normalizeGroupOrders();
        rebuildTree();
        refreshHeader();
    }

    private void handleAddSet() {
        var sel = workoutTree.getSelectionModel().getSelectedItem();
        if (sel == null || !(sel.getValue() instanceof SetGroup g)) {
            new Alert(Alert.AlertType.INFORMATION, "Select a group to add a set.").showAndWait();
            return;
        }
        if (currentSwimmer == null || !currentSwimmer.hasAnySeeds()) {
            new Alert(Alert.AlertType.WARNING, "Set seed times first (right panel).").showAndWait();
            return;
        }
        SwimSet created = SetFormDialog.show(workout, null);
        if (created == null) return;
        g.addSet(created);
        rebuildTree();
        refreshHeader();
    }

    // === Model helpers ===

    public void setWorkout(Workout workout) {
        this.workout = Objects.requireNonNull(workout);
        normalizeGroupOrders();
        rebuildTree();
        refreshHeader();
    }

    public void setCurrentSwimmer(Swimmer s) {
        this.currentSwimmer = s;
        if (seedPane != null) seedPane.bindSwimmer(s);
        refreshHeader();
    }

    private void normalizeGroupOrders() {
        if (workout == null) return;
        int order = 1;
        for (SetGroup g : workout.getGroups()) g.setOrder(order++);
    }

    private static Distance computeTotalDistance(Workout w) {
        double meters = 0.0;
        for (SetGroup g : w.getGroups()) {
            double perGroupMeters = 0.0;
            for (SwimSet s : g.getSets()) {
                perGroupMeters += s.getDistancePerRep().toMeters() * s.getReps();
            }
            meters += perGroupMeters * Math.max(g.getReps(), 1);
        }
        return Distance.ofMeters(meters);
    }

    /** Swim time from seed pace. If a set’s stroke seed is missing, it’s skipped. */
    private static long estimateSwimSeconds(Workout w, Swimmer swimmer) {
        if (w == null || swimmer == null) return 0L;
        double totalSeconds = 0.0;
        for (SetGroup g : w.getGroups()) {
            int groupReps = Math.max(1, g.getReps());
            double secondsOneGroup = 0.0;
            for (SwimSet s : g.getSets()) {
                double metersPerRep = s.getDistancePerRep().toMeters();
                double metersTotalSet = metersPerRep * Math.max(1, s.getReps());
                var seed = swimmer.getSeedTime(s.getStroke());
                if (seed == null) seed = swimmer.getSeedTime(swimworkoutbuilder_javafx.model.enums.StrokeType.FREESTYLE);
                if (seed == null) continue;
                double speedMps = seed.speedMps();
                if (speedMps > 0) secondsOneGroup += (metersTotalSet / speedMps);
            }
            totalSeconds += secondsOneGroup * groupReps;
        }
        return Math.round(totalSeconds);
    }

    /** Rest from (interval − goal) per rep, plus between-set and between-group rest. */
    private static long estimateRestSeconds(Workout w) {
        if (w == null) return 0L;
        long total = 0;
        for (SetGroup g : w.getGroups()) {
            int groupReps = Math.max(1, g.getReps());

            long perGroup = 0;

            // rest from interval/goal on each rep
            for (SwimSet s : g.getSets()) {
                int reps = Math.max(1, s.getReps());
                long perRep = 0;
                TimeSpan interval = s.getInterval();
                TimeSpan goal = s.getGoalTime();
                if (interval != null && goal != null) {
                    long i = interval.toMillis() / 1000;
                    long gsec = goal.toMillis() / 1000;
                    perRep = Math.max(0, i - gsec);
                }
                perGroup += perRep * reps;
            }

            // default rest between consecutive sets in the group (if your SetGroup has it)
            try {
                var getter = SetGroup.class.getMethod("getRestBetweenSetsSec");
                int betweenSets = (int) getter.invoke(g);
                int gaps = Math.max(0, g.getSetCount() - 1);
                perGroup += (long) Math.max(0, betweenSets) * gaps;
            } catch (Exception ignored) { /* field may not exist in your variant */ }

            // multiply by number of group repeats
            perGroup *= groupReps;

            // rest between group repeats
            perGroup += (long) Math.max(0, g.getRestAfterGroupSec()) * Math.max(0, groupReps - 1);

            total += perGroup;
        }
        return total;
    }

    private static String fmtHMS(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return (h > 0) ? String.format("%d:%02d:%02d", h, m, s)
                : String.format("%d:%02d", m, s);
    }

    private static String fmtMMSS(TimeSpan t) {
        if (t == null) return "?";
        long ms = t.toMillis();
        long m = ms / 60000;
        long s = (ms % 60000) / 1000;
        return String.format("%d:%02d", m, s);
    }

    private static String fmtDistanceForCourse(Distance d, Course c) {
        int yards  = (int) Math.round(d.toYards());
        int meters = (int) Math.round(d.toMeters());
        return switch (c) { case SCY -> yards + " yds"; case SCM, LCM -> meters + " m"; };
    }

    private String displayDistance(SwimSet s) {
        if (workout != null && workout.getCourse() == Course.SCY) {
            int yards = (int) Math.round(s.getDistancePerRep().toYards());
            int snapped = (int) Math.round(yards / 25.0) * 25;
            return snapped + "yd";
        }
        int meters = (int) Math.round(s.getDistancePerRep().toMeters());
        return meters + "m";
    }

    private static String nameOrPreferred(Swimmer s) {
        if (s == null) return "-";
        String base = s.getFirstName() + " " + s.getLastName();
        return (s.getPreferredName() == null || s.getPreferredName().isBlank())
                ? base
                : s.getPreferredName() + " (" + base + ")";
    }

    private boolean confirm(String title, String message) {
        var alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(title);
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void deleteSet(SwimSet s) {
        if (workout == null) return;
        for (SetGroup g : workout.getGroups()) {
            if (g.getSets().remove(s)) { rebuildTree(); refreshHeader(); return; }
        }
    }

    private void deleteGroup(SetGroup g) {
        if (workout == null) return;
        workout.getGroups().remove(g);
        normalizeGroupOrders();
        rebuildTree();
        refreshHeader();
    }
}