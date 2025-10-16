package swimworkoutbuilder_javafx.model.pacing;


import java.util.Objects;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.model.units.TimeSpan;

/**
 * Represents a swimmer’s **baseline pace** (seed time) for a specific stroke and distance.
 *
 * <p>A {@code SeedPace} defines how fast a swimmer can complete a known distance,
 * serving as the foundation for all pace, goal, and interval calculations in the app.
 * It captures both the measured distance and the time required, and derives the
 * canonical swim speed in meters per second (m/s) for cross-course computations.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Store the original test distance as a {@link swimworkoutbuilder_javafx.model.units.Distance}.</li>
 *   <li>Store the corresponding completion time as a {@link swimworkoutbuilder_javafx.model.units.TimeSpan}.</li>
 *   <li>Derive and cache the swimmer’s canonical speed in meters per second.</li>
 *   <li>Provide immutability, ensuring the seed pace cannot change once created.</li>
 *   <li>Expose the timestamp of when the seed pace was last recorded or updated.</li>
 * </ul>
 *
 * <h2>Canonical Design</h2>
 * <p>All internal calculations use canonical (meter-based) units, ensuring consistency
 * between yards and meters. For example, a 100-yard and 100-meter pace can both be
 * compared and converted without floating-point drift.</p>
 *
 * <h2>Typical Usage</h2>
 * <pre>{@code
 * // Example: 100-yard freestyle in 78.0 seconds
 * SeedPace seed = new SeedPace(Distance.ofYards(100), TimeSpan.ofSeconds(78.0));
 * double speed = seed.speedMps(); // → 1.172 m/s
 * }</pre>
 *
 * <h2>Integration</h2>
 * <ul>
 *   <li>Used by {@link swimworkoutbuilder_javafx.model.Swimmer} to maintain per-stroke seed paces.</li>
 *   <li>Queried by {@link swimworkoutbuilder_javafx.model.pacing.DefaultPacePolicy} for goal and interval computation.</li>
 *   <li>Supports extensions such as per-course adjustments or historical tracking.</li>
 * </ul>
 *
 * @author Parker Blackwell
 * @version MVP 1.0 (October 2025)
 * @see swimworkoutbuilder_javafx.model.units.Distance
 * @see swimworkoutbuilder_javafx.model.units.TimeSpan
 * @see swimworkoutbuilder_javafx.model.Swimmer
 */
public final class SeedPace implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final Distance originalDistance;  // e.g., 100y or 100m
    private final TimeSpan time;              // time for that distance
    private final double speedMps;            // meters/second (0 if time==0)

    /**
     * Creates a new {@code SeedPace} from a measured distance and completion time.
     *
     * <p>This constructor immediately derives and caches the swimmer’s canonical
     * speed in meters per second (m/s) based on the given distance and time.
     * Both arguments must be non-null and the time must be greater than zero.</p>
     *
     * @param originalDistance the distance swum (e.g., 100 yards or 100 meters)
     * @param time the total time taken to complete that distance
     * @throws NullPointerException if either parameter is {@code null}
     */
    public SeedPace(Distance originalDistance, TimeSpan time) {
        this.originalDistance = Objects.requireNonNull(originalDistance, "originalDistance");
        this.time             = Objects.requireNonNull(time, "time");
        long ms = this.time.toMillis();
        this.speedMps = (ms <= 0L) ? 0.0 : (this.originalDistance.toMeters() / (ms / 1000.0));
    }

    /**
     * Returns the original baseline distance (e.g., 100y or 100m) that this seed was measured over.
     * This is used to normalize workout rep distances for pace calculations.
     */
    public Distance getOriginalDistance() { return originalDistance; }

    /**
     * Returns the total recorded time for the original seed distance.
     * Stored as an immutable {@link TimeSpan} for precise millisecond access.
     */
    public TimeSpan getTime() { return time; }

    /**
     * Returns the swimmer’s baseline speed in meters per second, derived from the seed distance/time.
     * A value of 0.0 indicates an invalid or zero-length time.
     */
    public double speedMps() { return speedMps; }

    @Override
    public String toString() {
        return "SeedPace{" +
                "originalDistance=" + originalDistance +
                ", time=" + time +
                ", speedMps=" + String.format("%.3f", speedMps) +
                '}';
    }
}
