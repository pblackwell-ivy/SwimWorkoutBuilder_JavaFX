package swimworkoutbuilder_javafx.state;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.model.Workout;

/**
 * Single source of truth for UI state.
 * Exposes observable properties so views/presenters can bind without caring
 * where widgets are placed on screen.
 *
 * Backward compatibility:
 * - currentSwimmer/Workout + activeSwimmer/Workout aliases
 */
public final class AppState {

    // Primary (preferred) names
    private final ObjectProperty<Swimmer> currentSwimmer = new SimpleObjectProperty<>();
    private final ObjectProperty<Workout> currentWorkout = new SimpleObjectProperty<>();

    private final ListProperty<Swimmer> swimmers =
            new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ListProperty<Workout> recentWorkouts =
            new SimpleListProperty<>(FXCollections.observableArrayList());

    // ---- Current swimmer ----
    public ObjectProperty<Swimmer> currentSwimmerProperty() { return currentSwimmer; }
    public Swimmer getCurrentSwimmer() { return currentSwimmer.get(); }
    public void setCurrentSwimmer(Swimmer s) { currentSwimmer.set(s); }

    // ---- Current workout ----
    public ObjectProperty<Workout> currentWorkoutProperty() { return currentWorkout; }
    public Workout getCurrentWorkout() { return currentWorkout.get(); }
    public void setCurrentWorkout(Workout w) { currentWorkout.set(w); }

    // ---- Swimmers list (observable) ----
    public ListProperty<Swimmer> swimmersProperty() { return swimmers; }
    public ObservableList<Swimmer> getSwimmers() { return swimmers.get(); }
    public void setSwimmers(ObservableList<Swimmer> list) { this.swimmers.set(list); }

    // ---- Recent workouts (optional) ----
    public ListProperty<Workout> recentWorkoutsProperty() { return recentWorkouts; }
    public ObservableList<Workout> getRecentWorkouts() { return recentWorkouts.get(); }
    public void setRecentWorkouts(ObservableList<Workout> list) { this.recentWorkouts.set(list); }

    // -------- Aliases for older call sites (safe to keep) --------
    public ObjectProperty<Swimmer> activeSwimmerProperty() { return currentSwimmer; }
    public Swimmer getActiveSwimmer() { return getCurrentSwimmer(); }
    public void setActiveSwimmer(Swimmer s) { setCurrentSwimmer(s); }

    public ObjectProperty<Workout> activeWorkoutProperty() { return currentWorkout; }
    public Workout getActiveWorkout() { return getCurrentWorkout(); }
    public void setActiveWorkout(Workout w) { setCurrentWorkout(w); }

    // ---------------------------------------------------------------------
// TEMPORARY SINGLETON ACCESSOR for legacy UI classes.
// TODO: remove once all views use constructor-injected AppState
// ---------------------------------------------------------------------
    private static final AppState INSTANCE = new AppState();

    public static AppState get() { return INSTANCE; }
}