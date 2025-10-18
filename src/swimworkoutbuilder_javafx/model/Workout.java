package swimworkoutbuilder_javafx.model;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import swimworkoutbuilder_javafx.model.enums.Course;
import swimworkoutbuilder_javafx.model.units.Distance;

/**
 * Represents a structured swim workout for a specific swimmer.
 *
 * <p>{@code Workout} acts as a top-level container for all workout content —
 * including metadata (name, notes, course) and a sequence of {@link SetGroup}s.
 * It does not perform any pace or timing logic; those calculations are handled by
 * the {@link swimworkoutbuilder_javafx.model.pacing.PacePolicy} implementations.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Encapsulate all metadata describing a workout (name, course, notes, swimmer ID).</li>
 *   <li>Contain an ordered list of {@link SetGroup}s representing warmup, main, and cooldown phases.</li>
 *   <li>Provide helper methods for reordering and measuring total workout distance.</li>
 *   <li>Ensure all instances are associated with a swimmer via {@code swimmerId}.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *   <li>Workouts are immutable in ID but mutable in content (groups can be edited or rearranged).</li>
 *   <li>Distances are measured canonically in meters via {@link Distance} for consistency.</li>
 *   <li>This class is model-only; the UI and pacing logic are defined elsewhere.</li>
 * </ul>
 *
 * <h2>Typical Usage</h2>
 * <pre>{@code
 * UUID swimmerId = swimmer.getId();
 * Workout w = new Workout(swimmerId, "Tuesday Threshold", Course.SCY);
 *
 * SetGroup warmup = new SetGroup("Warmup", 1, 1);
 * warmup.addSet(new SwimSet(StrokeType.FREESTYLE, 4, Distance.ofYards(50), Effort.EASY, w.getCourse(), "Smooth strokes"));
 *
 * w.addSetGroup(warmup);
 * System.out.println(w.totalDistance());
 * }</pre>
 *
 * @author Parker Blackwell
 * @version MVP 1.0 (October 2025)
 * @see SetGroup
 * @see swimworkoutbuilder_javafx.model.pacing.PacePolicy
 */
public class Workout implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    // ----------------------------------------------------------
    // Identity & metadata
    // ----------------------------------------------------------

    private final UUID id;                    // Unique workout ID
    private UUID swimmerId;                   // The swimmer this workout belongs to
    private String name;                      // e.g., "Tuesday Threshold"
    private Course course;                    // SCY, SCM, or LCM
    private String notes;                     // Optional workout-level notes (theme, focus)
    private final Instant createdAt;          // When workout was created
    private Instant updatedAt;                // When workout last updated

    // Defaults (used by printer between groups)
    private int defaultRestBetweenGroupsSeconds = 60;

    // Contents
    private final List<SetGroup> groups = new ArrayList<>();

    // ----------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------

    /**
     * Creates a new workout with minimal metadata.
     *
     * @param swimmerId the swimmer this workout belongs to
     * @param name the descriptive name of the workout (e.g. "Tuesday Threshold")
     * @param course the pool course type (SCY, SCM, or LCM)
     */
    public Workout(UUID swimmerId, String name, Course course) {
        this.id = UUID.randomUUID();
        this.swimmerId = Objects.requireNonNull(swimmerId, "swimmerId");
        this.name = Objects.requireNonNull(name, "name");
        this.course = Objects.requireNonNull(course, "course");
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Creates a workout with full metadata and default rest settings.
     *
     * @param swimmerId the swimmer this workout belongs to
     * @param name descriptive workout name
     * @param course pool course type (SCY, SCM, or LCM)
     * @param notes optional notes or theme
     * @param defaultRestBetweenGroupsSeconds default rest between consecutive groups (≥0)
     */
    public Workout(UUID swimmerId, String name, Course course, String notes, int defaultRestBetweenGroupsSeconds) {
        this.id = UUID.randomUUID();
        this.swimmerId = Objects.requireNonNull(swimmerId, "swimmerId");
        this.name = Objects.requireNonNull(name, "name");
        this.course = Objects.requireNonNull(course, "course");
        this.notes = notes;
        this.defaultRestBetweenGroupsSeconds = Math.max(0, defaultRestBetweenGroupsSeconds);
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    // Repository/loader (exact values from storage)
    public Workout(UUID id, UUID swimmerId, String name, Course course, String notes,
                   int defaultRestBetweenGroupsSeconds, java.time.Instant createdAt, java.time.Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.swimmerId = Objects.requireNonNull(swimmerId, "swimmerId");
        this.name = Objects.requireNonNull(name, "name");
        this.course = Objects.requireNonNull(course, "course");
        this.notes = notes;
        this.defaultRestBetweenGroupsSeconds = Math.max(0, defaultRestBetweenGroupsSeconds);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    // Deep copy (same logical workout id; copies groups)
    public Workout(Workout other) {
        this.id = Objects.requireNonNull(other, "other").id;
        this.swimmerId = other.swimmerId;
        this.name = other.name;
        this.course = other.course;
        this.notes = other.notes;
        this.defaultRestBetweenGroupsSeconds = other.defaultRestBetweenGroupsSeconds;
        this.createdAt = other.createdAt;
        this.updatedAt = other.updatedAt;
        for (SetGroup g : other.getGroups()) {
            if (g != null) this.getGroups().add(g.deepCopy());
        }
    }

    // ----------------------------------------------------------
    // Basic getters/setters
    // ----------------------------------------------------------

    /** Unique identifier for this workout (immutable once created). */
    public UUID getId() { return id; }

    public UUID getSwimmerId() { return swimmerId; }
    public void setSwimmerId(UUID swimmerId) {
        this.swimmerId = Objects.requireNonNull(swimmerId, "swimmerId");
        touchUpdated();
    }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
        touchUpdated();
    }

    public Course getCourse() { return course; }
    public void setCourse(Course course) {
        this.course = Objects.requireNonNull(course, "course");
        touchUpdated();
    }

    public String getNotes() { return notes; }
    public void setNotes(String notes) {
        this.notes = notes;
        touchUpdated();
    }

    public int getDefaultRestBetweenGroupsSeconds() { return defaultRestBetweenGroupsSeconds; }
    public void setDefaultRestBetweenGroupsSeconds(int seconds) {
        this.defaultRestBetweenGroupsSeconds = Math.max(0, seconds);
        touchUpdated();
    }

    // Timestamps
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }
    public void touchUpdated() { this.updatedAt = Instant.now(); }

    // ----------------------------------------------------------
    // Group management (ordered & mutable)
    // ----------------------------------------------------------

    /** Returns the list of {@link SetGroup}s that make up this workout. */
    public List<SetGroup> getGroups() { return groups; }

    /** Returns how many groups this workout currently contains. */
    public int getGroupCount() { return groups.size(); }

    /** Appends a new group to the workout. Null values are ignored. */
    public void addSetGroup(SetGroup group) {
        if (group != null) groups.add(group);
    }

    /** Inserts a group at a specific index (throws if index invalid). */
    public void insertSetGroup(int index, SetGroup group) {
        if (group == null) return;
        groups.add(index, group);
    }

    /** Removes and returns the group at the specified index. */
    public SetGroup removeSetGroup(int index) {
        return groups.remove(index);
    }

    /** Moves a group from one index to another, preserving relative order. */
    public void moveGroup(int fromIndex, int toIndex) {
        if (fromIndex == toIndex) return;
        SetGroup g = groups.remove(fromIndex);
        groups.add(toIndex, g);
    }

    /** Swaps the position of two groups in the list. */
    public void swapGroups(int i, int j) {
        if (i == j) return;
        SetGroup a = groups.get(i);
        SetGroup b = groups.get(j);
        groups.set(i, b);
        groups.set(j, a);
    }


    // ----------------------------------------------------------
// Workout copy helpers
// ----------------------------------------------------------

    /**
     * Deep copy of this Workout, preserving the same ID and timestamps.
     * All groups are deep-copied so the clone is fully independent.
     *
     * <p>Intended for in-memory duplication (e.g., undo/redo buffers,
     * MVVM staging models). For a "Save As New" flow that should receive a
     * new ID and fresh timestamps, use {@link #duplicateWithNewId(Workout)}.</p>
     */
    public Workout deepCopy() {
        return new Workout(this); // uses the deep copy constructor you defined above
    }

    /**
     * Copies mutable fields and deep-copies groups from {@code other} into this instance.
     * <ul>
     *   <li>Keeps this workout’s ID and createdAt as-is.</li>
     *   <li>Copies swimmerId, name, course, notes, default rest.</li>
     *   <li>Replaces groups with deep copies from {@code other}.</li>
     *   <li>Updates this.updatedAt to now.</li>
     * </ul>
     */
    public void copyFrom(Workout other) {
        Objects.requireNonNull(other, "other");

        // Metadata (do NOT change this.id or this.createdAt here)
        this.swimmerId = Objects.requireNonNull(other.swimmerId, "swimmerId");
        this.name = Objects.requireNonNull(other.name, "name");
        this.course = Objects.requireNonNull(other.course, "course");
        this.notes = other.notes;
        this.defaultRestBetweenGroupsSeconds = Math.max(0, other.defaultRestBetweenGroupsSeconds);

        // Replace groups with deep copies
        this.groups.clear();
        for (SetGroup g : other.groups) {
            if (g != null) this.groups.add(g.deepCopy());
        }

        // Touch updated timestamp
        this.updatedAt = java.time.Instant.now();
    }

    /**
     * Factory that duplicates a workout as a brand-new entity
     * (new ID, fresh timestamps), deep-copying groups.
     *
     * <p>Use this for “Duplicate / Save As New”.</p>
     */
    public static Workout duplicateWithNewId(Workout source) {
        Objects.requireNonNull(source, "source");

        Workout copy = new Workout(
                java.util.UUID.randomUUID(),
                source.getSwimmerId(),
                source.getName(),
                source.getCourse(),
                source.getNotes(),
                source.getDefaultRestBetweenGroupsSeconds(),
                java.time.Instant.now(),   // createdAt
                java.time.Instant.now()    // updatedAt
        );

        for (SetGroup g : source.getGroups()) {
            if (g != null) copy.getGroups().add(g.deepCopy());
        }

        return copy;
    }

    // ----------------------------------------------------------
    // Distance helpers
    // ----------------------------------------------------------

    /**
     * Returns the total distance for one full pass through all groups.
     * <p>Uses the canonical meter-based representation via {@link Distance}.</p>
     */
    public Distance singlePassDistance() {
        return Distance.ofMeters(singlePassDistanceMeters());
    }

    /**
     * Returns the total distance of the entire workout, including
     * any group repetitions (e.g., “Main ×4” counts 4× its base distance).
     */
    public Distance totalDistance() {
        return Distance.ofMeters(totalDistanceMeters());
    }

    /** Sum of group distances (in meters) for one pass. */
    @Deprecated
    public int singlePassDistanceMeters() {
        int sum = 0;
        for (SetGroup g : groups) sum += g.singlePassDistanceMeters();
        return sum;
    }

    /** Total workout distance (in meters), including group repetitions. */
    @Deprecated
    public int totalDistanceMeters() {
        int sum = 0;
        for (SetGroup g : groups) sum += g.totalDistanceMeters();
        return sum;
    }

    // ----------------------------------------------------------
    // Object overrides
    // ----------------------------------------------------------

    @Override
    public String toString() {
        return "Workout{" +
                "id=" + id +
                ", swimmerId=" + swimmerId +
                ", name='" + name + '\'' +
                ", course=" + course +
                ", groups=" + groups.size() +
                ", defaultRestBetweenGroupsSeconds=" + defaultRestBetweenGroupsSeconds +
                (notes != null && !notes.isBlank() ? ", notes='" + notes + '\'' : "") +
                '}';
    }
}
