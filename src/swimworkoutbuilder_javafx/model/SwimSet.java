package swimworkoutbuilder_javafx.model;

// ------------------------------------------------------------
// Imports
// ------------------------------------------------------------
import swimworkoutbuilder_javafx.model.enums.Course;
import swimworkoutbuilder_javafx.model.enums.Effort;
import swimworkoutbuilder_javafx.model.enums.Equipment;
import swimworkoutbuilder_javafx.model.enums.StrokeType;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.model.units.TimeSpan;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * One swim training set (leaf of the workout tree), e.g. “8×50 Free @ Threshold”.
 *
 * <p>Holds the core attributes needed for pacing and display:
 * stroke, reps, per-rep distance (canonical via {@link Distance}),
 * effort, course, equipment, and optional per-rep goal/interval times.</p>
 *
 * <p><b>Key features:</b>
 * <ul>
 *   <li>Distance is snapped up to a legal multiple of the pool length for the {@link Course}.</li>
 *   <li>Mutable model with validated setters; safe defaults in the no-arg ctor.</li>
 *   <li>Deep-copy and repository/loader constructors for cloning and persistence.</li>
 * </ul>
 *
 * @author Parker Blackwell
 * @version 1.1
 * @since 2025-10-05
 */
public final class SwimSet implements Serializable {
    private static final long serialVersionUID = 1L;

    // ------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------
    private StrokeType stroke;            // nullable until user selects
    private int reps;                     // >= 1
    private Distance distancePerRep;      // per-rep distance (canonical, snapped to course)
    private Effort effort;                // nullable until chosen
    private Course course;                // SCY/SCM/LCM; used for snapping
    private String notes;                 // optional

    private Set<Equipment> equipment = EnumSet.noneOf(Equipment.class);

    // Optional timing (per rep)
    private TimeSpan interval;            // nullable: target interval “@”
    private TimeSpan goalTime;            // nullable: target goal time

    // ------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------

    /** No-arg constructor with safe defaults (handy for dialogs). */
    public SwimSet() {
        this.stroke  = null;
        this.reps    = 1;
        this.course  = Course.SCY;                         // default so snapping works
        this.distancePerRep = Distance.ofYards(25);        // 1 pool length in default course
        this.effort  = null;
        this.notes   = "";
        this.equipment = EnumSet.noneOf(Equipment.class);
        this.interval = null;
        this.goalTime = null;

        // ensure distance is consistent with the default course
        this.distancePerRep = snapUpToCourseMultiple(this.distancePerRep, this.course);
    }

    /** Convenience constructor without notes. */
    public SwimSet(StrokeType stroke,
                   int reps,
                   Distance distancePerRep,
                   Effort effort,
                   Course course) {
        this(stroke, reps, distancePerRep, effort, course, "", null, null);
    }

    /** Full constructor (typical use). */
    public SwimSet(StrokeType stroke,
                   int reps,
                   Distance distancePerRep,
                   Effort effort,
                   Course course,
                   String notes) {
        this(stroke, reps, distancePerRep, effort, course, notes, null, null);
    }

    /**
     * Full constructor including optional per-rep interval/goal times.
     * Validates reps, distance, and course; snaps distance to pool multiples.
     */
    public SwimSet(StrokeType stroke,
                   int reps,
                   Distance distancePerRep,
                   Effort effort,
                   Course course,
                   String notes,
                   TimeSpan interval,
                   TimeSpan goalTime) {
        if (reps < 1) throw new IllegalArgumentException("reps must be >= 1");
        Objects.requireNonNull(distancePerRep, "distancePerRep");
        if (distancePerRep.rawMicroUnits() <= 0) throw new IllegalArgumentException("distancePerRep must be > 0");
        this.course = Objects.requireNonNull(course, "course");

        this.stroke = stroke;
        this.reps   = reps;
        this.effort = effort;
        this.notes  = (notes == null ? "" : notes);
        this.distancePerRep = snapUpToCourseMultiple(distancePerRep, this.course);
        this.interval = interval;
        this.goalTime = goalTime;
    }

    /**
     * Repository/loader constructor.
     * Use when reconstructing from storage with exact values (no snapping surprises).
     * Caller should ensure values are valid.
     */
    public SwimSet(StrokeType stroke,
                   int reps,
                   Distance distancePerRep,
                   Effort effort,
                   Course course,
                   String notes,
                   Set<Equipment> equipment,
                   TimeSpan interval,
                   TimeSpan goalTime) {
        this.stroke = stroke;
        this.reps   = Math.max(1, reps);
        this.course = Objects.requireNonNull(course, "course");
        this.distancePerRep = Objects.requireNonNull(distancePerRep, "distancePerRep");
        this.effort = effort;
        this.notes  = (notes == null ? "" : notes);
        this.equipment = (equipment == null) ? EnumSet.noneOf(Equipment.class) : EnumSet.copyOf(equipment);
        this.interval = interval;
        this.goalTime = goalTime;
        // (Assume persisted distance already respects course; if not, consumer may re-snap.)
    }

    /** Deep copy constructor. */
    public SwimSet(SwimSet other) {
        Objects.requireNonNull(other, "other");
        this.stroke = other.stroke;
        this.reps   = other.reps;
        this.distancePerRep = other.distancePerRep; // Distance is an immutable value object in this model
        this.effort = other.effort;
        this.course = other.course;
        this.notes  = other.notes;
        this.equipment = other.equipment == null
                ? EnumSet.noneOf(Equipment.class)
                : EnumSet.copyOf(other.equipment);
        this.interval = other.interval;   // TimeSpan is an immutable value object in this model
        this.goalTime = other.goalTime;
    }

    // ------------------------------------------------------------
    // Accessors / Mutators
    // ------------------------------------------------------------

    public StrokeType getStroke() { return stroke; }
    public void setStroke(StrokeType stroke) { this.stroke = stroke; }

    public int getReps() { return reps; }
    public void setReps(int reps) {
        if (reps < 1) throw new IllegalArgumentException("reps must be >= 1");
        this.reps = reps;
    }

    public Distance getDistancePerRep() { return distancePerRep; }
    public void setDistancePerRep(Distance distancePerRep) {
        Objects.requireNonNull(distancePerRep, "distancePerRep");
        if (distancePerRep.rawMicroUnits() <= 0) throw new IllegalArgumentException("distancePerRep must be > 0");
        this.distancePerRep = snapUpToCourseMultiple(distancePerRep, this.course);
    }

    public Effort getEffort() { return effort; }
    public void setEffort(Effort effort) { this.effort = effort; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) {
        this.course = Objects.requireNonNull(course, "course");
        // Re-snap the current distance for the new course context
        this.distancePerRep = snapUpToCourseMultiple(this.distancePerRep, this.course);
    }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = (notes == null ? "" : notes); }

    public Set<Equipment> getEquipment() { return equipment; }
    public void setEquipment(Set<Equipment> equipment) {
        this.equipment = (equipment == null)
                ? EnumSet.noneOf(Equipment.class)
                : EnumSet.copyOf(equipment);
    }

    public void addEquipment(Equipment e) {
        if (e == null) return;
        if (equipment == null) equipment = EnumSet.noneOf(Equipment.class);
        equipment.add(e);
    }

    public void removeEquipment(Equipment e) {
        if (e == null || equipment == null) return;
        equipment.remove(e);
    }

    public boolean hasEquipment(Equipment e) {
        return equipment != null && equipment.contains(e);
    }

    public TimeSpan getInterval() { return interval; }
    public void setInterval(TimeSpan interval) { this.interval = interval; }

    public TimeSpan getGoalTime() { return goalTime; }
    public void setGoalTime(TimeSpan goalTime) { this.goalTime = goalTime; }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    /**
     * Snap a distance upward to the nearest legal multiple of the pool length.
     * Uses canonical (0.0001 m) integer math via {@link Distance#rawMicroUnits()}.
     */
    private static Distance snapUpToCourseMultiple(Distance distance, Course course) {
        Distance poolLen = course.getLength();
        long d = distance.rawMicroUnits();
        long p = poolLen.rawMicroUnits();
        if (p <= 0) return distance; // safety

        long multiples = d / p;
        if (d % p != 0) multiples++;              // round UP to next whole length
        long snapped = Math.max(p, multiples * p); // at least 1 length

        // Preserve original preferred display unit
        return Distance.ofCanonicalMicroUnits(snapped, distance.displayUnit());
    }

    // ------------------------------------------------------------
    // Object overrides
    // ------------------------------------------------------------

    @Override
    public String toString() {
        return "SwimSet{" +
                "stroke=" + stroke +
                ", reps=" + reps +
                ", distancePerRep=" + distancePerRep +
                ", effort=" + effort +
                ", course=" + course +
                (equipment != null && !equipment.isEmpty() ? ", equipment=" + equipment : "") +
                (interval != null ? ", interval=" + interval : "") +
                (goalTime != null ? ", goalTime=" + goalTime : "") +
                (notes != null && !notes.isBlank() ? ", notes='" + notes + '\'' : "") +
                '}';
    }
}