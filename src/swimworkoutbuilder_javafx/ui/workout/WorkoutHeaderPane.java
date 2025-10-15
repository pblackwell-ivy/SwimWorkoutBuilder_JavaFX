package swimworkoutbuilder_javafx.ui.workout;


import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.model.enums.Course;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.ui.DateFmt;

/**
 * Workout header
 * Row 1: Title ............. [✎][🗑]
 * Row 2: Notes
 * Row 3: Pool length radios  | Distance | Duration | Swim | Rest |
 * Row 4: Created • Updated
 */
public final class WorkoutHeaderPane {

    private final VBox root = new VBox(10);
    private final AppState app;

    // Display labels (view mode)
    private final Label lblTitle = new Label();
    private final Label lblNotes = new Label();

    // Edit fields (edit mode)
    private final TextField tfTitle = new TextField();
    private final TextField tfNotes = new TextField();

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

    // Actions
    private final Button btnEdit   = new Button("✎");
    private final Button btnDelete = new Button("🗑");
    private final Button btnSave   = new Button("Save");
    private final Button btnCancel = new Button("Cancel");

    private boolean editing = false;
    private WorkoutBuilderPresenter presenter;

    public WorkoutHeaderPane(AppState app) {
        this.app = Objects.requireNonNull(app, "app");
        buildUI();
        wireAppState();
        refreshFrom(app.getCurrentWorkout());
    }

    public Node node() { return root; }

    /** Presenter binding for live stats + course changes. */
    public void bindPresenter(WorkoutBuilderPresenter p) {
        this.presenter = p;
        if (p == null) return;

        // Live redraw when groups/sets change
        p.refreshTickProperty().addListener((o, a, b) -> refreshFrom(app.getCurrentWorkout()));

        // Update stats when any computed text changes
        ReadOnlyStringProperty td = p.totalDistanceTextProperty();
        ReadOnlyStringProperty du = p.durationTextProperty();
        ReadOnlyStringProperty sw = p.swimTimeTextProperty();
        ReadOnlyStringProperty rs = p.restTimeTextProperty();

        td.addListener((o, a, b) -> updateStats(app.getCurrentWorkout()));
        du.addListener((o, a, b) -> updateStats(app.getCurrentWorkout()));
        sw.addListener((o, a, b) -> updateStats(app.getCurrentWorkout()));
        rs.addListener((o, a, b) -> updateStats(app.getCurrentWorkout()));

        // Initial stats
        updateStats(app.getCurrentWorkout());
    }

    // ------------------------------------------------------------------
    // UI
    // ------------------------------------------------------------------
    private void buildUI() {
        root.setPadding(new Insets(10));
        root.getStyleClass().add("surface");

        // Styles
        lblTitle.getStyleClass().add("workout-title");
        lblNotes.getStyleClass().add("workout-notes");

        btnEdit.getStyleClass().addAll("button","secondary","sm");
        btnDelete.getStyleClass().addAll("button","danger","sm");
        btnSave.getStyleClass().addAll("button","primary");
        btnCancel.getStyleClass().addAll("button","ghost");

        // Row 1: Title (label in view mode) + actions at right
        HBox row1 = new HBox(10);
        Region spacer1 = new Region(); HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox actionsView = new HBox(8, btnEdit, btnDelete);
        HBox actionsEdit = new HBox(8, btnCancel, btnSave);
        actionsEdit.setVisible(false);
        actionsEdit.managedProperty().bind(actionsEdit.visibleProperty());
        row1.getChildren().addAll(lblTitle, spacer1, actionsView, actionsEdit);
        row1.setAlignment(Pos.CENTER_LEFT);
        row1.getStyleClass().add("toolbar");

        // Row 2: Notes (label in view mode)
        HBox row2 = new HBox(lblNotes);
        row2.setAlignment(Pos.CENTER_LEFT);

        // Row 3: Pool length + stats
        rbSCY.setToggleGroup(tgCourse);
        rbSCM.setToggleGroup(tgCourse);
        rbLCM.setToggleGroup(tgCourse);

        HBox radios = new HBox(10, new Label("Pool length:"), rbSCY, rbSCM, rbLCM);
        radios.setAlignment(Pos.CENTER_LEFT);

        HBox stats = new HBox(14,
                chip(statDistance), sep(), chip(statDuration), sep(), chip(statSwim), sep(), chip(statRest));
        stats.setAlignment(Pos.CENTER_RIGHT);

        HBox row3 = new HBox(10, radios);
        Region spacer3 = new Region(); HBox.setHgrow(spacer3, Priority.ALWAYS);
        row3.getChildren().addAll(spacer3, stats);
        row3.setAlignment(Pos.CENTER_LEFT);
        row3.getStyleClass().add("toolbar");

        // Row 4: timestamps
        lblTimestamps.getStyleClass().add("muted");
        HBox row4 = new HBox(lblTimestamps);
        row4.setAlignment(Pos.CENTER_LEFT);

        // Edit widgets (hidden until editing)
        tfTitle.setPromptText("Workout name");
        tfNotes.setPromptText("Notes (optional)");
        tfTitle.setVisible(false); tfTitle.setManaged(false);
        tfNotes.setVisible(false); tfNotes.setManaged(false);

        // Insert edit fields in place (they’ll be toggled with labels)
        // Row 1: swap lblTitle <-> tfTitle
        row1.getChildren().remove(lblTitle);
        row1.getChildren().add(0, wrapSwap(lblTitle, tfTitle));
        // Row 2: swap lblNotes <-> tfNotes
        row2.getChildren().clear();
        row2.getChildren().add(wrapSwap(lblNotes, tfNotes));

        // Assemble
        root.getChildren().setAll(row1, row2, row3, row4);

        // Actions
        btnEdit.setOnAction(e -> enterEdit(actionsView, actionsEdit));
        btnCancel.setOnAction(e -> { refreshFrom(app.getCurrentWorkout()); exitEdit(actionsView, actionsEdit); });
        btnSave.setOnAction(e -> onSave(actionsView, actionsEdit));
        btnDelete.setOnAction(e -> onDelete());

        // Course changes apply immediately (always enabled)
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
        app.currentWorkoutProperty().addListener((o, oldW, newW) -> refreshFrom(newW));
    }

    // ------------------------------------------------------------------
    // Edit mode
    // ------------------------------------------------------------------
    private void enterEdit(HBox actionsView, HBox actionsEdit) {
        editing = true;
        swapToEdit(true);
        actionsView.setVisible(false);
        actionsEdit.setVisible(true);
        tfTitle.requestFocus();
    }

    private void exitEdit(HBox actionsView, HBox actionsEdit) {
        editing = false;
        swapToEdit(false);
        actionsEdit.setVisible(false);
        actionsView.setVisible(true);
    }

    private void swapToEdit(boolean on) {
        // Title
        lblTitle.setVisible(!on); lblTitle.setManaged(!on);
        tfTitle.setVisible(on);  tfTitle.setManaged(on);
        // Notes
        lblNotes.setVisible(!on); lblNotes.setManaged(!on);
        tfNotes.setVisible(on);  tfNotes.setManaged(on);
    }

    private void onSave(HBox actionsView, HBox actionsEdit) {
        if (presenter == null) {
            exitEdit(actionsView, actionsEdit);
            return;
        }
        Workout w = app.getCurrentWorkout();
        if (w == null) {
            exitEdit(actionsView, actionsEdit);
            return;
        }

        String name  = tfTitle.getText().trim();
        String notes = tfNotes.getText().trim();
        Course c     = selectedCourse();

        presenter.saveHeaderEdits(name, notes, c);   // one-shot save+persist

        exitEdit(actionsView, actionsEdit);
        refreshFrom(app.getCurrentWorkout());        // reflect updated model
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
            lblTitle.setText("");
            lblNotes.setText("");
            tfTitle.clear();
            tfNotes.clear();
            tgCourse.selectToggle(null);
            lblTimestamps.setText("");
            updateStats(null);
            return;
        }

        // Title/Notes (both display + edit widgets)
        String title = nullToEmpty(w.getName());
        String notes = nullToEmpty(w.getNotes());
        lblTitle.setText(title);
        lblNotes.setText(notes.isBlank() ? " " : notes); // keep row height
        tfTitle.setText(title);
        tfNotes.setText(notes);

        // Course radios reflect model
        Course course = (w.getCourse() == null ? Course.SCY : w.getCourse());
        switch (course) {
            case SCY -> tgCourse.selectToggle(rbSCY);
            case SCM -> tgCourse.selectToggle(rbSCM);
            case LCM -> tgCourse.selectToggle(rbLCM);
        }

        updateStats(w);
        lblTimestamps.setText("Created " + DateFmt.local(w.getCreatedAt()) +
                "   •   Updated " + DateFmt.local(w.getUpdatedAt()));
    }

    private void updateStats(Workout w) {
        if (w == null) {
            statDistance.setText("—");
            statDuration.setText("—");
            statSwim.setText("—");
            statRest.setText("—");
            return;
        }

        // Prefer presenter’s computed values; fall back to model when blank.
        String dist = (presenter != null) ? presenter.totalDistanceTextProperty().get() : null;
        if (dist == null || dist.isBlank()) dist = w.totalDistance().toShortString();

        String dur  = (presenter != null) ? presenter.durationTextProperty().get()  : "—";
        String swim = (presenter != null) ? presenter.swimTimeTextProperty().get() : "—";
        String rest = (presenter != null) ? presenter.restTimeTextProperty().get() : "—";

        statDistance.setText(dist);
        statDuration.setText(dur);
        statSwim.setText(swim);
        statRest.setText(rest);
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

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
}
