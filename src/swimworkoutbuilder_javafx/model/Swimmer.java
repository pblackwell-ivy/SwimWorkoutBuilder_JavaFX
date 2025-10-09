package swimworkoutbuilder_javafx.model;

import swimworkoutbuilder_javafx.model.enums.StrokeType;
import swimworkoutbuilder_javafx.model.pacing.SeedPace;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.model.units.TimeSpan;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Collections;

/**
 * Represents an individual swimmer with personal details and performance data.
 */
public class Swimmer implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private String firstName;
    private String lastName;
    private String preferredName;   // optional / nullable
    private String teamName;        // optional / nullable
    private final Map<StrokeType, SeedPace> seedPaces = new EnumMap<>(StrokeType.class);


    // --- Constructors ---

    public Swimmer(String firstName, String lastName) {
        this(UUID.randomUUID(), firstName, lastName, null, null);
    }

    public Swimmer(String firstName, String lastName, String preferredName, String teamName) {
        this(UUID.randomUUID(), firstName, lastName, preferredName, teamName);
    }

    // UUID-aware constructor (e.g., repository load)
    public Swimmer(UUID id, String firstName, String lastName, String preferredName, String teamName) {
        if (id == null) throw new IllegalArgumentException("id must not be null");
        this.id = id;
        this.firstName = Objects.requireNonNull(firstName, "firstName");
        this.lastName  = Objects.requireNonNull(lastName, "lastName");
        this.preferredName = preferredName;
        this.teamName = teamName;
    }

    // --- Identity ---

    public UUID getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPreferredName() { return preferredName; }
    public String getTeamName() { return teamName; }

    public void setFirstName(String firstName) { this.firstName = Objects.requireNonNull(firstName, "firstName"); }
    public void setLastName(String lastName) { this.lastName = Objects.requireNonNull(lastName, "lastName"); }
    public void setPreferredName(String preferredName) { this.preferredName = preferredName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    // --- Seeds (per stroke) ---

    // === Add these inside class Swimmer (alongside your existing seed methods) ===

    /** Read-only view of all seeds (used by UI to show/edit the table). */
    public Map<StrokeType, SeedPace> getSeedPaces() {
        return Collections.unmodifiableMap(seedPaces);
    }

    /** Replace all seeds at once (used by Seed dialog Save). */
    public void setAllSeedPaces(Map<StrokeType, SeedPace> src) {
        seedPaces.clear();
        if (src != null) seedPaces.putAll(src);
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
    }

    /** Removes all seeds for this swimmer. */
    public void clearAllSeeds() { seedPaces.clear(); }

    // --- Derived helpers ---

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

    @Override
    public String toString() {
        return "Swimmer(" +
                "id=" + id + ", " +
                "name=" + firstName + " " + lastName + ", " +
                "preferredName=" + (preferredName == null ? "" : preferredName) + ", " +
                "teamName=" + (teamName == null ? "" : teamName) + ", " +
                "seedPaces=" + seedPaces +
                ')';
    }
}
