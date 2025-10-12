package swimworkoutbuilder_javafx.ui.swimmers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.state.AppState;

import java.time.format.DateTimeFormatter;

/**
 * Compact swimmer selector + read-only details block for the left pane.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Show a ComboBox of swimmers from AppState</li>
 *   <li>Reflect selected swimmer’s basic details (name, team, created/updated)</li>
 *   <li>Expose local Edit/Delete buttons (no behavior wired yet)</li>
 * </ul>
 *
 * <p>Design notes:
 * <ul>
 *   <li>Pure view: no repo access; reads/writes AppState only</li>
 *   <li>Minimal layout to keep vertical footprint small</li>
 * </ul>
 */
public final class SwimmerSection {

    private final VBox root = new VBox(8);

    private final ComboBox<Swimmer> cbSwimmer = new ComboBox<>();

    private final Label lblFullName    = new Label("-");
    private final Label lblPreferred   = new Label("-");
    private final Label lblTeam        = new Label("-");
    private final Label lblCreated     = new Label("-");
    private final Label lblUpdated     = new Label("-");

    // Local actions (intentionally not wired yet; presenter will attach later)
    private final Button btnEdit   = new Button("Edit");
    private final Button btnDelete = new Button("Delete");

    private final AppState app;

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(java.time.ZoneId.systemDefault());

    public SwimmerSection(AppState app) {
        this.app = app;

        // ----- Top row: selector + local actions (Edit/Delete) -----
        var actions = new HBox(6, btnEdit, btnDelete);
        var spacer  = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        var selectorRow = new HBox(8, new Label("Swimmer:"), cbSwimmer, spacer, actions);
        selectorRow.setAlignment(Pos.CENTER_LEFT);

        // Keep combo width reasonable but compact
        cbSwimmer.setPrefWidth(220);

        // ----- Details block (read-only) -----
        var details = new VBox(3,
                row("Name:",        lblFullName),
                row("Preferred:",   lblPreferred),
                row("Team:",        lblTeam),
                row("Created:",     lblCreated),
                row("Updated:",     lblUpdated)
        );
        details.getStyleClass().add("swimmer-details");

        root.getChildren().addAll(selectorRow, details);
        root.setPadding(new Insets(8, 8, 12, 8));

        // ----- Bindings to AppState -----

        // Populate list from AppState.swimmers
        cbSwimmer.setItems(app.getSwimmers());
        // If the list object in AppState is replaced, keep ComboBox in sync
        app.swimmersProperty().addListener((obs, oldList, newList) -> cbSwimmer.setItems(newList));

        // Write selection -> AppState.currentSwimmer
        cbSwimmer.getSelectionModel().selectedItemProperty().addListener((o, old, sel) -> {
            if (sel != app.getCurrentSwimmer()) {
                app.setCurrentSwimmer(sel);
            }
        });

        // Read AppState.currentSwimmer -> selection + details
        app.currentSwimmerProperty().addListener((o, old, sel) -> {
            if (cbSwimmer.getValue() != sel) cbSwimmer.setValue(sel);
            renderDetails(sel);
        });

        // Initial render
        if (app.getCurrentSwimmer() != null) {
            cbSwimmer.setValue(app.getCurrentSwimmer());
        } else if (!app.getSwimmers().isEmpty()) {
            cbSwimmer.setValue(app.getSwimmers().get(0));
        }
        renderDetails(cbSwimmer.getValue());

        // Buttons disabled when nothing selected
        btnEdit.disableProperty().bind(cbSwimmer.valueProperty().isNull());
        btnDelete.disableProperty().bind(cbSwimmer.valueProperty().isNull());
    }

    private static HBox row(String label, Label value) {
        var l = new Label(label);
        l.getStyleClass().add("muted");
        var box = new HBox(6, l, value);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private void renderDetails(Swimmer s) {
        if (s == null) {
            lblFullName.setText("-");
            lblPreferred.setText("-");
            lblTeam.setText("-");
            lblCreated.setText("-");
            lblUpdated.setText("-");
            return;
        }
        String full = (nullToEmpty(s.getFirstName()) + " " + nullToEmpty(s.getLastName())).trim();
        lblFullName.setText(full.isEmpty() ? "—" : full);
        lblPreferred.setText(emptyDash(s.getPreferredName()));
        lblTeam.setText(emptyDash(s.getTeamName()));
        // If your Swimmer doesn’t have timestamps yet, these will show “—”
        try {
            var created = s.getCreatedAt();
            var updated = s.getUpdatedAt();
            lblCreated.setText(created != null ? TS.format(created) : "—");
            lblUpdated.setText(updated != null ? TS.format(updated) : "—");
        } catch (Throwable ignored) {
            lblCreated.setText("—");
            lblUpdated.setText("—");
        }
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
    private static String emptyDash(String s)   { return (s == null || s.isBlank()) ? "—" : s; }

    /** Primary accessor for embedding in layouts. */
    public Parent node() { return root; }

    /** Alias for compatibility with callers using .root(). */
    public Parent root() { return root; }
}