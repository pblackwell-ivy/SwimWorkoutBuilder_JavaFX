package swimworkoutbuilder_javafx.model.units;

/**
 * Represents an immutable span of time stored in **milliseconds**.
 *
 * <p>{@code TimeSpan} is a lightweight utility class for expressing durations
 * (e.g., 1:23.45) in a canonical, integer-based format. It provides
 * arithmetic helpers, conversion utilities, and consistent string formatting
 * for display in a human-readable form.</p>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *   <li>Internally stores time as a {@code long} number of milliseconds for exact arithmetic.</li>
 *   <li>Immutable — all operations return new {@code TimeSpan} instances.</li>
 *   <li>Implements {@link Comparable} for natural ordering by duration.</li>
 *   <li>Used in conjunction with {@link swimworkoutbuilder.model.units.Distance} and
 *       {@link swimworkoutbuilder.model.pacing.SeedPace} for pacing calculations.</li>
 * </ul>
 *
 * <h2>Typical Usage</h2>
 * <pre>{@code
 * TimeSpan t1 = TimeSpan.ofSeconds(78.3);         // 1:18.30
 * TimeSpan t2 = TimeSpan.ofMinutesSecondsMillis(0, 25, 0); // 0:25.00
 *
 * TimeSpan total = t1.plus(t2);                   // 1:43.30
 * System.out.println(total);                      // → "1:43.30"
 * }</pre>
 *
 * @author Parker Blackwell
 * @version MVP 1.0 (October 2025)
 * @see swimworkoutbuilder.model.units.Distance
 * @see swimworkoutbuilder.model.pacing.SeedPace
 */
public final class TimeSpan implements Comparable<TimeSpan>, java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /** Duration in milliseconds (canonical internal storage). */
    private final long millis;

    /** Private constructor — use factory methods for clarity. */
    private TimeSpan(long millis) { this.millis = millis; }

    // ----------------------------------------------------------
    // Factory methods
    // ----------------------------------------------------------

    /** Creates a {@code TimeSpan} from an exact millisecond value. */
    public static TimeSpan ofMillis(long ms) { return new TimeSpan(ms); }

    /** Creates a {@code TimeSpan} from a floating-point seconds value. */
    public static TimeSpan ofSeconds(double seconds) {
        return new TimeSpan(Math.round(seconds * 1000.0));
    }

    /**
     * Creates a {@code TimeSpan} from minute, second, and millisecond components.
     * @param minutes whole minutes
     * @param seconds whole seconds
     * @param millis  additional milliseconds
     * @return new {@code TimeSpan} representing the combined duration
     */
    public static TimeSpan ofMinutesSecondsMillis(int minutes, int seconds, int millis) {
        long total = Math.addExact(Math.addExact(minutes * 60_000L, seconds * 1_000L), millis);
        return new TimeSpan(total);
    }

    // ----------------------------------------------------------
    // Conversions
    // ----------------------------------------------------------

    /** Returns the total duration in milliseconds. */
    public long toMillis() { return millis; }

    /** Returns the total duration in seconds as a double. */
    public double toSeconds() { return millis / 1000.0; }

    // ----------------------------------------------------------
    // Arithmetic
    // ----------------------------------------------------------

    /** Returns a new {@code TimeSpan} representing this + other. */
    public TimeSpan plus(TimeSpan other) {
        return new TimeSpan(Math.addExact(millis, other.millis));
    }

    /** Returns a new {@code TimeSpan} representing this − other. */
    public TimeSpan minus(TimeSpan other) {
        return new TimeSpan(Math.subtractExact(millis, other.millis));
    }

    /** Returns a scaled version of this time span (e.g., 1.5× longer). */
    public TimeSpan times(double factor) {
        return new TimeSpan(Math.round(millis * factor));
    }

    // ----------------------------------------------------------
    // Comparison & equality
    // ----------------------------------------------------------

    @Override
    public int compareTo(TimeSpan o) { return Long.compare(this.millis, o.millis); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSpan)) return false;
        return millis == ((TimeSpan) o).millis;
    }

    @Override
    public int hashCode() { return Long.hashCode(millis); }

    // ----------------------------------------------------------
    // Formatting
    // ----------------------------------------------------------

    /**
     * Returns a human-readable formatted string, e.g., {@code "1:23.45"}.
     * <p>Displays minutes, seconds, and hundredths of a second.</p>
     */
    @Override
    public String toString() {
        long total = millis;
        long minutes = total / 60_000; total %= 60_000;
        long seconds = total / 1_000;  total %= 1_000;
        long hundredths = Math.round(total / 10.0);

        // Normalize edge cases (e.g., 59.99 rounds up cleanly)
        if (hundredths == 100) { hundredths = 0; seconds++; }
        if (seconds == 60) { seconds = 0; minutes++; }

        return String.format("%d:%02d.%02d", minutes, seconds, hundredths);
    }
}
