package swimworkoutbuilder_javafx.dev;

import swimworkoutbuilder_javafx.model.*;
import swimworkoutbuilder_javafx.model.enums.*;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.store.LocalStore;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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