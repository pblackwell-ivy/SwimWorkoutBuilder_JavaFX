package swimworkoutbuilder_javafx.model;

import swimworkoutbuilder_javafx.model.enums.StrokeType;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.model.units.TimeSpan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A logical collection of SwimSets that may repeat.
 * Supports rest between sets (inside the group) and rest between group repeats.
 */
public class SetGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private int order = 0;
    private int reps = 1;
    private String notes;

    /** New: rest between sets within the group (seconds, >=0) */
    private int restBetweenSetsSec = 0;

    /** Already existed: rest between group repetitions (seconds, >=0) */
    private int restAfterGroupSec = 0;

    private final List<SwimSet> sets = new ArrayList<>();

    public SetGroup() {}

    public SetGroup(String name) {
        this.name = name;
    }

    // --- Getters/Setters ---

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = Math.max(1, reps); }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getRestBetweenSetsSec() { return Math.max(0, restBetweenSetsSec); }
    public void setRestBetweenSetsSec(int restBetweenSetsSec) { this.restBetweenSetsSec = Math.max(0, restBetweenSetsSec); }

    public int getRestAfterGroupSec() { return Math.max(0, restAfterGroupSec); }
    public void setRestAfterGroupSec(int restAfterGroupSec) { this.restAfterGroupSec = Math.max(0, restAfterGroupSec); }

    public List<SwimSet> getSets() { return sets; }
    public void addSet(SwimSet s) { if (s != null) sets.add(s); }

    // --- Distance helpers (group-level) ---------------------------------

    /** Number of SwimSet rows in this group. */
    public int getSetCount() { return sets.size(); }

    /** Distance for one pass through this group (sum of all sets, no group reps). */
    public swimworkoutbuilder_javafx.model.units.Distance singlePassDistance() {
        swimworkoutbuilder_javafx.model.units.Distance sum =
                swimworkoutbuilder_javafx.model.units.Distance.ofMeters(0);
        for (SwimSet s : sets) {
            // set distance = per-rep distance × set reps
            sum = sum.plus(s.getDistancePerRep().times(s.getReps()));
        }
        return sum;
    }

    /** Total distance for the entire group, including group repetitions (e.g., “Main ×4”). */
    public swimworkoutbuilder_javafx.model.units.Distance totalDistance() {
        return singlePassDistance().times(Math.max(1, reps));
    }

    /** Convenience (meters, rounded) for legacy callers. */
    public int singlePassDistanceMeters() {
        return (int) Math.round(singlePassDistance().toMeters());
    }

    /** Convenience (meters, rounded) for legacy callers. */
    public int totalDistanceMeters() {
        return (int) Math.round(totalDistance().toMeters());
    }

    @Override
    public String toString() {
        return "SetGroup{" +
                "name='" + name + '\'' +
                ", order=" + order +
                ", reps=" + reps +
                ", restBetweenSetsSec=" + restBetweenSetsSec +
                ", restAfterGroupSec=" + restAfterGroupSec +
                ", notes='" + notes + '\'' +
                ", sets=" + sets.size() +
                '}';
    }
}