package swimworkoutbuilder_javafx.ui.workout;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import swimworkoutbuilder_javafx.model.SetGroup;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.state.AppState;

/**
 * Presenter for the central Workout builder area.
 * Keeps header and list of SetGroups in sync with the current Workout.
 */
public final class WorkoutPresenter {

    private final AppState app;
    private final StringProperty header = new SimpleStringProperty("Workout");
    private final ObservableList<SetGroup> groups = FXCollections.observableArrayList();

    public WorkoutPresenter(AppState appState) {
        this.app = appState;
        app.currentWorkoutProperty().addListener((o, oldW, newW) -> refresh(newW));
        refresh(app.getCurrentWorkout());
    }

    private void refresh(Workout w) {
        if (w == null) {
            header.set("Workout");
            groups.clear();
        } else {
            header.set(w.getName() == null ? "Workout" : w.getName());
            groups.setAll(w.getGroups());
        }
    }

    public StringProperty headerProperty() { return header; }
    public ObservableList<SetGroup> groups() { return groups; }

    // will be implemented later
    public void addGroup() {}
    public void removeGroup(SetGroup g) {}
}