package swimworkoutbuilder_javafx.state;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.model.Workout;

import java.io.Serializable;

public final class AppState implements Serializable {
    private static final long serialVersionUID = 1L;

    // Singleton
    private static final AppState INSTANCE = new AppState();
    private AppState() {}
    /** Preferred accessor used throughout the UI. */
    public static AppState get() { return INSTANCE; }
    /** Kept for any legacy callers. */
    public static AppState getInstance() { return INSTANCE; }

    // Observable state
    private final ObjectProperty<ObservableList<Swimmer>> swimmers =
            new SimpleObjectProperty<>(FXCollections.observableArrayList());
    private final ObjectProperty<Swimmer> currentSwimmer = new SimpleObjectProperty<>();
    private final ObjectProperty<Workout> currentWorkout = new SimpleObjectProperty<>();

    // Swimmers list (the ComboBox binds to this)
    public ObservableList<Swimmer> getSwimmers() { return swimmers.get(); }
    public void setSwimmers(ObservableList<Swimmer> value) { swimmers.set(value); }
    public ObjectProperty<ObservableList<Swimmer>> swimmersProperty() { return swimmers; }

    // Current swimmer
    public Swimmer getCurrentSwimmer() { return currentSwimmer.get(); }
    public void setCurrentSwimmer(Swimmer value) { currentSwimmer.set(value); }
    public ObjectProperty<Swimmer> currentSwimmerProperty() { return currentSwimmer; }

    // Current workout
    public Workout getCurrentWorkout() { return currentWorkout.get(); }
    public void setCurrentWorkout(Workout value) { currentWorkout.set(value); }
    public ObjectProperty<Workout> currentWorkoutProperty() { return currentWorkout; }

    // Convenience
    public void clear() { currentSwimmer.set(null); currentWorkout.set(null); }

    @Override public String toString() {
        return "AppState{swimmers=" + (getSwimmers()==null?0:getSwimmers().size()) +
                ", currentSwimmer=" + (getCurrentSwimmer()==null?"none":getCurrentSwimmer().getId()) +
                ", currentWorkout=" + (getCurrentWorkout()==null?"none":getCurrentWorkout().getId()) +
                '}';
    }
}