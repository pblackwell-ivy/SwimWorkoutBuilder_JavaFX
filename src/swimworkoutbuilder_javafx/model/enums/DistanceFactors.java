package swimworkoutbuilder_javafx.model.enums;

import swimworkoutbuilder_javafx.model.units.Distance;

/**
 * DistanceFactors provides multipliers to adjust target pace
 * depending on repeat distance. Short reps are faster than seed,
 * longer reps trend slower.
 *
 * Distances are stored as exact Distance values (canonical 0.0001 m units).
 * Multipliers are applied during pace calculations.
 *
 * To do: add a fallback calculation for distances outside the buckets.
 */
public enum DistanceFactors {
    D25 (Distance.ofMeters(25),    0.92),
    D50 (Distance.ofMeters(50),    0.94),
    D75 (Distance.ofMeters(75),    0.97),
    D100(Distance.ofMeters(100),   1.00),
    D200(Distance.ofMeters(200),   1.05),
    D400(Distance.ofMeters(400),   1.10),
    D800(Distance.ofMeters(800),   1.15),
    D1500(Distance.ofMeters(1500), 1.20);

    private final Distance distance;
    private final double multiplier;

    DistanceFactors(Distance distance, double multiplier) {
        this.distance = distance;
        this.multiplier = multiplier;
    }

    /** Returns the canonical distance (exact) this factor is defined for. */
    public Distance distance() { return distance; }

    /** Returns the multiplier applied at this distance. */
    public double multiplier() { return multiplier; }

    /**
     * Get the pace multiplier for an arbitrary distance.
     * Uses distance buckets (rounded up to the nearest defined distance)
     * to approximate performance trends.
     */
    public static double forDistance(Distance d) {
        long meters = Math.round(d.toMeters());
        if (meters <= 25)   return D25.multiplier;
        if (meters <= 50)   return D50.multiplier;
        if (meters <= 75)   return D75.multiplier;
        if (meters <= 100)  return D100.multiplier;
        if (meters <= 200)  return D200.multiplier;
        if (meters <= 400)  return D400.multiplier;
        if (meters <= 800)  return D800.multiplier;
        return D1500.multiplier; // default for anything longer
    }

    @Override
    public String toString() {
        return Math.round(distance.toMeters()) + "m (" + multiplier + ")";
    }
}
