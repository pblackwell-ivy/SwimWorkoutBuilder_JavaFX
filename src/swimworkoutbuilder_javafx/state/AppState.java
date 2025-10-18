package swimworkoutbuilder_javafx.state;

// ------------------------------------------------------------
// Imports
// ------------------------------------------------------------


import java.io.Serializable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.ui.workout.WorkoutBuilderPresenter;

/**
 * Global observable state for the application (MVVM-ish “store”).
 *
 * <p>Holds the list of known swimmers and the currently-selected
 * swimmer and workout. Views bind to these properties; presenters
 * update them in response to user actions.</p>
 *
 * <p><b>Design notes</b></p>
 * <ul>
 *   <li>Singleton with simple observable properties (no frameworks).</li>
 *   <li>When the current swimmer changes, an existing workout that
 *       belongs to a different swimmer is cleared to avoid edits
 *       against the wrong swimmer.</li>
 *   <li>Views should bind to the {@code *Property()} accessors rather
 *       than polling the getters.</li>
 * </ul>
 *
 * @author Parker Blackwell
 * @version 1.1
 * @since 2025-10-10
 */
public final class AppState implements Serializable {
    private static final long serialVersionUID = 1L;

    // ------------------------------------------------------------
    // Singleton
    // ------------------------------------------------------------
    private static final AppState INSTANCE = new AppState();
    /** Preferred accessor used throughout the UI. */
    public static AppState get() { return INSTANCE; }
    /** Legacy alias retained for older callers. */
    public static AppState getInstance() { return INSTANCE; }

    // ------------------------------------------------------------
    // Observable state
    // ------------------------------------------------------------
    /** Backing list for the swimmer chooser. */
    private final ObjectProperty<ObservableList<Swimmer>> swimmers =
            new SimpleObjectProperty<>(FXCollections.observableArrayList());

    /** Currently selected swimmer. */
    private final ObjectProperty<Swimmer> currentSwimmer = new SimpleObjectProperty<>();

    /** Currently selected workout (may be {@code null}). */
    private final ObjectProperty<Workout> currentWorkout = new SimpleObjectProperty<>();

    /** Single presenter shared by header/builder panes; created once and exposed via AppState. */
    private final WorkoutBuilderPresenter workoutBuilderPresenter = new WorkoutBuilderPresenter(this);

    // ------------------------------------------------------------
    // Construction & invariants
    // ------------------------------------------------------------
    private AppState() {
        // Keep workout selection consistent with swimmer selection.
        currentSwimmer.addListener((obs, oldS, newS) -> {
            // If no swimmer, clear any workout.
            if (newS == null) {
                setCurrentWorkout(null);
                return;
            }
            // If a workout is set for a different swimmer, clear it.
            Workout w = getCurrentWorkout();
            if (w != null && !w.getSwimmerId().equals(newS.getId())) {
                setCurrentWorkout(null);
            }
        });
    }

    // ------------------------------------------------------------
    // Swimmers list (the ComboBox binds to this)
    // ------------------------------------------------------------
    public ObservableList<Swimmer> getSwimmers() { return swimmers.get(); }
    public void setSwimmers(ObservableList<Swimmer> value) { swimmers.set(value); }

    public ObjectProperty<ObservableList<Swimmer>> swimmersProperty() { return swimmers; }

    // ------------------------------------------------------------
    // Current swimmer
    // ------------------------------------------------------------
    public Swimmer getCurrentSwimmer() { return currentSwimmer.get(); }
    public void setCurrentSwimmer(Swimmer value) { currentSwimmer.set(value); }

    public ObjectProperty<Swimmer> currentSwimmerProperty() { return currentSwimmer; }

    // ------------------------------------------------------------
    // Current workout
    // ------------------------------------------------------------
    public Workout getCurrentWorkout() { return currentWorkout.get(); }
    public void setCurrentWorkout(Workout value) { currentWorkout.set(value); }

    public ObjectProperty<Workout> currentWorkoutProperty() { return currentWorkout; }

    // ------------------------------------------------------------
    // Utilities
    // ------------------------------------------------------------
    /** Clear both current selections (does not modify the swimmers list). */
    public void clear() {
        currentSwimmer.set(null);
        currentWorkout.set(null);
    }

    /** Presenter accessor for callers like ActionBar/MainView. */
    public WorkoutBuilderPresenter getWorkoutBuilderPresenter() { return workoutBuilderPresenter; }
    /** Convenience pass‑through to observe/save dirty state globally if needed. */
    public ReadOnlyBooleanProperty dirtyProperty() { return workoutBuilderPresenter.dirtyProperty(); }
    /** Persist the current workout via presenter (null‑safe). */
    public void persistCurrentWorkout() { workoutBuilderPresenter.persistCurrentWorkout(); }

    @Override
    public String toString() {
        return "AppState{" +
                "swimmers=" + (getSwimmers() == null ? 0 : getSwimmers().size()) +
                ", currentSwimmer=" + (getCurrentSwimmer() == null ? "none" : getCurrentSwimmer().getId()) +
                ", currentWorkout=" + (getCurrentWorkout() == null ? "none" : getCurrentWorkout().getId()) +
                '}';
    }
}
