package swimworkoutbuilder_javafx.state;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.model.Workout;

public final class AppState {
    private static final AppState INSTANCE = new AppState();
    public static AppState get() { return INSTANCE; }
    private AppState() {}

    private final ObjectProperty<Swimmer> currentSwimmer = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Workout> currentWorkout = new SimpleObjectProperty<>(null);

    public ObjectProperty<Swimmer> currentSwimmerProperty() { return currentSwimmer; }
    public Swimmer getCurrentSwimmer() { return currentSwimmer.get(); }
    public void setCurrentSwimmer(Swimmer s) { currentSwimmer.set(s); }

    public ObjectProperty<Workout> currentWorkoutProperty() { return currentWorkout; }
    public Workout getCurrentWorkout() { return currentWorkout.get(); }
    public void setCurrentWorkout(Workout w) { currentWorkout.set(w); }
}