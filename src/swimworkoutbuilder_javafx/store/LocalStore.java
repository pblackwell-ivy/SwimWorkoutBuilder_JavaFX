package swimworkoutbuilder_javafx.store;

import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.model.Workout;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public final class LocalStore {
    private LocalStore() {}

    // --- Directories & app file ---
    public static File baseDir() {
        String home = System.getProperty("user.home");
        File dir = new File(home, "Documents/SwimWorkoutBuilder");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }
    public static File swimmersDir() { return ensure(new File(baseDir(), "swimmers")); }
    public static File workoutsDir() { return ensure(new File(baseDir(), "workouts")); }
    public static File appFile()     { return new File(baseDir(), "app.properties"); }
    private static File ensure(File f) { if (!f.exists()) f.mkdirs(); return f; }

    // --- Swimmers ---
    public static void saveSwimmer(Swimmer s) throws IOException {
        File f = new File(swimmersDir(), s.getId().toString() + ".bin");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(s);
        }
    }

    public static Swimmer loadSwimmer(UUID id) throws IOException, ClassNotFoundException {
        File f = new File(swimmersDir(), id.toString() + ".bin");
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (Swimmer) ois.readObject();
        }
    }

    public static List<Swimmer> listSwimmers() {
        File[] files = swimmersDir().listFiles((d, n) -> n.endsWith(".bin"));
        if (files == null) return List.of();
        List<Swimmer> out = new ArrayList<>();
        for (File f : files) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                out.add((Swimmer) ois.readObject());
            } catch (Exception ignored) {}
        }
        out.sort(Comparator.comparing(Swimmer::getLastName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Swimmer::getFirstName, String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    // --- Workouts ---
    public static void saveWorkout(Workout w) throws IOException {
        File f = new File(workoutsDir(), w.getId().toString() + ".bin");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(w);
        }
    }

    public static Workout loadWorkout(UUID id) throws IOException, ClassNotFoundException {
        File f = new File(workoutsDir(), id.toString() + ".bin");
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (Workout) ois.readObject();
        }
    }

    public static List<Workout> listWorkoutsFor(UUID swimmerId) {
        File[] files = workoutsDir().listFiles((d, n) -> n.endsWith(".bin"));
        if (files == null) return List.of();
        List<Workout> out = new ArrayList<>();
        for (File f : files) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                Workout w = (Workout) ois.readObject();
                if (Objects.equals(w.getSwimmerId(), swimmerId)) out.add(w);
            } catch (Exception ignored) {}
        }
        out.sort(Comparator.comparing(Workout::getName, String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    // --- Remember last open ---
    public static void saveLast(UUID swimmerId, UUID workoutId) {
        Properties p = new Properties();
        if (swimmerId != null) p.setProperty("last.swimmer", swimmerId.toString());
        if (workoutId != null) p.setProperty("last.workout", workoutId.toString());
        try (OutputStream os = Files.newOutputStream(appFile().toPath())) {
            p.store(os, "SwimWorkoutBuilder app");
        } catch (IOException ignored) {}
    }

    public static Optional<UUID> lastSwimmer() {
        Properties p = new Properties();
        try (InputStream is = Files.newInputStream(appFile().toPath())) {
            p.load(is);
        } catch (IOException ignored) {}
        String s = p.getProperty("last.swimmer");
        try { return (s==null)?Optional.empty():Optional.of(UUID.fromString(s)); }
        catch (Exception e) { return Optional.empty(); }
    }

    public static Optional<UUID> lastWorkout() {
        Properties p = new Properties();
        try (InputStream is = Files.newInputStream(appFile().toPath())) {
            p.load(is);
        } catch (IOException ignored) {}
        String s = p.getProperty("last.workout");
        try { return (s==null)?Optional.empty():Optional.of(UUID.fromString(s)); }
        catch (Exception e) { return Optional.empty(); }
    }

    // --- Deletes (used by UI) ---
    /** Delete swimmer file, clear "last" if needed, and delete all of their workouts. */
    public static boolean deleteSwimmer(UUID swimmerId) {
        File f = new File(swimmersDir(), swimmerId.toString() + ".bin");
        boolean ok = !f.exists() || f.delete();

        // Clear "last" if it matched this swimmer
        var lastS = lastSwimmer();
        if (lastS.isPresent() && lastS.get().equals(swimmerId)) {
            saveLast(null, null);
        }
        // Delete all workouts for this swimmer
        deleteWorkoutsFor(swimmerId);
        return ok;
    }

    /** Delete all workouts associated with a swimmer. Returns number deleted. */
    public static int deleteWorkoutsFor(UUID swimmerId) {
        File[] files = workoutsDir().listFiles((d, name) -> name.endsWith(".bin"));
        int count = 0;
        if (files == null) return 0;
        for (File f : files) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                Workout w = (Workout) ois.readObject();
                if (Objects.equals(w.getSwimmerId(), swimmerId)) {
                    if (f.delete()) count++;
                }
            } catch (Exception ignored) {}
        }
        return count;
    }
}