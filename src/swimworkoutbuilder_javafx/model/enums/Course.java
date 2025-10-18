package swimworkoutbuilder_javafx.model.enums;

import swimworkoutbuilder_javafx.model.units.Distance;

/**
 * Course enumerates the three most common competition pool lengths in the world and their impact on goal and rest
 * time calculations.  This enum is used to define the swimming course length for a workout and is
 * an essential UI/logic component of the app.  Future capabilities will include user defined custom course lengths
 * to accomodate any swimming course length.
 *
 * SCY = Short Course Yards (25 yards)
 * SCM = Short Course Meters (25 meters)
 * LCM = Long Course Meters (50 meters)
 *
 * v2: added performance multiplier used by pace calculation.
 * SC pools have more turns, which means more push-offs and more underwaters as a percentage
 * of the distance compared to an LC pool.
 *
 * Typical defaults:
 *  - SCY: 1.00 (25y: more turns, more underwater as a percentage of distance)
 *  - SCM: 1.04 (25m: underwater % slightly less than SCY)
 *  - LCM: 1.07 (50m: half as many turns as SCY/SCM, less underwater vs. surface swimming)
 */
public enum Course {
    SCY("Short Course Yards", Distance.ofYards(25), CourseUnit.YARDS, 1.00),
    SCM("Short Course Meters", Distance.ofMeters(25), CourseUnit.METERS, 1.04),
    LCM("Long Course Meters", Distance.ofMeters(50), CourseUnit.METERS, 1.07);

    private final String description;
    private final Distance length;      // pool length as exact Distance
    private final CourseUnit unit;
    private final double multiplier;    // performance multiplier

    Course(String description, Distance length, CourseUnit unit, double multiplier) {
        this.description = description;
        this.length = length;
        this.unit = unit;
        this.multiplier = multiplier;
    }

    public String getDescription() { return description; }

    /** Returns the pool length as a Distance (exact, e.g., 25 yards or 25 meters). */
    public Distance getLength() { return length; }

    public CourseUnit getUnit() { return unit; }

    /** Performance multiplier applied in pace calculations. */
    public double multiplier() { return multiplier; }

    @Override
    public String toString() {
        return description + " (" + length.toString() + ")" + " (multiplier=" + multiplier + ")";
    }
}
