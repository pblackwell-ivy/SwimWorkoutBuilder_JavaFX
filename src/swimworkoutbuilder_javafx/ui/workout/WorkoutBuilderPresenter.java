package swimworkoutbuilder_javafx.ui.workout;

import javafx.beans.property.*;
import javafx.stage.Window;
import swimworkoutbuilder_javafx.model.SetGroup;
import swimworkoutbuilder_javafx.model.SwimSet;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.ui.dialogs.SetFormDialog;
import swimworkoutbuilder_javafx.ui.dialogs.SetGroupFormDialog;

import java.util.List;
import java.util.Objects;

/**
 * Presenter for the center "Workout Builder" feature.
 *
 * <p>Owns the selection and operations for groups and sets inside the current workout.
 * It mutates the in-memory {@link Workout} held by {@link AppState} but does not
 * persist to disk; persistence remains an explicit action (e.g., in the header's Save).</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Expose current workout and current selections (group, set)</li>
 *   <li>Provide commands to add/edit/delete/move groups and sets</li>
 *   <li>Emit a simple refresh signal the view can observe to rebuild its list</li>
 * </ul>
 *
 * <h2>Design notes</h2>
 * <ul>
 *   <li>We avoid keeping a shadow ObservableList of groups. The view can read
 *       {@code getGroupsView()} and rebuild on {@code refreshTick} changes.</li>
 *   <li>Dialogs return edited models; we apply them to the live workout.</li>
 *   <li>No I/O here; explicit Save/Cancel remains in header presenter.</li>
 * </ul>
 */
public final class WorkoutBuilderPresenter {

    private final AppState app;

    // Current selections (for the view to bind)
    private final ObjectProperty<SetGroup> selectedGroup = new SimpleObjectProperty<>();
    private final ObjectProperty<SwimSet>  selectedSet   = new SimpleObjectProperty<>();

    // Simple "please refresh" pulse; increment when structure changes
    private final IntegerProperty refreshTick = new SimpleIntegerProperty(0);

    public WorkoutBuilderPresenter(AppState app) {
        this.app = Objects.requireNonNull(app, "app");
        // Clear selections when workout changes
        this.app.currentWorkoutProperty().addListener((obs, oldW, newW) -> {
            selectedGroup.set(null);
            selectedSet.set(null);
            pulse();
        });
    }

    // ---------------------------------------------------------------------
    // Bindable properties
    // ---------------------------------------------------------------------

    public ReadOnlyObjectProperty<Workout> workoutProperty() {
        return app.currentWorkoutProperty();
    }
    public Workout getWorkout() { return app.getCurrentWorkout(); }

    public ObjectProperty<SetGroup> selectedGroupProperty() { return selectedGroup; }
    public SetGroup getSelectedGroup() { return selectedGroup.get(); }
    public void setSelectedGroup(SetGroup g) {
        selectedGroup.set(g);
        // Keep set selection sensible
        if (g == null) selectedSet.set(null);
    }

    public ObjectProperty<SwimSet> selectedSetProperty() { return selectedSet; }
    public SwimSet getSelectedSet() { return selectedSet.get(); }
    public void setSelectedSet(SwimSet s) { selectedSet.set(s); }

    /** View calls this to iterate the current groups. */
    public List<SetGroup> getGroupsView() {
        Workout w = getWorkout();
        return (w == null) ? List.of() : w.getGroups();
    }

    /** View can observe this to know when to rebuild the list UI. */
    public ReadOnlyIntegerProperty refreshTickProperty() { return refreshTick; }

    private void pulse() { refreshTick.set(refreshTick.get() + 1); }

    // ---------------------------------------------------------------------
    // Group operations
    // ---------------------------------------------------------------------

    /** Open dialog to add a new group; inserts at end if user saves. */
    public void addGroupDialog(Window owner) {
        Workout w = getWorkout();
        if (w == null) return;

        SetGroup created = SetGroupFormDialog.show(null);
        if (created != null) {
            w.addSetGroup(created);
            app.getCurrentWorkout().touchUpdated();
            setSelectedGroup(created);
            pulse();
        }
    }

    /** Open dialog to edit an existing group in-place. */
    public void editGroupDialog(SetGroup group, Window owner) {
        Workout w = getWorkout();
        if (w == null || group == null) return;

        // Dialog edits a copy; returns edited instance or null
        SetGroup edited = SetGroupFormDialog.show(group);
        if (edited != null && edited != group) {
            // Replace the old instance with the returned edited one
            List<SetGroup> groups = w.getGroups();
            int idx = groups.indexOf(group);
            if (idx >= 0) {
                groups.set(idx, edited);
                setSelectedGroup(edited);
                app.getCurrentWorkout().touchUpdated();
                pulse();
            }
        } else if (edited != null) {
            // If dialog mutated the same instance, still pulse
            app.getCurrentWorkout().touchUpdated();
            pulse();
        }
    }

    public void deleteGroup(SetGroup group) {
        Workout w = getWorkout();
        if (w == null || group == null) return;
        w.getGroups().remove(group);
        if (selectedGroup.get() == group) {
            selectedGroup.set(null);
            selectedSet.set(null);
        }
        app.getCurrentWorkout().touchUpdated();
        pulse();
    }

    public void moveGroupUp(SetGroup group) {
        Workout w = getWorkout();
        if (w == null || group == null) return;
        List<SetGroup> gs = w.getGroups();
        int i = gs.indexOf(group);
        if (i > 0) {
            w.swapGroups(i, i - 1);
            app.getCurrentWorkout().touchUpdated();
            pulse();
        }
    }

    public void moveGroupDown(SetGroup group) {
        Workout w = getWorkout();
        if (w == null || group == null) return;
        List<SetGroup> gs = w.getGroups();
        int i = gs.indexOf(group);
        if (i >= 0 && i < gs.size() - 1) {
            w.swapGroups(i, i + 1);
            app.getCurrentWorkout().touchUpdated();
            pulse();
        }
    }

    // ---------------------------------------------------------------------
    // Set operations
    // ---------------------------------------------------------------------

    /** Open dialog to add a set to a group. */
    public void addSetDialog(SetGroup group, Window owner) {
        Workout w = getWorkout();
        if (w == null || group == null) return;

        SwimSet created = SetFormDialog.show(w, null);
        if (created != null) {
            group.addSet(created);
            setSelectedGroup(group);
            setSelectedSet(created);
            app.getCurrentWorkout().touchUpdated();
            pulse();
        }
    }

    /** Open dialog to edit an existing set. */
    public void editSetDialog(SetGroup group, SwimSet set, Window owner) {
        Workout w = getWorkout();
        if (w == null || group == null || set == null) return;

        SwimSet edited = SetFormDialog.show(w, set);
        if (edited != null && edited != set) {
            List<SwimSet> sets = group.getSets();
            int idx = sets.indexOf(set);
            if (idx >= 0) {
                sets.set(idx, edited);
                setSelectedGroup(group);
                setSelectedSet(edited);
                app.getCurrentWorkout().touchUpdated();
                pulse();
            }
        } else if (edited != null) {
            app.getCurrentWorkout().touchUpdated();
            pulse();
        }
    }

    public void deleteSet(SetGroup group, SwimSet set) {
        Workout w = getWorkout();
        if (w == null || group == null || set == null) return;

        group.getSets().remove(set);
        if (selectedSet.get() == set) selectedSet.set(null);
        app.getCurrentWorkout().touchUpdated();
        pulse();
    }

    public void moveSetUp(SetGroup group, SwimSet set) {
        if (group == null || set == null) return;
        List<SwimSet> sets = group.getSets();
        int i = sets.indexOf(set);
        if (i > 0) {
            sets.set(i, sets.get(i - 1));
            sets.set(i - 1, set);
            app.getCurrentWorkout().touchUpdated();
            pulse();
        }
    }

    public void moveSetDown(SetGroup group, SwimSet set) {
        if (group == null || set == null) return;
        List<SwimSet> sets = group.getSets();
        int i = sets.indexOf(set);
        if (i >= 0 && i < sets.size() - 1) {
            sets.set(i, sets.get(i + 1));
            sets.set(i + 1, set);
            app.getCurrentWorkout().touchUpdated();
            pulse();
        }
    }
}