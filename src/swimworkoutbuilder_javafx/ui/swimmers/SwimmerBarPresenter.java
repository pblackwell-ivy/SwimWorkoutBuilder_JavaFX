package swimworkoutbuilder_javafx.ui.swimmers;

import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.state.AppState;

/**
 * Presenter for the swimmer chooser.
 * Exposes the list of swimmers and the selected swimmer property.
 * Add/Edit/Delete will be wired later to dialogs and repositories.
 */
public final class SwimmerBarPresenter {

    private final AppState app;
    private final ObservableList<Swimmer> swimmers = FXCollections.observableArrayList();

    public SwimmerBarPresenter(AppState appState) {
        this.app = appState;

        // keep local list in sync with global state
        swimmers.setAll(app.getSwimmers());
        app.swimmersProperty().addListener((obs, oldV, newV) -> {
            swimmers.setAll(app.getSwimmers());
        });
    }

    /** Observable list of swimmers for the ComboBox. */
    public ObservableList<Swimmer> swimmers() { return swimmers; }

    /** Currently selected swimmer (bind bidirectionally to ComboBox). */
    public ObjectProperty<Swimmer> selectedProperty() { return app.currentSwimmerProperty(); }

    // placeholders; will call dialogs and repos later
    public void addNew() {}
    public void editSelected() {}
    public void deleteSelected() {}
}