package swimworkoutbuilder_javafx.model.units;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents an immutable swimming distance with canonical storage in meters
 * using fixed-point precision of 0.0001 meters (0.1 mm).
 *
 * <p>The {@code Distance} class uses a canonical internal representation:
 * all distances are stored as integer counts of 1/10,000 of a meter,
 * regardless of how they were originally entered or displayed.
 * This design simplifies the model, ensures unit consistency, and prevents
 * rounding drift when converting between yards and meters.</p>
 *
 * <p>Conversion follows the exact factor:
 * <b>1 yard = 0.9144 meters</b>.
 * By scaling values to 10,000 micro-meters per meter, arithmetic remains exact:
 * <pre>{@code
 * 25 yd × 9,144 microUnits = 228,600 microUnits = 22.86 m
 * }</pre>
 * This avoids floating-point precision loss and ensures that repeated
 * conversions (yards ↔ meters) always produce stable, reversible results.</p>
 *
 * <p>All internal computations are performed in meters to maintain
 * consistency across unit systems and guarantee deterministic results.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Encapsulates a scalar distance value stored in meters.</li>
 *   <li>Provides factory methods for constructing instances from yards or meters
 *       (e.g., {@link #ofYards(double)} and {@link #ofMeters(double)}).</li>
 *   <li>Supports conversion methods between meters and yards, rounding to
 *       common swimming course increments where appropriate.</li>
 *   <li>Ensures immutability and value-based equality for safe use as a model
 *       component or map key.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *   <li>Distances are canonicalized to meters internally. Conversions are performed only when needed.</li>
 *   <li>The conversion ratio is based on 1 yard = 0.9144 meters, consistent with
 *       official FINA and USA Swimming standards.</li>
 *   <li>Instances are immutable and thread-safe.</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Distance d1 = Distance.ofYards(100);   // create from yards
 * Distance d2 = Distance.ofMeters(50);   // create from meters
 *
 * System.out.println(d1.toMeters());     // 91.44
 * System.out.println(d2.toYards());      // 54.68
 * }</pre>
 *
 * <h2>Typical Integration</h2>
 * <p>
 * {@code Distance} is used by {@link swimworkoutbuilder_javafx.model.SwimSet},
 * {@link swimworkoutbuilder_javafx.model.pacing.SeedPace}, and
 * {@link swimworkoutbuilder_javafx.model.pacing.DefaultPacePolicy}
 * for computing goal times, intervals, and rest durations.
 * </p>
 *
 * @author Parker Blackwell
 * @version MVP 1.0 (October 2025)
 * @see swimworkoutbuilder_javafx.model.units.TimeSpan
 * @see swimworkoutbuilder_javafx.model.enums.Course
 */
public final class Distance implements Comparable<Distance>, java.io.Serializable {
    private static final long serialVersionUID = 1L;

    // Defines how the distance is expressed (YARDS or METERS). Independent of pool/course.
    public enum Unit { METERS, YARDS }

    private static final long MICROUNITS_PER_METER = 10_000L;
    private static final long MICROUNITS_PER_YARD = 9_144L; // exact: 0.9144 m * 10_000

    // canonical integer representation = mmeters x 10,0000 (prevent rounding errors)
    private final long microUnits;

    // User's preferred display unit (used only for UI or serialization).
    private final Unit display;   // how the user entered / prefers to see it

    // Private; called by ofMeters(), ofYards(), or ofCanonicalMicroUnits().
    // Example:  Distance.ofYards(25) -> microUnits=228,600, display=YARDS
    private Distance(long microUnits, Unit display) {
        this.microUnits = microUnits;
        this.display = Objects.requireNonNull(display, "display");
    }

    /** Factory: exact when meters has ≤4 decimals; otherwise rounds to nearest 0.1 mm. */
    public static Distance ofMeters(double meters) {
        long v = Math.round(meters * MICROUNITS_PER_METER);
        return new Distance(v, Unit.METERS);
        // If you need guaranteed no-double path, add a BigDecimal overload.
    }

    /** Factory: exact for any fractional/whole yards (because 1 yd = 9144 um4 exactly). */
    public static Distance ofYards(double yards) {
        long v = Math.round(yards * MICROUNITS_PER_YARD);
        return new Distance(v, Unit.YARDS);
    }

    /** Exact factory (canonical). */
    public static Distance ofCanonicalMicroUnits(long microUnits, Unit display) {
        return new Distance(microUnits, display);
    }

    /** Canonical raw value (0.0001 m units). */
    public long rawMicroUnits() { return microUnits; }

    /** Preferred display unit. */
    public Unit displayUnit() { return display; }

    // --------- Conversions (use doubles for UI only; core math should use um4) ---------

    public double toMeters() { return (double) microUnits / MICROUNITS_PER_METER; }

    public double toYards()  { return (double) microUnits / MICROUNITS_PER_YARD;  }

    /** Convert to a new Distance with the requested display unit (value unchanged). */
    public Distance withDisplay(Unit unit) {
        if (unit == this.display) return this;
        return new Distance(this.microUnits, unit);
    }

    // --------- Arithmetic (exact in canonical space) ---------

    public Distance plus(Distance other) {
        return new Distance(Math.addExact(this.microUnits, other.microUnits), this.display);
    }

    public Distance minus(Distance other) {
        return new Distance(Math.subtractExact(this.microUnits, other.microUnits), this.display);
    }

    public Distance times(int k) {
        return new Distance(Math.multiplyExact(this.microUnits, k), this.display);
    }

    public Distance times(double factor) {
        // Only use for policy multipliers; rounds to nearest 0.1 mm at the edge.
        long v = Math.round(this.microUnits * factor);
        return new Distance(v, this.display);
    }

    // --------- Comparisons / Equality ---------

    // Enables sorting of distances (e.g., Collections.sort(listOfDistances)), do range checks, min/max
    // Compares canonical microUnits so ordering is exact and unit agnostic.
    @Override public int compareTo(Distance o) { return Long.compare(this.microUnits, o.microUnits); }


    /** Defines value equality for this immutable value object. */
    /** Two distances are equal if their canonical microUnits match. Ignores display unit -> 50m and 54.68 yd are equal.*/
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Distance)) return false;
        Distance d = (Distance) o;
        return microUnits == d.microUnits; // display unit is UI preference; equality is canonical
    }

    // Required due to overriding equals()
    @Override public int hashCode() { return Long.hashCode(microUnits); }

    // --------- Formatting helpers (UI/IO can use these) ---------

    /** Meters as BigDecimal with 4 decimal places (exact). */
    public BigDecimal metersAsBigDecimal() {
        return BigDecimal.valueOf(microUnits, 4); // scale = 4 decimal places
    }

    /** Yards as BigDecimal (may be repeating; this is for display/rounding at edges). */
    public BigDecimal yardsAsBigDecimal(int scale) {
        return BigDecimal.valueOf(microUnits)
                .divide(BigDecimal.valueOf(MICROUNITS_PER_YARD), scale, java.math.RoundingMode.HALF_UP);
    }

    /** Short human-readable form in the preferred display unit, e.g. "200 yd" or "150 m". */
    public String toShortString() {
        double value = (display == Unit.YARDS) ? toYards() : toMeters();
        // Round to 1 decimal, but strip trailing .0
        double oneDecimal = Math.rint(value * 10.0) / 10.0;
        boolean isIntLike = Math.abs(oneDecimal - Math.rint(oneDecimal)) < 1e-9;
        String num = isIntLike ? String.format("%.0f", oneDecimal) : String.format("%.1f", oneDecimal);
        return num + (display == Unit.YARDS ? " yd" : " m");
    }

    @Override public String toString() {
        return display == Unit.METERS
                ? metersAsBigDecimal() + " m"
                : yardsAsBigDecimal(2) + " yd";
    }
}
