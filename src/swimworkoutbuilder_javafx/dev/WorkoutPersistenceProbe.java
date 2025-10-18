package swimworkoutbuilder_javafx.dev;


import java.time.Instant;
import java.util.List;
import java.util.UUID;
import swimworkoutbuilder_javafx.model.*;
import swimworkoutbuilder_javafx.model.enums.*;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.store.LocalStore;
/**
 * [UI Component] WorkoutPersistenceProbe for the "dev" feature.
 *
 * <p><b>Responsibilities:</b>
 * <ul>
 *   <li>Render nodes and bind to observable state</li>
 *   <li>Expose minimal API for host containers</li>
 *   <li>Integrate canonical button roles and theming</li>
 * </ul>
 *
 * <p><b>Design Notes:</b>
 * <ul>
 *   <li>Encapsulate layout and styling concerns</li>
 *   <li>Prefer composition over inheritance</li>
 *   <li>Avoid side effects; pure UI behavior</li>
 * </ul>
 *
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * // Typical usage for WorkoutPersistenceProbe
 * WorkoutPersistenceProbe obj = new WorkoutPersistenceProbe();
 * obj.toString(); // replace with real usage
 * }</pre>
 *
 * @author Parker Blackwell
 * @version 1.0
 * @since 2025-10-14
 */

public final class WorkoutPersistenceProbe {
    public static void main(String[] args) throws Exception {
        // 1) Create & save a swimmer
        UUID swimmerId = UUID.randomUUID();
        Swimmer s = new Swimmer(swimmerId, "Probe", "User", "", "TestTeam",
                Instant.now(), Instant.now());
        LocalStore.saveSwimmer(s);

        // 2) Build a workout with 1 group and 2 sets, then save it
        Workout w = new Workout(swimmerId, "Probe Workout", Course.SCY, "Roundtrip test", 30);
        SetGroup g = new SetGroup("Main");
        g.setReps(2);
        g.addSet(new SwimSet(StrokeType.FREESTYLE, 8, Distance.ofYards(50), Effort.EASY,  Course.SCY, "Smooth"));
        g.addSet(new SwimSet(StrokeType.BACKSTROKE, 4, Distance.ofYards(100), Effort.ENDURANCE, Course.SCY, "Hold form"));
        w.getGroups().clear();
        w.getGroups().add(g);
        LocalStore.saveWorkout(w);

        System.out.println("Saved workout id=" + w.getId());

        // 3) List and load back
        List<Workout> list = LocalStore.listWorkoutsFor(swimmerId);
        System.out.println("Workouts on disk for swimmer: " + list.size());
        Workout loaded = list.stream().filter(x -> x.getId().equals(w.getId())).findFirst().orElse(null);
        if (loaded == null) {
            System.out.println("ERROR: listing did not include the saved workout.");
            return;
        }
        // (If listWorkoutsFor returns full objects, skip; otherwise reload by id)
        System.out.println("Loaded (from list) groups=" + loaded.getGroupCount());
        if (loaded.getGroupCount() > 0) {
            SetGroup lg = loaded.getGroups().get(0);
            System.out.println("Group '" + lg.getName() + "' reps=" + lg.getReps() + ", sets=" + lg.getSetCount());
        }
    }
}
