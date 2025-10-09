package swimworkoutbuilder_javafx.model.pacing;

import swimworkoutbuilder_javafx.model.SwimSet;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.model.enums.DistanceFactors;
import swimworkoutbuilder_javafx.model.enums.Effort;
import swimworkoutbuilder_javafx.model.enums.Equipment;
import swimworkoutbuilder_javafx.model.enums.StrokeType;

import java.util.Objects;
import java.util.Set;

/**
 * Multiplier-based MVP policy for computing goal, interval, and rest.
 */
public class DefaultPacePolicy implements PacePolicy {

    private static final boolean DEBUG = false;

    @Override
    public double goalSeconds(Workout workout, SwimSet set, Swimmer swimmer, int repIndex) {
        // Validate required references early to avoid NPEs in callers.
        Objects.requireNonNull(workout, "workout");
        Objects.requireNonNull(set, "set");
        Objects.requireNonNull(swimmer, "swimmer");

        // 1) Seed (canonical speed)
        StrokeType stroke = set.getStroke();
        SeedPace seed = swimmer.getSeedTime(stroke);
        if (seed == null) throw new IllegalStateException("Missing seed for stroke: " + stroke);
        double speedMps = seed.speedMps(); // canonical
        if (speedMps <= 0.0) throw new IllegalStateException("Seed speed must be > 0 m/s for " + stroke);

        // 2) Multipliers
        Effort effort = set.getEffort();
        double mEffort  = (effort == null) ? 1.0 : effort.paceMultiplier();
        double mDist    = usesDistanceFactor(effort) ? DistanceFactors.forDistance(set.getDistancePerRep()) : 1.0;
        double mCourse  = workout.getCourse().multiplier();
        double mEquip   = equipmentProduct(set.getEquipment());
        double mFatigue = 1.0; // hook for future

        // 3) Distance in meters (canonical), compute goal
        double repMeters = set.getDistancePerRep().toMeters();
        double goal = (repMeters / speedMps) * mEffort * mDist * mCourse * mEquip * mFatigue;

        if (DEBUG) {
            System.out.printf(
                    "[DEBUG] %s rep #%d goal: (%.2fm / %.4f m/s) × %.2f(effort) × %.2f(dist) × %.2f(course) × %.2f(equip) × %.2f(fatigue) = %.2fs%n",
                    stroke, repIndex + 1,
                    repMeters, speedMps, mEffort, mDist, mCourse, mEquip, mFatigue, goal
            );
        }
        return goal;
    }

    @Override
    public int restSeconds(Workout workout, SwimSet set, Swimmer swimmer, int repIndex) {
        Objects.requireNonNull(workout, "workout");
        Objects.requireNonNull(set, "set");
        Objects.requireNonNull(swimmer, "swimmer");

        int goalRounded = (int) Math.round(goalSeconds(workout, set, swimmer, repIndex));

        SeedPace seed = swimmer.getSeedTime(set.getStroke());
        if (seed == null) throw new IllegalStateException("Missing seed for " + set.getStroke());

        double repMeters  = set.getDistancePerRep().toMeters();
        double seedMeters = seed.getOriginalDistance().toMeters();

        double r = distanceRatio(repMeters, seedMeters);
        double pct = restPercent(set.getEffort(), r);

        int rest = (int) Math.round(goalRounded * pct);

        if (DEBUG) {
            System.out.printf("[DEBUG-REST] r=%.2f (rep=%.2fm / seed=%.2fm), effort=%s, rest%%=%.1f%% -> rest=%ds%n",
                    r, repMeters, seedMeters, set.getEffort(), pct * 100.0, rest);
        }
        return Math.max(0, rest);
    }

    @Override
    public int intervalSeconds(Workout workout, SwimSet set, Swimmer swimmer, int repIndex) {
        int goalRounded = (int) Math.round(goalSeconds(workout, set, swimmer, repIndex));
        int rest = restSeconds(workout, set, swimmer, repIndex);
        int interval = goalRounded + rest;
        int rounded = roundToNearest5(interval);
        if (DEBUG && rounded != interval) {
            System.out.printf("[DEBUG-INT] interval %ds -> rounded to %ds%n", interval, rounded);
        }
        return rounded;
    }

    @Override
    public String timingLabel(Workout workout, SwimSet set, Swimmer swimmer, int repIndex) {
        int rest = restSeconds(workout, set, swimmer, repIndex);
        return "rest: " + rest;
    }

    // --- helpers ---

    private static boolean usesDistanceFactor(Effort e) {
        if (e == null) return false;
        switch (e) {
            case THRESHOLD:
            case RACE_PACE:
            case VO2_MAX:
            case SPRINT:
                return true;
            case EASY:
            case ENDURANCE:
            default:
                return false;
        }
    }

    private static double equipmentProduct(Set<Equipment> equipment) {
        if (equipment == null || equipment.isEmpty()) return 1.0;
        double m = 1.0;
        for (Equipment eq : equipment) {
            if (eq != null) m *= eq.multiplier();
        }
        return m;
    }

    /** Distance ratio r = rep / seedBase, clamped to avoid degenerate values. */
    private static double distanceRatio(double repMeters, double seedMeters) {
        double base = (seedMeters <= 0.0) ? 100.0 : seedMeters; // fallback: per-100m baseline
        double r = repMeters / base;
        return Math.max(0.1, r);
    }

    private static double lerp(double a, double b, double t) {
        if (t <= 0) return a;
        if (t >= 1) return b;
        return a + (b - a) * t;
    }

    /** Round seconds to the nearest 5-second boundary (ties round up). */
    private static int roundToNearest5(int secs) {
        int rem = secs % 5;
        if (rem < 0) rem += 5;
        return (rem < 3) ? (secs - rem) : (secs + (5 - rem));
    }

    /**
     * Rest percentage as a function of effort and distance ratio r.
     */
    private static double restPercent(Effort e, double r) {
        if (e == null) return 0.06;

        switch (e) {
            case EASY: {
                if (r <= 1.0) return 0.10;
                if (r <= 4.0) return lerp(0.10, 0.18, (r - 1.0) / 3.0);
                return lerp(0.18, 0.05, Math.min((r - 4.0) / 11.0, 1.0));
            }
            case ENDURANCE: {
                if (r <= 1.0) return 0.04;
                if (r <= 4.0) return lerp(0.04, 0.05, (r - 1.0) / 3.0);
                return lerp(0.05, 0.045, Math.min((r - 4.0) / 11.0, 1.0));
            }
            case THRESHOLD: {
                if (r <= 1.0) return 0.067;
                if (r <= 4.0) return lerp(0.067, 0.055, (r - 1.0) / 3.0);
                return lerp(0.055, 0.040, Math.min((r - 4.0) / 11.0, 1.0));
            }
            case RACE_PACE:
            case VO2_MAX:
            case SPRINT: {
                if (r <= 1.0) return 0.70;
                if (r <= 4.0) return lerp(0.70, 0.25, (r - 1.0) / 3.0);
                return lerp(0.25, 0.05, Math.min((r - 4.0) / 11.0, 1.0));
            }
            default:
                return 0.06;
        }
    }
}
