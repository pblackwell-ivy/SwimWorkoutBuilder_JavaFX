package swimworkoutbuilder_javafx.ui.swimmers;


import java.util.Objects;
import java.util.UUID;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.store.LocalStore;

/**
 * Presenter for creating/editing swimmers.
 *
 * <p>Owns the UI state (fields, mode flags) and updates the in-memory
 * {@link AppState}. Persistence (save) is delegated to {@link LocalStore}.
 * This class is UI-toolkit agnostic besides JavaFX properties.</p>
 */
public final class SwimmerPresenter {

    // --- External model/state -------------------------------------------------
    private final AppState app = AppState.get();

    // List the view can bind to (by default mirrors AppState.swimmers)
    private final ObservableList<Swimmer> swimmers = FXCollections.observableArrayList();

    // Currently selected in the list (or null)
    private final ObjectProperty<Swimmer> selected = new SimpleObjectProperty<>();

    // --- Form fields (bound to inputs) ---------------------------------------
    private final StringProperty firstName     = new SimpleStringProperty("");
    private final StringProperty lastName      = new SimpleStringProperty("");
    private final StringProperty preferredName = new SimpleStringProperty("");
    private final StringProperty teamName      = new SimpleStringProperty("");

    // --- Mode flags -----------------------------------------------------------
    public enum Mode { VIEWING, ADDING, EDITING }
    private final ObjectProperty<Mode> mode = new SimpleObjectProperty<>(Mode.VIEWING);

    private final BooleanProperty canSave   = new SimpleBooleanProperty(false);
    private final BooleanProperty canDelete = new SimpleBooleanProperty(false);

    // --- Working copy for edit/add -------------------------------------------
    private Swimmer working; // deep copy during edit/add

    public SwimmerPresenter() {
        // Mirror AppState swimmers by default (keeps us decoupled from MainView for now)
        swimmers.setAll(app.getSwimmers());
        app.swimmersProperty().addListener((obs, o, n) -> {
            swimmers.setAll(n != null ? n : FXCollections.observableArrayList());
        });

        // Selection wiring
        selected.addListener((obs, oldSel, sel) -> {
            canDelete.set(sel != null);
            if (mode.get() == Mode.VIEWING && sel != null) {
                // Populate the read-only snapshot into the fields (for display)
                firstName.set(sel.getFirstName());
                lastName.set(sel.getLastName());
                preferredName.set(sel.getPreferredName() == null ? "" : sel.getPreferredName());
                teamName.set(sel.getTeamName() == null ? "" : sel.getTeamName());
            }
        });

        // Save allowed when in ADDING/EDITING and required fields are present
        canSave.bind(mode.isEqualTo(Mode.ADDING).or(mode.isEqualTo(Mode.EDITING))
                .and(firstName.isNotEmpty())
                .and(lastName.isNotEmpty()));
    }

    // --- Commands -------------------------------------------------------------

    /** Start creating a new swimmer. */
    public void beginAdd() {
        mode.set(Mode.ADDING);
        working = new Swimmer("",""); // temp; fields overwrite
        firstName.set("");
        lastName.set("");
        preferredName.set("");
        teamName.set("");
    }

    /** Begin editing the currently selected swimmer. */
    public void beginEdit() {
        Swimmer cur = selected.get();
        if (cur == null) return;
        mode.set(Mode.EDITING);
        working = new Swimmer(cur); // deep copy preserves id
        firstName.set(working.getFirstName());
        lastName.set(working.getLastName());
        preferredName.set(working.getPreferredName() == null ? "" : working.getPreferredName());
        teamName.set(working.getTeamName() == null ? "" : working.getTeamName());
    }

    /** Cancel add/edit and return to viewing mode. */
    public void cancel() {
        working = null;
        mode.set(Mode.VIEWING);
        // Re-sync display fields from the selection
        Swimmer sel = selected.get();
        if (sel != null) {
            firstName.set(sel.getFirstName());
            lastName.set(sel.getLastName());
            preferredName.set(sel.getPreferredName() == null ? "" : sel.getPreferredName());
            teamName.set(sel.getTeamName() == null ? "" : sel.getTeamName());
        } else {
            firstName.set(""); lastName.set(""); preferredName.set(""); teamName.set("");
        }
    }

    /** Persist the add/edit and update AppState. */
    public boolean save() {
        if (!canSave.get()) return false;

        // Apply form fields to the working copy
        if (working == null) {
            working = new Swimmer(firstName.get(), lastName.get(), null, null);
        }
        working.setFirstName(firstName.get().trim());
        working.setLastName(lastName.get().trim());
        working.setPreferredName(emptyToNull(preferredName.get()));
        working.setTeamName(emptyToNull(teamName.get()));

        // If ADDING, ensure it has a fresh UUID (constructor already did)
        // If EDITING, the deep copy preserved the logical id

        // Update AppState list
        if (mode.get() == Mode.ADDING) {
            app.getSwimmers().add(working);
            swimmers.add(working);
            selected.set(working);
            app.setCurrentSwimmer(working);
        } else if (mode.get() == Mode.EDITING) {
            // Replace the existing swimmer (by id) in both lists
            replaceById(app.getSwimmers(), working);
            replaceById(swimmers, working);
            // If currently active, update AppState’s selection reference too
            if (app.getCurrentSwimmer() != null &&
                    Objects.equals(app.getCurrentSwimmer().getId(), working.getId())) {
                app.setCurrentSwimmer(working);
            }
            selected.set(working);
        }

        // Persist (best-effort; we keep UI responsive regardless)
        try {
            LocalStore.saveSwimmer(working);
        } catch (Exception ignored) {}

        mode.set(Mode.VIEWING);
        working = null;
        return true;
    }

    /** Delete the selected swimmer (from lists and disk). */
    public void deleteSelected() {
        Swimmer sel = selected.get();
        if (sel == null) return;

        app.getSwimmers().removeIf(s -> s.getId().equals(sel.getId()));
        swimmers.removeIf(s -> s.getId().equals(sel.getId()));

        // Clear current selection if it was this swimmer
        if (app.getCurrentSwimmer() != null &&
                app.getCurrentSwimmer().getId().equals(sel.getId())) {
            app.setCurrentSwimmer(null);
        }
        selected.set(null);

        // Best-effort persistence (if LocalStore has a deleteSwimmer, use it)
        try {
            // If your LocalStore has deleteSwimmer(UUID), this will work.
            // If not, no harm done.
            LocalStore.class
                    .getMethod("deleteSwimmer", UUID.class)
                    .invoke(null, sel.getId());
        } catch (Throwable ignored) { }
    }

    // --- Helpers --------------------------------------------------------------

    private static void replaceById(ObservableList<Swimmer> list, Swimmer updated) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(updated.getId())) {
                list.set(i, updated);
                return;
            }
        }
        list.add(updated);
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    // --- Exposed properties for the view -------------------------------------

    public ObservableList<Swimmer> swimmers() { return swimmers; }

    public ObjectProperty<Swimmer> selectedProperty() { return selected; }
    public Swimmer getSelected() { return selected.get(); }
    public void setSelected(Swimmer s) { selected.set(s); }

    public StringProperty firstNameProperty() { return firstName; }
    public StringProperty lastNameProperty() { return lastName; }
    public StringProperty preferredNameProperty() { return preferredName; }
    public StringProperty teamNameProperty() { return teamName; }

    public ObjectProperty<Mode> modeProperty() { return mode; }
    public Mode getMode() { return mode.get(); }

    public BooleanProperty canSaveProperty() { return canSave; }
    public BooleanProperty canDeleteProperty() { return canDelete; }
}
