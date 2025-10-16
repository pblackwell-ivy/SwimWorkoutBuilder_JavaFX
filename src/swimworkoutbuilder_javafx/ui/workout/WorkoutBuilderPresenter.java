package swimworkoutbuilder_javafx.ui.workout;

import java.io.IOException;
import java.time.Instant;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import swimworkoutbuilder_javafx.model.SetGroup;
import swimworkoutbuilder_javafx.model.SwimSet;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.model.enums.Course;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.store.LocalStore;
import swimworkoutbuilder_javafx.model.pacing.DefaultPacePolicy;
import swimworkoutbuilder_javafx.model.pacing.PacePolicy;
import swimworkoutbuilder_javafx.model.units.TimeSpan;

/**
 * [Presenter] WorkoutBuilderPresenter for the "workout" feature.
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *   <li>Expose observable state to the view</li>
 *   <li>Handle user intents and orchestrate updates</li>
 *   <li>Coordinate with stores/services for data operations</li>
 * </ul>
 *
 * <p><b>Design Notes:</b>
 * <ul>
 *   <li>Lightweight MVP (Presenter + Pane)</li>
 *   <li>No blocking work on FX thread; delegate background tasks</li>
 *   <li>Business logic lives in services/stores</li>
 * </ul>
 *
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * // Typical usage for WorkoutBuilderPresenter
 * WorkoutBuilderPresenter obj = new WorkoutBuilderPresenter();
 * obj.toString(); // replace with real usage
 * }</pre>
 *
 * @author Parker Blackwell
 * @version 1.0
 * @since 2025-10-14
 */


public class WorkoutBuilderPresenter {

    private final AppState app;
    private static final PacePolicy POLICY = new DefaultPacePolicy();

    private final ObservableList<SetGroup> groups = FXCollections.observableArrayList();

    private final StringProperty totalDistanceText = new SimpleStringProperty("-");
    private final StringProperty swimTimeText      = new SimpleStringProperty("-");
    private final StringProperty restTimeText      = new SimpleStringProperty("-");
    private final StringProperty durationText      = new SimpleStringProperty("-");

    private final IntegerProperty refreshTick = new SimpleIntegerProperty(0);

    // track unsaved edits
    private final BooleanProperty dirty = new SimpleBooleanProperty(false); 

    public WorkoutBuilderPresenter(AppState app) {
        this.app = app;

        if (app.getCurrentWorkout() != null) {
            groups.setAll(app.getCurrentWorkout().getGroups());
            computeStats();
            dirty.set(false); 
        }

        app.currentWorkoutProperty().addListener((obs, oldW, newW) -> {
            groups.clear();
            if (newW != null) groups.setAll(newW.getGroups());
            computeStats();
            dirty.set(false); 
        });
    }

    // ---------- Properties used by panes ----------

    public ObservableList<SetGroup> groups() { return groups; }

    public ReadOnlyStringProperty totalDistanceTextProperty() { return totalDistanceText; }
    public ReadOnlyStringProperty swimTimeTextProperty()      { return swimTimeText; }
    public ReadOnlyStringProperty restTimeTextProperty()      { return restTimeText; }
    public ReadOnlyStringProperty durationTextProperty()      { return durationText; }

    public IntegerProperty refreshTickProperty() { return refreshTick; }

    public ReadOnlyBooleanProperty dirtyProperty() { return dirty; } 

    public Workout getDisplayedWorkout() { return app.getCurrentWorkout(); }

    // ---------- Header operations ----------
    // called by header Save button
    public void saveHeaderEdits(String name, String notes, Course course) {
        Workout w = app.getCurrentWorkout();
        if (w == null) return;

        if (name != null)  w.setName(name.trim());
        if (notes != null) w.setNotes(notes.trim());
        if (course != null) w.setCourse(course);

        computeStats();                          // keep summary current
        persist(w);                              // write to disk (+ updatedAt)
        refreshTick.set(refreshTick.get() + 1);  // nudge UI to refresh
    }

    // ---------- Group operations ----------
    public void addGroup(String name, int reps, String notes) {                 
        var w = app.getCurrentWorkout();                                        
        if (w == null) return;                                                  
        String n = (name == null || name.isBlank()) ? "New Group" : name.trim();
        var g = new swimworkoutbuilder_javafx.model.SetGroup(n);                
        g.setReps(Math.max(1, reps));                                           
        if (notes != null && !notes.isBlank()) g.setNotes(notes.trim());        
        w.addSetGroup(g);                                                        
        groups.setAll(w.getGroups());                                           
        touch();                                                                 
    }                                                                            

    public void updateGroup(int index, String name, int reps, String notes) {
        Workout w = app.getCurrentWorkout();
        if (w == null) return;
        if (index < 0 || index >= w.getGroupCount()) return;

        SetGroup g = w.getGroups().get(index);
        if (name != null)  g.setName(name.trim());
        if (reps > 0)      g.setReps(reps);
        g.setNotes((notes == null) ? "" : notes.trim());

        touch(); // recompute + tick + mark dirty
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
    // --- add these methods anywhere in the class body (e.g., after your set ops) ---

    /** Update name/notes from the header and persist. */
    public void updateHeader(String name, String notes) {                   
        Workout w = app.getCurrentWorkout();                                
        if (w == null) return;                                              
        w.setName(name == null ? "" : name.trim());                         
        w.setNotes(notes == null ? "" : notes.trim());                      
        w.setUpdatedAt(Instant.now());                                      
        try { LocalStore.saveWorkout(w); } catch (Exception ignored) {}     
        computeStats();                                                     
        refreshTick.set(refreshTick.get() + 1);                             
    }                                                                       

    /** Change pool length (Course)
     * Rounds each set's per-rep distance up to the nearest multiple of the pool length.
     * Flips the set's course and the workout's course, recomputes stats, and refreshes the UI.
     * @param newCourse
     */
    public void changeCourse(Course newCourse) {
        Workout w = app.getCurrentWorkout();
        if (w == null || newCourse == null) return;

        // Target pool length in *target* units
        final int poolLen = (newCourse == Course.LCM) ? 50 : 25;

        // Walk groups/sets and replace each set with a rounded copy
        for (int gi = 0; gi < w.getGroupCount(); gi++) {
            var g = w.getGroups().get(gi);
            for (int si = 0; si < g.getSetCount(); si++) {
                SwimSet old = g.getSets().get(si);

                // 1) convert current per-rep distance to the *target* unit (rounded to nearest int)
                long amountInTarget = (newCourse == Course.SCY)
                        ? Math.round(old.getDistancePerRep().toYards())
                        : Math.round(old.getDistancePerRep().toMeters());

                // 2) round UP to nearest multiple of poolLen
                int rounded = roundToNearestMultiple((int)Math.max(0, amountInTarget), poolLen);
                if (rounded == 0 && amountInTarget > 0) rounded = poolLen; // keep at least 1 length if there was intent

                // 3) build the new Distance in target units
                Distance newDist = (newCourse == Course.SCY)
                        ? Distance.ofYards(rounded)
                        : Distance.ofMeters(rounded);

                // 4) replace the set (copying all other fields)
                SwimSet neu = new SwimSet(
                        old.getStroke(),
                        old.getReps(),
                        newDist,
                        old.getEffort(),
                        newCourse,
                        old.getNotes()
                );
                if (old.getGoalTime() != null) neu.setGoalTime(old.getGoalTime());

                g.getSets().set(si, neu);
            }
        }

        // Flip workout course and refresh
        w.setCourse(newCourse);
        computeStats();
        refreshTick.set(refreshTick.get() + 1);
    }


    // Helper for pool length changes
    private static int roundToNearestMultiple(int value, int base) {
        if (base <= 0) return value;
        if (value <= 0) return 0;
        // round-half-up to nearest multiple of base
        int q = (value + base / 2) / base;
        return q * base;
    }

    /** Delete the current workout from disk and clear the selection. */
    public void deleteCurrentWorkout() {
        var w = app.getCurrentWorkout();
        if (w == null) return;

        try {
            LocalStore.deleteWorkout(w.getId());   // delete the .bin file on disk  [oai_citation:0‡store_java_code.txt](sediment://file_0000000007d461f79984bdda06ed1b18)
        } catch (IOException ex) {
            // Surface a simple error dialog; keep it quiet if you prefer
            new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR,
                    "Delete failed: " + ex.getMessage()
            ).showAndWait();
            return;
        }

        app.setCurrentWorkout(null);   // clear UI selection
        groups.clear();                // clear working list
        computeStats();                // reset header stats
        refreshTick.set(refreshTick.get() + 1); // pulse UI
    }

    public void loadFrom(Workout w) {
        if (w == null || w.getGroups() == null) {
            groups.clear();
        } else {
            groups.setAll(w.getGroups());
        }
        dirty.set(false);     
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
        dirty.set(false);     
    }

    private void bumpRefresh() {
        try { refreshTick.set(refreshTick.get() + 1); } catch (Throwable ignored) {}
    }

    private void touch() {
        computeStats();
        refreshTick.set(refreshTick.get() + 1);
        dirty.set(true);      
    }

    private void persist(Workout w) {
        try {
            w.setUpdatedAt(Instant.now());
            LocalStore.saveWorkout(w);
        } catch (IOException ex) {
            ex.printStackTrace(); // TODO: surface to UI if you want
        }
    }

    // exact integer conversions (half-up)
    private static long metersToYardsRounded(long meters) {
        // yards = round(meters / 0.9144)  ==> (meters * 10_000 + 9_144/2) / 9_144
        return (meters * 10_000L + 9_144L / 2) / 9_144L;
    }

    // Sum in *yards* (integers) to avoid meter→yard rounding drift.
    private static long totalYardsInt(Workout w) {
        if (w == null || w.getGroups() == null) return 0L;
        long yards = 0L;
        for (var g : w.getGroups()) {
            if (g == null || g.getSets() == null) continue;
            long groupYards = 0L;
            for (var s : g.getSets()) {
                if (s == null || s.getDistancePerRep() == null) continue;
                // Round each per-rep yard value, multiply by reps, then sum.
                long perRepYards = Math.round(s.getDistancePerRep().toYards());
                groupYards += perRepYards * Math.max(1, s.getReps());
            }
            yards += groupYards * Math.max(1, g.getReps());
        }
        return yards;
    }

    // Replace the entire computeStats() method with this:
    private void computeStats() {
        Workout w = app.getCurrentWorkout();
        if (w == null) {
            totalDistanceText.set("-");
            swimTimeText.set("-");
            restTimeText.set("-");
            durationText.set("-");
            return;
        }

        // --- Distance text (unchanged behavior) ---
        long meters = Math.round(w.totalDistance().toMeters());
        Course course = (w.getCourse() == null) ? Course.SCY : w.getCourse();
        String distText = switch (course) {
            case SCY -> totalYardsInt(w) + " yd";
            case SCM, LCM -> meters + " m";
        };
        totalDistanceText.set(distText);

        // --- Time totals via pacing policy ---
        var swimmer = app.getCurrentSwimmer();
        if (swimmer == null) {
            swimTimeText.set("—");
            restTimeText.set("—");
            durationText.set("—");
            return;
        }

        double swimSec = 0.0;
        double restSec = 0.0;

        try {
            if (w.getGroups() != null) {
                for (var g : w.getGroups()) {
                    if (g == null || g.getSets() == null) continue;
                    int groupReps = Math.max(1, g.getReps());

                    for (int gr = 0; gr < groupReps; gr++) {
                        for (var s : g.getSets()) {
                            if (s == null) continue;
                            int setReps = Math.max(1, s.getReps());
                            for (int r = 0; r < setReps; r++) {
                                // Policy works in canonical meters + your multipliers
                                double gSecs = Math.max(0.0, POLICY.goalSeconds(w, s, swimmer, r));
                                double rSecs = Math.max(0.0, POLICY.restSeconds(w, s, swimmer, r));
                                swimSec += gSecs;
                                restSec += rSecs;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // If seeds are missing or anything throws, fall back to blanks
            swimTimeText.set("—");
            restTimeText.set("—");
            durationText.set("—");
            return;
        }

        // Format using your TimeSpan
        TimeSpan swim = TimeSpan.ofSeconds(swimSec);
        TimeSpan rest = TimeSpan.ofSeconds(restSec);
        TimeSpan total = TimeSpan.ofSeconds(swimSec + restSec);

        swimTimeText.set(swim.toString());
        restTimeText.set(rest.toString());
        durationText.set(total.toString());
    }
}
