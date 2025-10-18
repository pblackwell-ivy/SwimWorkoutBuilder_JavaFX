package swimworkoutbuilder_javafx.store;


import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.model.Workout;
/**
 * [UI Component] LocalStore for the "swimworkoutbuilder_javafx" feature.
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
 * // Typical usage for LocalStore
 * LocalStore obj = new LocalStore();
 * obj.toString(); // replace with real usage
 * }</pre>
 *
 * @author Parker Blackwell
 * @version 1.0
 * @since 2025-10-14
 */

public final class LocalStore {

    // --------- locations ----------
    private static final Path ROOT = Path.of(System.getProperty("user.home"), ".swimworkoutbuilder");
    private static final Path SWIMMERS_DIR = ROOT.resolve("swimmers");
    private static final Path WORKOUTS_DIR = ROOT.resolve("workouts");
    private static final Path LAST_FILE    = ROOT.resolve("last.properties"); // tiny INI-like file

    private LocalStore() {}

    // --------- bootstrap ----------
    private static void ensureDirs() throws IOException {
        Files.createDirectories(SWIMMERS_DIR);
        Files.createDirectories(WORKOUTS_DIR);
    }

    // --------- simple (de)serialization ----------
    private static <T> void writeObject(Path file, T obj) throws IOException {
        try (OutputStream fos = Files.newOutputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T readObject(Path file, Class<T> type) throws IOException {
        try (InputStream fis = Files.newInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            Object o = ois.readObject();
            return type.cast(o);
        } catch (ClassNotFoundException e) {
            throw new IOException("Corrupt file: " + file, e);
        } catch (ClassCastException e) {
            throw new IOException("Unexpected data in: " + file, e);
        }
    }

    // --------- file naming ----------
    private static Path swimmerFile(UUID swimmerId) {
        return SWIMMERS_DIR.resolve(swimmerId.toString() + ".bin");
    }
    private static Path workoutFile(UUID workoutId) {
        return WORKOUTS_DIR.resolve(workoutId.toString() + ".bin");
    }

    // ======================================================================
    // Swimmers
    // ======================================================================

    public static void saveSwimmer(Swimmer s) throws IOException {
        Objects.requireNonNull(s, "swimmer");
        ensureDirs();
        writeObject(swimmerFile(s.getId()), s);
    }

    public static Swimmer loadSwimmer(UUID id) throws IOException {
        Objects.requireNonNull(id, "id");
        ensureDirs();
        return readObject(swimmerFile(id), Swimmer.class);
    }

    public static void deleteSwimmer(UUID id) throws IOException {
        Objects.requireNonNull(id, "id");
        ensureDirs();
        Files.deleteIfExists(swimmerFile(id));
        // If the last selection pointed to this swimmer, clear it
        Properties p = readLastPropsQuiet();
        if (id.toString().equals(p.getProperty("lastSwimmerId"))) {
            p.remove("lastSwimmerId");
            p.remove("lastWorkoutId"); // also clear paired workout
            writeLastPropsQuiet(p);
        }
    }

    public static List<Swimmer> listAllSwimmers() throws IOException {
        ensureDirs();
        if (!Files.isDirectory(SWIMMERS_DIR)) return List.of();
        try (var stream = Files.list(SWIMMERS_DIR)) {
            List<Swimmer> list = new ArrayList<>();
            for (Path f : stream.filter(p -> p.getFileName().toString().endsWith(".bin")).collect(Collectors.toList())) {
                try { list.add(readObject(f, Swimmer.class)); } catch (Exception ignored) {}
            }
            // Optional: sort by name
            list.sort(Comparator.comparing((Swimmer s) -> s.getLastName() == null ? "" : s.getLastName())
                    .thenComparing(s -> s.getFirstName() == null ? "" : s.getFirstName()));
            return list;
        }
    }

    // ======================================================================
    // Workouts
    // ======================================================================

    public static void saveWorkout(Workout w) throws IOException {
        Objects.requireNonNull(w, "workout");
        ensureDirs();
        // touch updatedAt so the list sorts nicely if you want
        w.setUpdatedAt(Instant.now());
        writeObject(workoutFile(w.getId()), w);
    }

    public static Workout loadWorkout(UUID id) throws IOException {
        Objects.requireNonNull(id, "id");
        ensureDirs();
        return readObject(workoutFile(id), Workout.class);
    }

    public static void deleteWorkout(UUID id) throws IOException {
        Objects.requireNonNull(id, "id");
        ensureDirs();
        Files.deleteIfExists(workoutFile(id));
        // clear lastWorkout if it pointed here
        Properties p = readLastPropsQuiet();
        if (id.toString().equals(p.getProperty("lastWorkoutId"))) {
            p.remove("lastWorkoutId");
            writeLastPropsQuiet(p);
        }
    }

    /** Lists workouts for a given swimmerId (loads headers; same format you used before). */
    public static List<Workout> listWorkoutsFor(UUID swimmerId) throws IOException {
        Objects.requireNonNull(swimmerId, "swimmerId");
        ensureDirs();
        if (!Files.isDirectory(WORKOUTS_DIR)) return List.of();
        List<Workout> out = new ArrayList<>();
        try (var stream = Files.list(WORKOUTS_DIR)) {
            for (Path f : stream.filter(p -> p.getFileName().toString().endsWith(".bin")).collect(Collectors.toList())) {
                try {
                    Workout w = readObject(f, Workout.class);
                    if (swimmerId.equals(w.getSwimmerId())) out.add(w);
                } catch (Exception ignored) {}
            }
        }
        // Optional: sort by updatedAt desc, then name
        out.sort(Comparator.<Workout, Instant>comparing(w -> w.getUpdatedAt() == null ? Instant.EPOCH : w.getUpdatedAt())
                .reversed()
                .thenComparing(w -> w.getName() == null ? "" : w.getName()));
        return out;
    }

    // ======================================================================
    // Legacy convenience (kept to avoid editing many files right now)
    // ======================================================================

    /** Write both IDs at once (either may be null to clear). */
    public static void saveLast(UUID swimmerId, UUID workoutId) {
        Properties p = readLastPropsQuiet();
        if (swimmerId == null) p.remove("lastSwimmerId"); else p.setProperty("lastSwimmerId", swimmerId.toString());
        if (workoutId == null) p.remove("lastWorkoutId"); else p.setProperty("lastWorkoutId", workoutId.toString());
        writeLastPropsQuiet(p);
    }

    /** Previously used by MainView etc.; returns Optional as before. */
    public static Optional<UUID> lastSwimmer() {
        Properties p = readLastPropsQuiet();
        String s = p.getProperty("lastSwimmerId");
        try { return (s == null || s.isBlank()) ? Optional.empty() : Optional.of(UUID.fromString(s)); }
        catch (Exception e) { return Optional.empty(); }
    }

    /** Previously used by MainView etc.; returns Optional as before. */
    public static Optional<UUID> lastWorkout() {
        Properties p = readLastPropsQuiet();
        String s = p.getProperty("lastWorkoutId");
        try { return (s == null || s.isBlank()) ? Optional.empty() : Optional.of(UUID.fromString(s)); }
        catch (Exception e) { return Optional.empty(); }
    }

    // --------- tiny props helpers for “last selection” ----------
    private static Properties readLastPropsQuiet() {
        Properties p = new Properties();
        try {
            ensureDirs();
            if (Files.exists(LAST_FILE)) {
                try (InputStream in = Files.newInputStream(LAST_FILE)) {
                    p.load(in);
                }
            }
        } catch (Exception ignored) {}
        return p;
    }

    private static void writeLastPropsQuiet(Properties p) {
        try {
            ensureDirs();
            try (OutputStream out = Files.newOutputStream(LAST_FILE)) {
                p.store(out, "SwimWorkoutBuilder last selections");
            }
        } catch (Exception ignored) {}
    }
}
