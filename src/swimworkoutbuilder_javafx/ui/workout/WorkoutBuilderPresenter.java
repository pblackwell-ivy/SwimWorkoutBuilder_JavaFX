package swimworkoutbuilder_javafx.ui.workout;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import swimworkoutbuilder_javafx.model.SetGroup;
import swimworkoutbuilder_javafx.model.SwimSet;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.state.AppState;

public class WorkoutBuilderPresenter {

    private final AppState app;

    // Exposed to the pane for rendering the current workout’s groups/sets
    private final ObservableList<SetGroup> groups = FXCollections.observableArrayList();

    // Header stats (bound by WorkoutHeaderPane)
    private final StringProperty totalDistanceText = new SimpleStringProperty("-");
    private final StringProperty swimTimeText      = new SimpleStringProperty("-");
    private final StringProperty restTimeText      = new SimpleStringProperty("-");
    private final StringProperty durationText      = new SimpleStringProperty("-");

    // Simple invalidation tick to refresh list cells that don’t observe nested changes
    private final IntegerProperty refreshTick = new SimpleIntegerProperty(0);

    public WorkoutBuilderPresenter(AppState app) {
        this.app = app;

        // Initialize from current workout (if any)
        if (app.getCurrentWorkout() != null) {
            groups.setAll(app.getCurrentWorkout().getGroups());
            computeStats();
        }

        // Watch workout switches
        app.currentWorkoutProperty().addListener((obs, oldW, newW) -> {
            groups.clear();
            if (newW != null) groups.setAll(newW.getGroups());
            computeStats();
        });
    }

    // ---------- Properties used by panes ----------

    public ObservableList<SetGroup> groups() { return groups; }

    public ReadOnlyStringProperty totalDistanceTextProperty() { return totalDistanceText; }
    public ReadOnlyStringProperty swimTimeTextProperty()      { return swimTimeText; }
    public ReadOnlyStringProperty restTimeTextProperty()      { return restTimeText; }
    public ReadOnlyStringProperty durationTextProperty()      { return durationText; }

    public IntegerProperty refreshTickProperty() { return refreshTick; }

    public Workout getDisplayedWorkout() { return app.getCurrentWorkout(); }

    // ---------- Group operations ----------

    public void addGroup(String name) {
        Workout w = app.getCurrentWorkout();
        if (w == null) return;
        String n = (name == null || name.isBlank()) ? "New Group" : name.trim();
        w.addSetGroup(new SetGroup(n));
        groups.setAll(w.getGroups());
        touch();
    }

    public void deleteGroup(int index) {
        Workout w = app.getCurrentWorkout();
        if (w == null || index < 0 || index >= w.getGroupCount()) return;
        w.removeSetGroup(index);
        groups.setAll(w.getGroups());
        touch();
    }

    public void moveGroupUp(int index) {
        Workout w = app.getCurrentWorkout();
        if (w == null || index <= 0 || index >= w.getGroupCount()) return;
        w.swapGroups(index, index - 1);
        groups.setAll(w.getGroups());
        touch();
    }

    public void moveGroupDown(int index) {
        Workout w = app.getCurrentWorkout();
        if (w == null || index < 0 || index >= w.getGroupCount() - 1) return;
        w.swapGroups(index, index + 1);
        groups.setAll(w.getGroups());
        touch();
    }

    // ---------- Set operations ----------

    public void addSet(int groupIndex, SwimSet set) {
        Workout w = app.getCurrentWorkout();
        if (w == null || set == null) return;
        if (groupIndex < 0 || groupIndex >= w.getGroupCount()) return;
        w.getGroups().get(groupIndex).addSet(set);
        touch();
    }

    public void deleteSet(int groupIndex, int setIndex) {
        Workout w = app.getCurrentWorkout();
        if (w == null) return;
        if (groupIndex < 0 || groupIndex >= w.getGroupCount()) return;
        var g = w.getGroups().get(groupIndex);
        if (setIndex < 0 || setIndex >= g.getSetCount()) return;
        g.getSets().remove(setIndex);
        touch();
    }

    public void moveSetUp(int groupIndex, int setIndex) {
        Workout w = app.getCurrentWorkout();
        if (w == null) return;
        if (groupIndex < 0 || groupIndex >= w.getGroupCount()) return;
        var g = w.getGroups().get(groupIndex);
        if (setIndex <= 0 || setIndex >= g.getSetCount()) return;
        var a = g.getSets().get(setIndex - 1);
        var b = g.getSets().get(setIndex);
        g.getSets().set(setIndex - 1, b);
        g.getSets().set(setIndex, a);
        touch();
    }

    public void moveSetDown(int groupIndex, int setIndex) {
        Workout w = app.getCurrentWorkout();
        if (w == null) return;
        if (groupIndex < 0 || groupIndex >= w.getGroupCount()) return;
        var g = w.getGroups().get(groupIndex);
        if (setIndex < 0 || setIndex >= g.getSetCount() - 1) return;
        var a = g.getSets().get(setIndex);
        var b = g.getSets().get(setIndex + 1);
        g.getSets().set(setIndex, b);
        g.getSets().set(setIndex + 1, a);
        touch();
    }

    public void replaceSet(int groupIndex, int setIndex, SwimSet newSet) {
        Workout w = app.getCurrentWorkout();
        if (w == null || newSet == null) return;
        if (groupIndex < 0 || groupIndex >= w.getGroupCount()) return;
        var g = w.getGroups().get(groupIndex);
        if (setIndex < 0 || setIndex >= g.getSetCount()) return;

        g.getSets().set(setIndex, newSet);
        touch();
    }

    // ---------- Helpers ----------

    private void touch() {
        computeStats();
        refreshTick.set(refreshTick.get() + 1);
    }

    private void computeStats() {
        Workout w = app.getCurrentWorkout();
        if (w == null) {
            totalDistanceText.set("-");
            swimTimeText.set("-");
            restTimeText.set("-");
            durationText.set("-");
            return;
        }

        Distance d = w.totalDistance();
        totalDistanceText.set(d.toShortString());

        // If you later add time estimation hooks, populate these:
        swimTimeText.set("—");
        restTimeText.set("—");
        durationText.set("—");
    }
}