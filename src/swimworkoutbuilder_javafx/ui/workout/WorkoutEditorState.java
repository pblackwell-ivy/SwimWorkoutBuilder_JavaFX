package swimworkoutbuilder_javafx.ui.workout;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import swimworkoutbuilder_javafx.model.Workout;

import java.util.Objects;

/**
 * Minimal editing state holder for a Workout.
 *
 * <p>Provides a staged copy for in-progress edits and tracks a dirty flag.
 * Call {@link #begin(Workout)} to start, mutate the staged model via the presenter,
 * then {@link #commit()} to copy staged -> original or {@link #cancel()} to discard.</p>
 *
 * <p>Deliberately persistence-agnostic: saving to disk is the presenter's job.</p>
 */
public final class WorkoutEditorState {

    private Workout original;   // the one in AppState
    private Workout staged;     // deep copy being edited

    private final BooleanProperty editing = new SimpleBooleanProperty(false);
    private final BooleanProperty dirty   = new SimpleBooleanProperty(false);

    /** Start an editing session from the given original workout. */
    public void begin(Workout original) {
        Objects.requireNonNull(original, "original");
        this.original = original;
        this.staged   = original.deepCopy();  // requires Workout#deepCopy()
        editing.set(true);
        dirty.set(false);
    }

    /** @return true if currently in an edit session. */
    public ReadOnlyBooleanProperty editingProperty() { return editing; }

    /** @return true when there are changes to save. */
    public ReadOnlyBooleanProperty dirtyProperty() { return dirty; }

    /** Convenience: can Save? (editing && dirty) */
    public boolean canSave() { return editing.get() && dirty.get(); }

    /** Expose the staged model to the presenter/pane for binding & mutations. */
    public Workout staged() {
        return staged;
        // presenter mutates staged (add/remove groups/sets, change header, etc.)
        // and then calls markDirty() after each mutation.
    }

    /** Mark the session as having unsaved changes. */
    public void markDirty() { if (editing.get()) dirty.set(true); }

    /** Discard staged changes and end the session. */
    public void cancel() {
        this.staged = null;
        this.original = null;
        editing.set(false);
        dirty.set(false);
    }

    /**
     * Copy staged -> original and end the session.
     * Does not persist; presenter is responsible for LocalStore.saveWorkout(original).
     */
    public void commit() {
        if (!editing.get() || original == null || staged == null) return;
        // requires Workout#copyFrom(Workout)
        original.copyFrom(staged);
        // Session ends; keep references clean
        cancel();
    }
}