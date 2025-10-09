package swimworkoutbuilder_javafx.model.utils;

import swimworkoutbuilder_javafx.model.SetGroup;
import swimworkoutbuilder_javafx.model.SwimSet;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.model.enums.Course;
import swimworkoutbuilder_javafx.model.pacing.PacePolicy;
import swimworkoutbuilder_javafx.model.units.Distance;

public final class WorkoutPrinter {
    private WorkoutPrinter() {}

    public static void printWorkout(Workout w, Swimmer swimmer, PacePolicy policy) {
        boolean displayYards = (w.getCourse() == Course.SCY);
        String unitLabel = displayYards ? "yd" : "m";

        System.out.println("==================================================");
        String swimmerName = (swimmer.getFirstName() + " " + swimmer.getLastName()).trim();
        String shortId = swimmer.getId().toString().substring(0, 8);
        System.out.printf("Swimmer: %s  (id=%s)%n", swimmerName, shortId);
        System.out.printf("Workout: %s  [%s]%n", w.getName(), w.getCourse());
        if (w.getNotes() != null && !w.getNotes().isBlank()) {
            System.out.println("Notes:   " + w.getNotes());
        }
        System.out.println();

        long workoutSwimSeconds = 0;
        long workoutIntraRestSeconds = 0;
        long workoutBetweenGroupRestSeconds = 0;
        long workoutDisplayDistance = 0; // yards for SCY, meters otherwise

        System.out.println("Groups (" + w.getGroups().size() + "):");
        int groupIndex = 0;

        for (SetGroup g : w.getGroups()) {
            groupIndex++;
            int groupReps = Math.max(1, g.getReps());

            System.out.printf("  %d) %s", g.getOrder(), g.getName());
            if (groupReps > 1) System.out.print("  x" + groupReps);
            System.out.println();
            if (g.getNotes() != null && !g.getNotes().isBlank()) {
                System.out.println("     - " + g.getNotes());
            }

            long singlePassGroupSwimSec = 0;
            long singlePassGroupIntraRestSec = 0;
            long singlePassGroupDisplayDist = 0; // yards or meters per display rules

            int idx = 1;
            for (SwimSet s : g.getSets()) {
                int reps = s.getReps();
                Distance rep = s.getDistancePerRep();

                // Per-rep display distance (snap to lap size for SCY display)
                int repDisplayDist = displayYards
                        ? snapToLapYards((int) Math.round(rep.toYards()))
                        : (int) Math.round(rep.toMeters());

                String strokeShort = (s.getStroke() == null) ? "" : s.getStroke().getShortLabel();

                System.out.printf("     %d. %dx%d%s %-14s %-12s%n",
                        idx++,
                        reps,
                        repDisplayDist, unitLabel,
                        strokeShort,
                        (s.getEffort() != null ? s.getEffort().getLabel() : "")
                );

                for (int r = 1; r <= reps; r++) {
                    double goal  = policy.goalSeconds(w, s, swimmer, r);
                    int interval = policy.intervalSeconds(w, s, swimmer, r);
                    int rest     = policy.restSeconds(w, s, swimmer, r);

                    singlePassGroupSwimSec      += Math.round(goal);
                    singlePassGroupIntraRestSec += rest;

                    System.out.printf("         #%d  goal %s | on %s | rest %s%n",
                            r, mmss(goal), mmss(interval), mmss(rest));
                }

                if (s.getNotes() != null && !s.getNotes().isBlank()) {
                    System.out.println("         note: " + s.getNotes());
                }

                // Add display distance for this set to the group display total
                singlePassGroupDisplayDist += (long) reps * repDisplayDist;
            }

            long groupDisplayTotal = singlePassGroupDisplayDist * groupReps;
            long groupSwimSecondsTotal = singlePassGroupSwimSec * groupReps;
            long groupIntraRestTotal   = singlePassGroupIntraRestSec * groupReps;

            workoutDisplayDistance      += groupDisplayTotal;
            workoutSwimSeconds          += groupSwimSecondsTotal;
            workoutIntraRestSeconds     += groupIntraRestTotal;

            System.out.printf("     Group totals: distance=%d%s  swim=%s  rest=%s  total=%s%n",
                    groupDisplayTotal, unitLabel,
                    mmss(groupSwimSecondsTotal),
                    mmss(groupIntraRestTotal),
                    mmss(groupSwimSecondsTotal + groupIntraRestTotal));

            // Between-group rest (use group's override if present, else workout default)
            int restAfter = (g.getRestAfterGroupSec() > 0)
                    ? g.getRestAfterGroupSec()
                    : w.getDefaultRestBetweenGroupsSeconds();

            if (groupIndex < w.getGroups().size() && restAfter > 0) {
                workoutBetweenGroupRestSeconds += restAfter;
                System.out.println("     (+" + mmss(restAfter) + " rest after group)");
            }

            System.out.println();
        }

        long workoutTotalSeconds = workoutSwimSeconds + workoutIntraRestSeconds + workoutBetweenGroupRestSeconds;

        System.out.println("Totals:");
        System.out.println("  swim time:          " + mmss(workoutSwimSeconds));
        System.out.println("  intra-set rest:     " + mmss(workoutIntraRestSeconds));
        System.out.println("  between-group rest: " + mmss(workoutBetweenGroupRestSeconds));
        System.out.println("  ------------------------------------");
        System.out.println("  workout total:      " + mmss(workoutTotalSeconds));
        System.out.println("  total distance:     " + workoutDisplayDistance + " " + unitLabel);
        System.out.println("==================================================");
        System.out.println();
    }

    // snap yard counts to pool lap increments for SCY (25 yd)
    private static int snapToLapYards(int yards) {
        int lap = 25;
        int laps = Math.round(yards / (float) lap);
        return laps * lap;
    }

    private static String mmss(double seconds) {
        long s = Math.max(0, Math.round(seconds));
        return String.format("%d:%02d", s / 60, s % 60);
    }
}
