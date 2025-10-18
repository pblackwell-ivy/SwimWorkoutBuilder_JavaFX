package swimworkoutbuilder_javafx.ui.workout;

import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.*;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.model.enums.Course;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.ui.DateFmt;
import swimworkoutbuilder_javafx.ui.Icons;

/**
 * WorkoutHeaderPane
 *
 * Visual + interaction model for the workout header area.
 *
 * Layout philosophy:
 * - Row A (global bar):  "Current Workout"  [Unsaved chip]   …………………  [Save] [Delete]
 *   • Save/Delete here apply to the ENTIRE workout (header + groups/sets).
 *   • The Unsaved chip reflects the presenter's dirty flag.
 * - Row B: Name + header edit actions (Edit/Cancel/Save) — these affect ONLY name/notes/course.
 *   • Header Save applies edits to the model and marks the workout dirty, but does not persist.
 * - Row C: Notes (view/edit swap)
 * - Row D: Course radios on the left, stats chips on the right
 * - Row E: Timestamps
 */
public final class WorkoutHeaderPane {

    private final VBox root = new VBox(6);
    private final AppState app;

    // --- view widgets ---
    private final Label titleBarLabel = new Label("Current Workout");
    private final Label chipUnsaved   = new Label("Unsaved");

    private final Button btnSaveGlobal = new Button();   // persists entire workout
    private final Button btnDelete     = new Button();

    // Header (name/notes) view/edit swap
    private final Label lblName  = new Label();
    private final Label lblNotes = new Label();

    private final TextField tfName  = new TextField();
    private final TextField tfNotes = new TextField();

    private final Button btnEditHeader   = new Button();
    private final Button btnSaveHeader   = new Button();
    private final Button btnCancelHeader = new Button();

    // Course radios
    private final ToggleGroup tgCourse = new ToggleGroup();
    private final RadioButton rbSCY = new RadioButton("25 yd");
    private final RadioButton rbSCM = new RadioButton("25 m");
    private final RadioButton rbLCM = new RadioButton("50 m");

    // Stats
    private final Label statDistance = new Label("—");
    private final Label statDuration = new Label("—");
    private final Label statSwim     = new Label("—");
    private final Label statRest     = new Label("—");

    // Timestamps
    private final Label lblTimestamps = new Label();

    // External
    private WorkoutBuilderPresenter presenter;
    private boolean editingHeader = false;

    // Local dirty flag for header UI (decoupled from presenter impl)
    private final BooleanProperty headerDirty = new SimpleBooleanProperty(false);

    public WorkoutHeaderPane(AppState app) {
        this.app = Objects.requireNonNull(app, "app");
        buildUI();
        wireAppState();
        refreshFrom(app.getCurrentWorkout());
    }

    public Node node() { return root; }

    /** Bind a presenter so we can reflect live stats and dirty state. */
    public void bindPresenter(WorkoutBuilderPresenter p) {
        // --- Unbind any prior presenter-related bindings ---
        try { chipUnsaved.visibleProperty().unbind(); } catch (Exception ignored) {}
        try { btnSaveGlobal.visibleProperty().unbind(); } catch (Exception ignored) {}
        try { statDistance.textProperty().unbind(); } catch (Exception ignored) {}
        try { statDuration.textProperty().unbind(); } catch (Exception ignored) {}
        try { statSwim.textProperty().unbind(); } catch (Exception ignored) {}
        try { statRest.textProperty().unbind(); } catch (Exception ignored) {}

        this.presenter = p;
        if (p == null) return;

        // Reset UI dirty on new bind
        headerDirty.set(false);

        // Unsaved chip + global Save now reflect this local flag
        chipUnsaved.visibleProperty().bind(headerDirty);
        btnSaveGlobal.visibleProperty().bind(headerDirty);

        // --- Bind stats labels directly to presenter string properties, but format time as mm:ss ---
        statDistance.textProperty().bind(p.totalDistanceTextProperty());
        statDuration.textProperty().bind(Bindings.createStringBinding(
                () -> mmssOnly(p.durationTextProperty().get()),
                p.durationTextProperty()));
        statSwim.textProperty().bind(Bindings.createStringBinding(
                () -> mmssOnly(p.swimTimeTextProperty().get()),
                p.swimTimeTextProperty()));
        statRest.textProperty().bind(Bindings.createStringBinding(
                () -> mmssOnly(p.restTimeTextProperty().get()),
                p.restTimeTextProperty()));

        // Any structural workout change → mark dirty (header shows Unsaved)
        p.refreshTickProperty().addListener((o, a, b) -> headerDirty.set(true));
    }

    // ------------------------------------------------------------------
    // UI
    // ------------------------------------------------------------------
    private void buildUI() {
        root.setPadding(new Insets(8, 12, 8, 12));
        root.getStyleClass().setAll("card");

        // Styles
        titleBarLabel.getStyleClass().add("card-title");
        lblName.getStyleClass().add("workout-title");
        lblNotes.getStyleClass().add("workout-notes");

        chipUnsaved.getStyleClass().add("chip");
        chipUnsaved.setVisible(false);
        chipUnsaved.managedProperty().bind(chipUnsaved.visibleProperty());

        // Global actions
        btnSaveGlobal.getStyleClass().setAll("button","primary","sm"); // NOT "icon" (we want filled button)
        btnSaveGlobal.setGraphic(Icons.make("save-white", 16));
        btnSaveGlobal.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnSaveGlobal.setTooltip(new Tooltip("Save workout (⌘S / Ctrl+S)"));
        btnSaveGlobal.setVisible(false);
        btnSaveGlobal.managedProperty().bind(btnSaveGlobal.visibleProperty());

        btnDelete.getStyleClass().setAll("button","danger","sm","icon");
        btnDelete.setGraphic(Icons.make("trash-2-danger", 16));
        btnDelete.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnDelete.setTooltip(new Tooltip("Delete workout"));

        // Header edit actions
        btnEditHeader.getStyleClass().setAll("button","secondary","sm","icon");
        btnSaveHeader.getStyleClass().setAll("button","secondary","sm","icon");
        btnCancelHeader.getStyleClass().setAll("button","secondary","sm","icon");
        btnEditHeader.setGraphic(Icons.make("pencil-swim-text", 16));
        btnSaveHeader.setGraphic(Icons.make("save-swim-text", 16));
        btnCancelHeader.setGraphic(Icons.make("circle-x-swim-text", 16));
        btnEditHeader.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnSaveHeader.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnCancelHeader.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        for (Button b : new Button[]{btnSaveGlobal, btnDelete, btnEditHeader, btnSaveHeader, btnCancelHeader}) {
            b.setFocusTraversable(false);
        }

        // Row A: Global title + unsaved chip + global buttons
        HBox leftA = new HBox(8, titleBarLabel, chipUnsaved);
        leftA.setAlignment(Pos.CENTER_LEFT);
        Region spacerA = new Region(); HBox.setHgrow(spacerA, Priority.ALWAYS);
        HBox rightA = new HBox(8, btnSaveGlobal, btnDelete);
        rightA.setAlignment(Pos.CENTER_RIGHT);
        HBox rowA = new HBox(10, leftA, spacerA, rightA);
        rowA.setAlignment(Pos.CENTER_LEFT);
        rowA.getStyleClass().setAll("card-header");

        // Row B: Name with header edit controls
        HBox leftB = new HBox(6, wrapSwap(lblName, tfName));
        leftB.setAlignment(Pos.CENTER_LEFT);
        Region spacerB = new Region(); HBox.setHgrow(spacerB, Priority.ALWAYS);
        HBox headerActionsView = new HBox(8, btnEditHeader);
        HBox headerActionsEdit = new HBox(8, btnCancelHeader, btnSaveHeader);
        headerActionsEdit.setVisible(false);
        headerActionsEdit.managedProperty().bind(headerActionsEdit.visibleProperty());
        headerActionsView.managedProperty().bind(headerActionsView.visibleProperty());
        HBox rowB = new HBox(10, leftB, spacerB, headerActionsView, headerActionsEdit);
        rowB.setAlignment(Pos.CENTER_LEFT);

        // Row C: Notes (swap)
        HBox rowC = new HBox(wrapSwap(lblNotes, tfNotes));
        rowC.setAlignment(Pos.CENTER_LEFT);

        // Row D: course + stats
        rbSCY.setToggleGroup(tgCourse);
        rbSCM.setToggleGroup(tgCourse);
        rbLCM.setToggleGroup(tgCourse);
        HBox radios = new HBox(10, new Label("Pool length:"), rbSCY, rbSCM, rbLCM);
        radios.setAlignment(Pos.CENTER_LEFT);

        HBox stats = new HBox(14, chip(statDistance), sep(), chip(statDuration), sep(), chip(statSwim), sep(), chip(statRest));
        stats.setAlignment(Pos.CENTER_RIGHT);

        Region spacerD = new Region(); HBox.setHgrow(spacerD, Priority.ALWAYS);
        HBox rowD = new HBox(10, radios, spacerD, stats);
        rowD.setAlignment(Pos.CENTER_LEFT);
        rowD.getStyleClass().add("toolbar");

        // Row E: timestamps
        lblTimestamps.getStyleClass().add("muted");
        HBox rowE = new HBox(lblTimestamps);
        rowE.setAlignment(Pos.CENTER_LEFT);

        // Edit visibility defaults
        tfName.setPromptText("Workout name");
        tfNotes.setPromptText("Notes (optional)");
        tfName.setVisible(false); tfName.setManaged(false);
        tfNotes.setVisible(false); tfNotes.setManaged(false);

        // Assemble
        root.getChildren().setAll(rowA, rowB, rowC, rowD, rowE);

        // Actions ------------------------------------------------------
        btnEditHeader.setOnAction(e -> enterHeaderEdit(headerActionsView, headerActionsEdit));
        btnCancelHeader.setOnAction(e -> { refreshFrom(app.getCurrentWorkout()); exitHeaderEdit(headerActionsView, headerActionsEdit); });
        btnSaveHeader.setOnAction(e -> onSaveHeader(headerActionsView, headerActionsEdit));
        btnDelete.setOnAction(e -> onDelete());
        btnSaveGlobal.setOnAction(e -> onSaveGlobal());

        // Course change applies immediately at the model level (marks dirty and re-computes)
        tgCourse.selectedToggleProperty().addListener((obs, o, n) -> {
            if (presenter == null) return;
            Course c = selectedCourse();
            if (c != null) presenter.changeCourse(c);
        });
    }

    private void wireAppState() {
        // Disable entire header only when no workout
        root.disableProperty().bind(Bindings.createBooleanBinding(
                () -> app.getCurrentWorkout() == null,
                app.currentWorkoutProperty()));

        // Refresh on workout switch
        app.currentWorkoutProperty().addListener((o, oldW, newW) -> {
            refreshFrom(newW);
            headerDirty.set(false); // a freshly loaded workout starts pristine
            // updateStats(); // no longer needed immediately after binding
        });
    }

    // ------------------------------------------------------------------
    // Header edit mode
    // ------------------------------------------------------------------
    private void enterHeaderEdit(HBox actionsView, HBox actionsEdit) {
        editingHeader = true;
        swapToEdit(true);
        actionsView.setVisible(false);
        actionsEdit.setVisible(true);
        tfName.requestFocus();
    }

    private void exitHeaderEdit(HBox actionsView, HBox actionsEdit) {
        editingHeader = false;
        swapToEdit(false);
        actionsEdit.setVisible(false);
        actionsView.setVisible(true);
    }

    private void swapToEdit(boolean on) {
        lblName.setVisible(!on); lblName.setManaged(!on);
        tfName.setVisible(on);  tfName.setManaged(on);
        lblNotes.setVisible(!on); lblNotes.setManaged(!on);
        tfNotes.setVisible(on);  tfNotes.setManaged(on);
    }

    /** Apply name/notes/course edits to the model and mark dirty (no disk write). */
    private void onSaveHeader(HBox actionsView, HBox actionsEdit) {
        if (presenter == null) { exitHeaderEdit(actionsView, actionsEdit); return; }
        Workout w = app.getCurrentWorkout();
        if (w == null) { exitHeaderEdit(actionsView, actionsEdit); return; }

        String name  = tfName.getText().trim();
        String notes = tfNotes.getText().trim();
        Course c     = selectedCourse();

        presenter.saveHeaderEdits(name, notes, c); // must mark dirty & tick inside
        headerDirty.set(true); // header edits change the workout; mark dirty
        updateStats();
        exitHeaderEdit(actionsView, actionsEdit);
        refreshFrom(app.getCurrentWorkout());
    }

    /** Persist ENTIRE workout; clears dirty and refreshes. */
    private void onSaveGlobal() {
        try {
            app.persistCurrentWorkout();
            // Clearing the dirty flag hides the chip and the Save button via bindings
            headerDirty.set(false);
            refreshFrom(app.getCurrentWorkout());
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR,
                    "Failed to save workout:\n" + ex.getMessage(), ButtonType.OK)
                    .showAndWait();
        }
    }

    private void onDelete() {
        if (presenter == null || app.getCurrentWorkout() == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete this workout?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        confirm.setTitle("Confirm Delete");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            presenter.deleteCurrentWorkout();
        }
    }

    // ------------------------------------------------------------------
    // Refresh & helpers
    // ------------------------------------------------------------------
    private void refreshFrom(Workout w) {
        if (w == null) {
            lblName.setText("");
            lblNotes.setText("");
            tfName.clear();
            tfNotes.clear();
            tgCourse.selectToggle(null);
            lblTimestamps.setText("");
            updateStats();
            tfName.setVisible(false); tfName.setManaged(false);
            tfNotes.setVisible(false); tfNotes.setManaged(false);
            lblName.setVisible(true); lblName.setManaged(true);
            lblNotes.setVisible(true); lblNotes.setManaged(true);
            return;
        }

        String name  = nullToEmpty(w.getName());
        String notes = nullToEmpty(w.getNotes());
        lblName.setText(name);
        lblNotes.setText(notes.isBlank() ? " " : notes);
        tfName.setText(name);
        tfNotes.setText(notes);

        Course course = (w.getCourse() == null ? Course.SCY : w.getCourse());
        switch (course) {
            case SCY -> tgCourse.selectToggle(rbSCY);
            case SCM -> tgCourse.selectToggle(rbSCM);
            case LCM -> tgCourse.selectToggle(rbLCM);
        }

        updateStats();
        lblTimestamps.setText("Created " + DateFmt.local(w.getCreatedAt()) +
                "   •   Updated " + DateFmt.local(w.getUpdatedAt()));
    }

    private void updateStats() {
        // If labels are bound to presenter properties, let bindings drive the UI
        if (statDistance.textProperty().isBound()
                || statDuration.textProperty().isBound()
                || statSwim.textProperty().isBound()
                || statRest.textProperty().isBound()) {
            return;
        }

        if (presenter != null) {
            String dist = presenter.totalDistanceTextProperty().get();
            String dur  = presenter.durationTextProperty().get();
            String swim = presenter.swimTimeTextProperty().get();
            String rest = presenter.restTimeTextProperty().get();

            statDistance.setText(dist == null || dist.isBlank() ? "—" : dist);
            statDuration.setText(mmssOnly(dur));
            statSwim.setText(mmssOnly(swim));
            statRest.setText(mmssOnly(rest));
            return;
        }

        // Fallback to the current workout's own totals if presenter is not available
        Workout w = app.getCurrentWorkout();
        if (w == null) {
            statDistance.setText("—");
            statDuration.setText("—");
            statSwim.setText("—");
            statRest.setText("—");
        } else {
            statDistance.setText(w.totalDistance().toShortString());
            statDuration.setText("—");
            statSwim.setText("—");
            statRest.setText("—");
        }
    }

    private static HBox chip(Label l) {
        l.getStyleClass().add("metric-value");
        HBox box = new HBox(l);
        box.getStyleClass().add("metric-chip");
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private static Label sep() {
        Label s = new Label("|");
        s.getStyleClass().add("muted");
        return s;
    }

    private static VBox wrapSwap(Node viewNode, Node editNode) {
        VBox box = new VBox(viewNode, editNode);
        box.setSpacing(0);
        return box;
    }

    private Course selectedCourse() {
        Toggle t = tgCourse.getSelectedToggle();
        if (t == rbSCY) return Course.SCY;
        if (t == rbSCM) return Course.SCM;
        if (t == rbLCM) return Course.LCM;
        return null;
    }

    // Returns only mm:ss, or "—" if blank/null. Removes trailing .SSS if present.
    private static String mmssOnly(String s) {
        if (s == null || s.isBlank()) return "—";
        int dot = s.lastIndexOf('.');
        return (dot > 0 ? s.substring(0, dot) : s);
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
}
