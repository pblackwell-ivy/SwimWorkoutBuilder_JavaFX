package swimworkoutbuilder_javafx.model;

// ------------------------------------------------------------
// Imports
// ------------------------------------------------------------

import java.io.ObjectStreamException;
import java.time.Instant;
import java.util.*;
import swimworkoutbuilder_javafx.model.enums.StrokeType;
import swimworkoutbuilder_javafx.model.pacing.SeedPace;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.model.units.TimeSpan;

/**
 * Represents an individual swimmer, including personal information,
 * team affiliation, and baseline seed times used for workout pacing.
 *
 * <p>This class serves as a core domain model within the SwimWorkoutBuilder
 * application. It stores identifying information and per-stroke performance
 * benchmarks ({@link swimworkoutbuilder_javafx.model.pacing.SeedPace}) that
 * other components use to calculate goal times, intervals, and pacing plans.</p>
 *
 * <p><b>Key features:</b>
 * <ul>
 *   <li>Stores personal and team details for a swimmer</li>
 *   <li>Maintains seed times by stroke type ({@link swimworkoutbuilder_javafx.model.enums.StrokeType})</li>
 *   <li>Tracks creation and last-update timestamps for persistence</li>
 *   <li>Supports serialization for saving and loading swimmer data</li>
 *   <li>Provides convenience methods for managing and validating seed times</li>
 * </ul>
 * <pb><b>Chanbge history</b>
 * <ul>
 *     <li><b>1.0</b> - Initial version (core swimmer fields and seed tracking)</li>
 *     <li><b>1.1</b> - Added <code>createdAt</code> and <code>updatedAt</code> timestamps for persistence and
 *     auditing support</code></li>
 * </ul>
 *
 * @author Parker Blackwell
 * @version 1.1
 * @since 2025-10-03
 */
public class Swimmer implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    // --------------------------------------------------------
    // Fields
    // --------------------------------------------------------
    private final UUID id;
    private String firstName;
    private String lastName;
    private String preferredName;   // optional / nullable
    private String teamName;        // optional / nullable

    // Timestamps
    private final Instant createdAt;   // when this swimmer record was created
    private Instant updatedAt;         // last time this swimmer record changed

    // Seeds by stroke
    private final Map<StrokeType, SeedPace> seedPaces = new EnumMap<>(StrokeType.class);

    // --------------------------------------------------------
    // Constructors (Public API unchanged)
    // --------------------------------------------------------

    /** Create a new swimmer (timestamps = now). */
    public Swimmer(String firstName, String lastName) {
        this(UUID.randomUUID(), firstName, lastName, null, null, Instant.now(), Instant.now());
    }

    /** Create a new swimmer with optional preferred/team (timestamps = now). */
    public Swimmer(String firstName, String lastName, String preferredName, String teamName) {
        if (firstName == null) throw new IllegalArgumentException("firstName must not be null");
        this.id = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName  = lastName;
        this.preferredName = preferredName;
        this.teamName = teamName;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    /** Repository/loader constructor with explicit timestamps. */
    public Swimmer(UUID id,
                   String firstName,
                   String lastName,
                   String preferredName,
                   String teamName,
                   Instant createdAt,
                   Instant updatedAt) {
        if (id == null) throw new IllegalArgumentException("id must not be null");
        this.id = id;
        this.firstName = Objects.requireNonNull(firstName, "firstName");
        this.lastName  = Objects.requireNonNull(lastName,  "lastName");
        this.preferredName = preferredName;
        this.teamName = teamName;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }
    // Convenience repo/loader ctor without timestamps (defaults to now)
    public Swimmer(java.util.UUID id,
                   String firstName,
                   String lastName,
                   String preferredName,
                   String teamName) {
        this(id, firstName, lastName, preferredName, teamName,
                java.time.Instant.now(), java.time.Instant.now());
    }
    /** Deep copy constructor (same logical swimmer id). */
    public Swimmer(Swimmer other) {
        this.id = other.id;
        this.firstName = other.firstName;
        this.lastName  = other.lastName;
        this.preferredName = other.preferredName;
        this.teamName = other.teamName;
        this.createdAt = other.createdAt;
        this.updatedAt = other.updatedAt;
        this.seedPaces.putAll(other.seedPaces); // map contents are immutable value objects
    }

    // --------------------------------------------------------
    // Identity & Profile (getters/setters)
    // --------------------------------------------------------

    public UUID getId() { return id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) {
        this.firstName = Objects.requireNonNull(firstName, "firstName");
        touchUpdated();
    }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) {
        this.lastName = Objects.requireNonNull(lastName, "lastName");
        touchUpdated();
    }

    public String getPreferredName() { return preferredName; }
    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
        touchUpdated();
    }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) {
        this.teamName = teamName;
        touchUpdated();
    }

    // --------------------------------------------------------
    // Timestamps
    // --------------------------------------------------------

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }
    public void touchUpdated() { this.updatedAt = Instant.now(); }

    // Safety for old serialized data that didn’t have timestamps
    private Object readResolve() throws ObjectStreamException {
        // createdAt/updatedAt may be null on very old saves; initialize if needed
        Instant now = Instant.now();
        try {
            // Use reflection-less fallbacks to keep this class simple:
            if (this.updatedAt == null) this.updatedAt = now;
            // createdAt is final — if it deserialized as null from an old file, we can’t assign here.
            // Practically, because it’s final, older serialized forms will not have this field and Java
            // will set it to default (null). If that happens in your environment, consider a custom
            // readObject or a migration in LocalStore to rebuild Swimmer via the repo constructor.
        } catch (Throwable ignored) {}
        return this;
    }

    // --------------------------------------------------------
    // Seeds (per stroke)
    // --------------------------------------------------------

    /** Read-only view of all seeds (used by UI to show/edit the table). */
    public Map<StrokeType, SeedPace> getSeedPaces() {
        return Collections.unmodifiableMap(seedPaces);
    }

    /** Replace all seeds at once (used by Seed dialog Save). */
    public void setAllSeedPaces(Map<StrokeType, SeedPace> src) {
        seedPaces.clear();
        if (src != null) seedPaces.putAll(src);
        touchUpdated();
    }

    /** Convenience flag: do we have at least one seed? */
    public boolean hasAnySeeds() {
        return !seedPaces.isEmpty();
    }

    /** Alias used by some UI code; equivalent to updateSeedTime(). */
    public void setSeedPace(StrokeType stroke, SeedPace pace) {
        updateSeedTime(stroke, pace);
    }

    /** Returns the baseline seed for the given stroke, or null if not set. */
    public SeedPace getSeedTime(StrokeType stroke) {
        Objects.requireNonNull(stroke, "stroke");
        return seedPaces.get(stroke);
    }

    /** Creates or replaces the baseline seed for the given stroke. */
    public void updateSeedTime(StrokeType stroke, SeedPace seed) {
        Objects.requireNonNull(stroke, "stroke");
        Objects.requireNonNull(seed,   "seed");
        seedPaces.put(stroke, seed);
        touchUpdated();
    }

    /** Convenience: set a seed defined as "per 100 yards" in seconds. */
    public void updateSeed100Y(StrokeType stroke, double secondsPer100Y) {
        Objects.requireNonNull(stroke, "stroke");
        if (secondsPer100Y <= 0) throw new IllegalArgumentException("secondsPer100Y must be > 0");
        updateSeedTime(stroke, new SeedPace(Distance.ofYards(100), TimeSpan.ofSeconds(secondsPer100Y)));
    }

    /** Convenience: set a seed defined as "per 100 meters" in seconds. */
    public void updateSeed100M(StrokeType stroke, double secondsPer100M) {
        Objects.requireNonNull(stroke, "stroke");
        if (secondsPer100M <= 0) throw new IllegalArgumentException("secondsPer100M must be > 0");
        updateSeedTime(stroke, new SeedPace(Distance.ofMeters(100), TimeSpan.ofSeconds(secondsPer100M)));
    }

    /** Convenience: explicit distance + time overload (e.g., 200m in 120.0s). */
    public void updateSeedTime(StrokeType stroke, Distance seedDistance, TimeSpan seedTime) {
        Objects.requireNonNull(stroke, "stroke");
        Objects.requireNonNull(seedDistance, "seedDistance");
        Objects.requireNonNull(seedTime,     "seedTime");
        updateSeedTime(stroke, new SeedPace(seedDistance, seedTime));
    }

    public boolean hasSeed(StrokeType stroke) {
        Objects.requireNonNull(stroke, "stroke");
        return seedPaces.containsKey(stroke);
    }

    public void clearSeed(StrokeType stroke) {
        Objects.requireNonNull(stroke, "stroke");
        seedPaces.remove(stroke);
        touchUpdated();
    }

    /** Removes all seeds for this swimmer. */
    public void clearAllSeeds() {
        seedPaces.clear();
        touchUpdated();
    }

    // --------------------------------------------------------
    // Derived helpers
    // --------------------------------------------------------

    /** Returns canonical speed (m/s) for a stroke, or NaN if no seed set. */
    public double speedMps(StrokeType stroke) {
        Objects.requireNonNull(stroke, "stroke");
        SeedPace s = seedPaces.get(stroke);
        return (s == null) ? Double.NaN : s.speedMps();
    }

    /** Returns true if all four primary strokes have seeds set. */
    public boolean hasCoreSeeds() {
        return hasSeed(StrokeType.FREESTYLE)
                && hasSeed(StrokeType.BACKSTROKE)
                && hasSeed(StrokeType.BREASTSTROKE)
                && hasSeed(StrokeType.BUTTERFLY);
    }

    // --------------------------------------------------------
    // Object overrides
    // --------------------------------------------------------

    @Override
    public String toString() {
        return "Swimmer(" +
                "id=" + id + ", " +
                "name=" + firstName + " " + lastName + ", " +
                "preferredName=" + (preferredName == null ? "" : preferredName) + ", " +
                "teamName=" + (teamName == null ? "" : teamName) + ", " +
                "createdAt=" + createdAt + ", " +
                "updatedAt=" + updatedAt + ", " +
                "seedPaces=" + seedPaces +
                ')';
    }
}
