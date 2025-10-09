package swimworkoutbuilder_javafx.model;

import swimworkoutbuilder_javafx.model.enums.Course;
import swimworkoutbuilder_javafx.model.enums.Effort;
import swimworkoutbuilder_javafx.model.enums.Equipment;
import swimworkoutbuilder_javafx.model.enums.StrokeType;
import swimworkoutbuilder_javafx.model.units.Distance;

import java.util.EnumSet;
import java.util.Set;

/**
 * Represents a single structured training set within a workout,
 * such as <b>“8 × 50 Freestyle @ Endurance”</b>.
 *
 * <p>Each {@code SwimSet} defines the fundamental elements required to compute
 * pacing, rest, and interval timing:
 * <ul>
 *   <li>{@link StrokeType} – which stroke or skill the set trains (e.g., FREESTYLE, DRILL, KICK)</li>
 *   <li>{@code reps} – how many times the distance repeats</li>
 *   <li>{@link Distance} – exact distance per repetition (canonical in meters)</li>
 *   <li>{@link Effort} – pacing intensity (EASY, THRESHOLD, RACE_PACE, etc.)</li>
 *   <li>{@link Course} – pool length and type (SCY, SCM, LCM)</li>
 *   <li>{@link Equipment} – optional training gear (fins, paddles, etc.)</li>
 * </ul></p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Ensure that all distances are valid legal multiples of the course length.</li>
 *   <li>Preserve canonical internal precision using {@link Distance}’s fixed-point model.</li>
 *   <li>Store metadata used by higher-level components like {@link swimworkoutbuilder.model.pacing.DefaultPacePolicy}.</li>
 *   <li>Support immutability of key distance and equipment attributes through controlled setters.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *   <li>{@link Distance} ensures exact math using integer micro-units (0.0001 m precision).</li>
 *   <li>Distances are “snapped up” to legal pool multiples to maintain realistic workout structure.
 *       For example, a 75 m request in a 50 m pool becomes 100 m.</li>
 *   <li>Equipment is modeled as an {@link EnumSet} to efficiently represent multiple active aids.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * SwimSet mainSet = new SwimSet(
 *     StrokeType.FREESTYLE,
 *     8,
 *     Distance.ofYards(50),
 *     Effort.THRESHOLD,
 *     Course.SCY,
 *     "Descend 1–4"
 * );
 * }</pre>
 *
 * @author Parker Blackwell
 * @version MVP 1.0 (October 2025)
 * @see swimworkoutbuilder.model.units.Distance
 * @see swimworkoutbuilder.model.enums.Course
 * @see swimworkoutbuilder.model.enums.Effort
 * @see swimworkoutbuilder.model.enums.StrokeType
 * @see swimworkoutbuilder.model.enums.Equipment
 */
public class SwimSet implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private StrokeType stroke;
    private int reps;
    private Distance distancePerRep; // exact canonical distance per repetition
    private Effort effort;
    private String notes;            // optional user notes
    private Course course;           // pool context for snapping distance
    private Set<Equipment> equipment = EnumSet.noneOf(Equipment.class); // defaults to none

    // inside class SwimSet (add with your other fields)
    private swimworkoutbuilder_javafx.model.units.TimeSpan interval;   // nullable
    private swimworkoutbuilder_javafx.model.units.TimeSpan goalTime;   // nullable

    public swimworkoutbuilder_javafx.model.units.TimeSpan getInterval() { return interval; }
    public void setInterval(swimworkoutbuilder_javafx.model.units.TimeSpan interval) { this.interval = interval; }

    public swimworkoutbuilder_javafx.model.units.TimeSpan getGoalTime() { return goalTime; }
    public void setGoalTime(swimworkoutbuilder_javafx.model.units.TimeSpan goalTime) { this.goalTime = goalTime; }
    // ----------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------

    // --- Convenience no-arg constructor for UI dialogs ---
    public SwimSet() {
        // choose safe, sane defaults that won't explode on setters
        this.stroke = null;
        this.reps = 1;
        this.course = swimworkoutbuilder_javafx.model.enums.Course.SCY; // a default so distance snapping works
        this.distancePerRep = swimworkoutbuilder_javafx.model.units.Distance.ofYards(25);
        this.effort = null;
        this.notes = "";
        this.equipment = java.util.EnumSet.noneOf(swimworkoutbuilder_javafx.model.enums.Equipment.class);
        this.interval = null;
        this.goalTime = null;
    }

    /**
     * Creates a new {@code SwimSet} with all core properties specified.
     *
     * <p>Performs validation on reps, distance, and course,
     * and snaps the given distance up to the nearest legal pool multiple
     * based on the course length (e.g., rounding 75 m to 100 m in a 50 m pool).</p>
     *
     * @param stroke stroke type (e.g., FREESTYLE, BACKSTROKE)
     * @param reps number of repetitions (must be ≥ 1)
     * @param distancePerRep distance for each repetition
     * @param effort intensity level of the set
     * @param course course type (SCY, SCM, or LCM)
     * @param notes optional descriptive text
     * @throws IllegalArgumentException if {@code reps < 1} or distance/course are invalid
     */
    public SwimSet(StrokeType stroke,
                   int reps,
                   Distance distancePerRep,
                   Effort effort,
                   Course course,
                   String notes) {
        if (reps < 1) throw new IllegalArgumentException("reps must be >= 1");
        if (distancePerRep == null || distancePerRep.rawMicroUnits() <= 0)
            throw new IllegalArgumentException("distancePerRep must be > 0");
        if (course == null) throw new IllegalArgumentException("course must not be null");

        this.stroke = stroke;
        this.reps = reps;
        this.effort = effort;
        this.notes = (notes == null ? "" : notes);
        this.course = course;
        this.distancePerRep = snapUpToCourseMultiple(distancePerRep, course);
    }

    /** Convenience constructor for sets without notes. */
    public SwimSet(StrokeType stroke,
                   int reps,
                   Distance distancePerRep,
                   Effort effort,
                   Course course) {
        this(stroke, reps, distancePerRep, effort, course, "");
    }

    // ----------------------------------------------------------
    // Accessors and mutators
    // ----------------------------------------------------------

    public StrokeType getStroke() { return stroke; }
    public void setStroke(StrokeType stroke) { this.stroke = stroke; }

    public int getReps() { return reps; }
    public void setReps(int reps) {
        if (reps < 1) throw new IllegalArgumentException("reps must be >= 1");
        this.reps = reps;
    }

    public Distance getDistancePerRep() { return distancePerRep; }
    public void setDistancePerRep(Distance distancePerRep) {
        if (distancePerRep == null || distancePerRep.rawMicroUnits() <= 0)
            throw new IllegalArgumentException("distancePerRep must be > 0");
        this.distancePerRep = snapUpToCourseMultiple(distancePerRep, this.course);
    }

    public Effort getEffort() { return effort; }
    public void setEffort(Effort effort) { this.effort = effort; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = (notes == null ? "" : notes); }

    public Course getCourse() { return course; }
    public void setCourse(Course course) {
        if (course == null) throw new IllegalArgumentException("course must not be null");
        this.course = course;
        // Resnap distance whenever the course changes (e.g., switching SCY → LCM)
        this.distancePerRep = snapUpToCourseMultiple(this.distancePerRep, this.course);
    }

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

    // ----------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------

    /**
     * Snaps a distance upward to the nearest legal pool multiple for the specified course.
     * <p>Uses integer math in canonical 0.0001 m units, ensuring there is no rounding drift.</p>
     *
     * @param distance the distance to normalize
     * @param course the pool course (e.g., 25 yd, 25 m, 50 m)
     * @return a new {@link Distance} object rounded up to the nearest course multiple
     */
    private static Distance snapUpToCourseMultiple(Distance distance, Course course) {
        Distance poolLen = course.getLength();
        long d = distance.rawMicroUnits();
        long p = poolLen.rawMicroUnits();
        if (p <= 0) return distance; // safety guard

        long multiples = d / p;
        if (d % p != 0) multiples++; // round UP
        long snapped = Math.max(p, Math.multiplyExact(multiples, p)); // ensure ≥ 1 pool length

        // Preserve user’s preferred display unit
        return Distance.ofCanonicalMicroUnits(snapped, distance.displayUnit());
    }

    @Override
    public String toString() {
        return "SwimSet{" +
                "stroke=" + stroke +
                ", reps=" + reps +
                ", distancePerRep=" + distancePerRep +
                ", effort=" + effort +
                ", course=" + course +
                (equipment != null && !equipment.isEmpty() ? ", equipment=" + equipment : "") +
                (notes != null && !notes.isBlank() ? ", notes='" + notes + '\'' : "") +
                '}';
    }
}
