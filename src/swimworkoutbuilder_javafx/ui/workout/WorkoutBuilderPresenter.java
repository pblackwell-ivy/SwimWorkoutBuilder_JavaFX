package swimworkoutbuilder_javafx.ui.workout;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import swimworkoutbuilder_javafx.model.SetGroup;
import swimworkoutbuilder_javafx.model.SwimSet;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.store.LocalStore;

public class WorkoutBuilderPresenter {

    private final AppState app;

    private final ObservableList<SetGroup> groups = FXCollections.observableArrayList();

    private final StringProperty totalDistanceText = new SimpleStringProperty("-");
    private final StringProperty swimTimeText      = new SimpleStringProperty("-");
    private final StringProperty restTimeText      = new SimpleStringProperty("-");
    private final StringProperty durationText      = new SimpleStringProperty("-");

    private final IntegerProperty refreshTick = new SimpleIntegerProperty(0);

    // NEW: track unsaved edits
    private final BooleanProperty dirty = new SimpleBooleanProperty(false); // NEW

    public WorkoutBuilderPresenter(AppState app) {
        this.app = app;

        if (app.getCurrentWorkout() != null) {
            groups.setAll(app.getCurrentWorkout().getGroups());
            computeStats();
            dirty.set(false); // NEW
        }

        app.currentWorkoutProperty().addListener((obs, oldW, newW) -> {
            groups.clear();
            if (newW != null) groups.setAll(newW.getGroups());
            computeStats();
            dirty.set(false); // NEW
        });
    }

    // ---------- Properties used by panes ----------

    public ObservableList<SetGroup> groups() { return groups; }

    public ReadOnlyStringProperty totalDistanceTextProperty() { return totalDistanceText; }
    public ReadOnlyStringProperty swimTimeTextProperty()      { return swimTimeText; }
    public ReadOnlyStringProperty restTimeTextProperty()      { return restTimeText; }
    public ReadOnlyStringProperty durationTextProperty()      { return durationText; }

    public IntegerProperty refreshTickProperty() { return refreshTick; }

    public ReadOnlyBooleanProperty dirtyProperty() { return dirty; } // NEW

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
    public void loadFrom(Workout w) {
        if (w == null || w.getGroups() == null) {
            groups.clear();
        } else {
            groups.setAll(w.getGroups());
        }
        dirty.set(false);     // NEW
        bumpRefresh();
    }

    public void commitTo(Workout w) throws IOException {
        if (w == null) return;
        w.getGroups().clear();
        for (var g : groups) {
            w.getGroups().add(new SetGroup(g));
        }
        w.setUpdatedAt(Instant.now());
        LocalStore.saveWorkout(w);
        dirty.set(false);     // NEW
    }

    private void bumpRefresh() {
        try { refreshTick.set(refreshTick.get() + 1); } catch (Throwable ignored) {}
    }

    private void touch() {
        computeStats();
        refreshTick.set(refreshTick.get() + 1);
        dirty.set(true);      // NEW
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
        swimTimeText.set("—");
        restTimeText.set("—");
        durationText.set("—");
    }
}