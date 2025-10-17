package swimworkoutbuilder_javafx.ui.shell;


import java.time.Instant;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.store.LocalStore;
import swimworkoutbuilder_javafx.ui.Icons;
import swimworkoutbuilder_javafx.ui.workout.LoadWorkoutDialog;
import swimworkoutbuilder_javafx.ui.workout.WorkoutFormDialog;

/**
 * Top application toolbar.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Swimmer selection (combo) and swimmer quick actions.</li>
 *   <li>Workout quick actions (new/open/print).</li>
 *   <li>Bind enable/disable state to {@link AppState} (idempotent wiring).</li>
 * </ul>
 *
 * <p>Styling: relies on global theme classes:
 * <pre>
 *   .toolbar .button.primary / .secondary / .ghost / .accent / .sm
 * </pre>
 *
 * <p>No business logic lives here—this is purely a shell/launcher for dialogs and state changes.</p>
 *
 * @since 1.0
 */
public final class ActionBar {

    // ---------------------------------------------------------------------
    // UI root
    // ---------------------------------------------------------------------
    private final HBox root = new HBox(10);

    // ---------------------------------------------------------------------
    // Swimmer controls
    // ---------------------------------------------------------------------
    private final ComboBox<Swimmer> cbSwimmer = new ComboBox<>();
    private final Button btnNewSwimmer  = new Button("New Swimmer");
    private final Button btnManageSwimmer = new Button("Manage…"); // opens combo popup for now

    // ---------------------------------------------------------------------
    // Workout controls
    // ---------------------------------------------------------------------
    private final Button btnNewWorkout  = new Button("New Workout");
    private final Button btnOpenWorkout = new Button("Open Workout");
    private final Button btnPrint       = new Button("Print");
    private final Button btnSaveWorkout = new Button();

    // ---------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------
    public ActionBar() {
        buildUI();
        wireState();
        wireHandlers();
        refreshInitialSelection();
    }

    /** Exposes the toolbar node. */
    public Node node() { return root; }

    // ---------------------------------------------------------------------
    // UI construction
    // ---------------------------------------------------------------------
    private void buildUI() {
        root.setPadding(new Insets(8, 12, 8, 12));
        root.getStyleClass().add("toolbar");

        // --- Swimmer combo formatting ------------------------------------
        cbSwimmer.setPrefWidth(220);
        cbSwimmer.setPromptText("Select swimmer…");
        cbSwimmer.setConverter(new StringConverter<>() {
            @Override public String toString(Swimmer s) { return displayName(s); }
            @Override public Swimmer fromString(String s) { return null; }
        });
        cbSwimmer.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(Swimmer s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : displayName(s));
            }
        });
        cbSwimmer.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Swimmer s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : displayName(s));
            }
        });

        // --- Role styles (theme classes) ---------------------------------
        setRoles(btnNewSwimmer,  "secondary");
        setRoles(btnManageSwimmer, "secondary");
        setRoles(btnNewWorkout,  "primary");
        setRoles(btnOpenWorkout, "secondary");
        setRoles(btnPrint,       "ghost");

        setRoles(btnSaveWorkout, "primary", "sm", "icon");
        btnSaveWorkout.setGraphic(Icons.make("save", 16));
        btnSaveWorkout.setTooltip(new Tooltip("Save workout"));
        btnSaveWorkout.disableProperty().bind(AppState.get().getWorkoutBuilderPresenter().dirtyProperty().not());

        // --- Layout: [ Swimmer: (combo)  New  Manage ]  |spacer|  [ New Workout  Open  Print ]
        Label swimmerLbl = new Label("Swimmer:");
        HBox left = new HBox(8, swimmerLbl, cbSwimmer, btnNewSwimmer, btnManageSwimmer);
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox right = new HBox(8, btnNewWorkout, btnOpenWorkout, btnSaveWorkout, btnPrint);
        root.getChildren().addAll(left, spacer, right);
    }

    // ---------------------------------------------------------------------
    // State wiring
    // ---------------------------------------------------------------------
    private void wireState() {
        var app = AppState.get();

        // Items + two-way selection
        cbSwimmer.setItems(app.getSwimmers());
        cbSwimmer.getSelectionModel().selectedItemProperty().addListener((o, oldV, sel) -> {
            if (sel != null && sel != app.getCurrentSwimmer()) app.setCurrentSwimmer(sel);
        });
        app.currentSwimmerProperty().addListener((o, oldV, s) -> {
            if (s != cbSwimmer.getValue()) cbSwimmer.setValue(s);
        });

        // Enable/disable workout actions based on swimmer presence
        Runnable updateWorkoutButtons = () -> {
            boolean ok = (app.getCurrentSwimmer() != null);
            btnNewWorkout.setDisable(!ok);
            btnOpenWorkout.setDisable(!ok);
        };
        updateWorkoutButtons.run();
        app.currentSwimmerProperty().addListener((obs, o, s) -> updateWorkoutButtons.run());

        // Print depends on a current workout
        btnPrint.setDisable(app.getCurrentWorkout() == null);
        app.currentWorkoutProperty().addListener((obs, o, w) -> btnPrint.setDisable(w == null));
    }

    private void refreshInitialSelection() {
        var app = AppState.get();
        if (app.getCurrentSwimmer() != null) {
            cbSwimmer.setValue(app.getCurrentSwimmer());
        } else if (!app.getSwimmers().isEmpty()) {
            cbSwimmer.getSelectionModel().selectFirst();
        }
    }

    // ---------------------------------------------------------------------
    // Handlers (shell only)
    // ---------------------------------------------------------------------
    private void wireHandlers() {
        var app = AppState.get();

        // “Manage…”: MVP behavior = just open the popup
        btnManageSwimmer.setOnAction(e -> cbSwimmer.show());

        btnNewSwimmer.setOnAction(e -> onAddSwimmer());

        btnNewWorkout.setOnAction(e -> {
            if (!confirmLoseChanges()) return;
            Swimmer cur = app.getCurrentSwimmer();
            if (cur == null) {
                new Alert(Alert.AlertType.INFORMATION, "Choose or create a swimmer first.").showAndWait();
                return;
            }
            var w = WorkoutFormDialog.show(cur.getId(), null);
            if (w != null) {
                try { LocalStore.saveWorkout(w); } catch (Exception ignored) {}
                app.setCurrentWorkout(w);
            }
        });

        btnOpenWorkout.setOnAction(e -> {
            if (!confirmLoseChanges()) return;
            Swimmer cur = app.getCurrentSwimmer();
            if (cur == null) {
                new Alert(Alert.AlertType.INFORMATION, "Choose or create a swimmer first.").showAndWait();
                return;
            }
            var w = LoadWorkoutDialog.show(cur.getId());
            if (w != null) app.setCurrentWorkout(w);
        });

        btnSaveWorkout.setOnAction(e -> {
            var presenter = AppState.get().getWorkoutBuilderPresenter();
            if (presenter != null) presenter.persistCurrentWorkout();
        });

        btnPrint.setOnAction(e ->
                new Alert(Alert.AlertType.INFORMATION, "Print Preview: coming soon.").showAndWait()
        );
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------
    private static void setRoles(Button b, String... roles) {
        b.getStyleClass().removeAll("primary","secondary","ghost","danger","accent","success","warn","sm","icon");
        b.getStyleClass().add("button");
        b.getStyleClass().addAll(roles);
        b.setFocusTraversable(false);
    }

    private void onAddSwimmer() {
        // Minimal “new swimmer” dialog (same UX as SwimmerPane)
        Dialog<Swimmer> dlg = new Dialog<>();
        dlg.setTitle("New Swimmer");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        TextField tfFirst = new TextField();
        TextField tfLast  = new TextField();
        TextField tfPref  = new TextField();
        TextField tfTeam  = new TextField();

        GridPane gp = new GridPane();
        gp.setHgap(8); gp.setVgap(8); gp.setPadding(new Insets(10));
        gp.addRow(0, new Label("First:"), tfFirst);
        gp.addRow(1, new Label("Last:"),  tfLast);
        gp.addRow(2, new Label("Preferred (optional):"), tfPref);
        gp.addRow(3, new Label("Team (optional):"), tfTeam);
        dlg.getDialogPane().setContent(gp);

        Node ok = dlg.getDialogPane().lookupButton(ButtonType.OK);
        ok.setDisable(true);
        tfFirst.textProperty().addListener((o,a,b)-> ok.setDisable(b.trim().isEmpty()));
        tfLast.textProperty().addListener((o,a,b)-> ok.setDisable(tfFirst.getText().trim().isEmpty() || b.trim().isEmpty()));

        dlg.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            return new Swimmer(
                    java.util.UUID.randomUUID(),
                    tfFirst.getText().trim(),
                    tfLast.getText().trim(),
                    tfPref.getText().isBlank() ? null : tfPref.getText().trim(),
                    tfTeam.getText().isBlank() ? null : tfTeam.getText().trim(),
                    Instant.now(),
                    Instant.now()
            );
        });

        Swimmer created = dlg.showAndWait().orElse(null);
        if (created == null) return;

        try { LocalStore.saveSwimmer(created); } catch (Exception ignored) {}
        var app = AppState.get();
        app.getSwimmers().add(created);
        app.setCurrentSwimmer(created);
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

    private boolean confirmLoseChanges() {
        var presenter = AppState.get().getWorkoutBuilderPresenter();
        if (presenter == null || !presenter.dirtyProperty().get()) return true;

        var SAVE = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        var DONT = new ButtonType("Don’t Save", ButtonBar.ButtonData.NO);
        var CANCEL = ButtonType.CANCEL;

        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "You have unsaved changes to this workout.", SAVE, DONT, CANCEL);
        a.setHeaderText("Unsaved changes");
        a.setTitle("Save changes?");

        ButtonType choice = a.showAndWait().orElse(CANCEL);
        if (choice == SAVE) {
            presenter.persistCurrentWorkout();
            return true;
        } else if (choice == DONT) {
            return true;
        } else {
            return false;
        }
    }
}
