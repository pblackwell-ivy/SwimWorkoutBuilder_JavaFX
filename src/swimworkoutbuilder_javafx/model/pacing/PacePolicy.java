package swimworkoutbuilder_javafx.model.pacing;

import swimworkoutbuilder_javafx.model.SwimSet;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.model.Workout;

/**
 * Strategy interface for turning sets + seeds into concrete timing.
 *
 * Policies define how goal times, rests, and intervals are computed from:
 *  • the workout context (course, modifiers),
 *  • the swimmer's seed pace,
 *  • and the set definition (stroke, reps, distance, effort, equipment).
 */
public interface PacePolicy {

    /** Goal time (seconds) for a single rep (not the send-off). */
    double goalSeconds(Workout workout, SwimSet set, Swimmer swimmer, int repIndex);

    /**
     * Interval/send-off (seconds) for a single rep.
     * MVP rule: interval = round(goal) + rest.
     */
    int intervalSeconds(Workout workout, SwimSet set, Swimmer swimmer, int repIndex);

    /** Rest after the rep (seconds). MVP rule: derived from Effort × distance ratio. */
    int restSeconds(Workout workout, SwimSet set, Swimmer swimmer, int repIndex);

    /**
     * Optional short label for UI/printer (e.g., "rest :20").
     * Implementations may format this however they wish.
     */
    String timingLabel(Workout workout, SwimSet set, Swimmer swimmer, int repIndex);
}
