package swimworkoutbuilder_javafx.ui.seeds;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import swimworkoutbuilder_javafx.state.AppState;

/**
 * Presenter for the Seed Times pane.
 * Manages editing state and Save/Cancel enablement.
 */
public final class SeedTimesPresenter {

    private final AppState app;
    private final BooleanProperty editing = new SimpleBooleanProperty(false);
    private final BooleanProperty canSave = new SimpleBooleanProperty(false);

    public SeedTimesPresenter(AppState appState) {
        this.app = appState;

        // when swimmer changes, reset editing state
        app.currentSwimmerProperty().addListener((o, oldS, newS) -> {
            editing.set(false);
            canSave.set(false);
        });
    }

    public BooleanProperty editingProperty() { return editing; }
    public BooleanProperty canSaveProperty() { return canSave; }

    // simple state transitions (UI buttons will bind to these)
    public void beginEdit() { editing.set(true); }
    public void cancel() { editing.set(false); canSave.set(false); }
    public void markDirty() { if (editing.get()) canSave.set(true); }
    public void save() {
        // actual save to repository will come later
        editing.set(false);
        canSave.set(false);
    }
}