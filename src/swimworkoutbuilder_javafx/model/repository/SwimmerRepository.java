package swimworkoutbuilder_javafx.model.repository;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import swimworkoutbuilder_javafx.model.Swimmer;

/**
 * @deprecated Legacy persistence prototype.
 * <p>
 * This class was part of an early experiment for saving and loading swimmers
 * to a CSV file before {@link swimworkoutbuilder_javafx.store.LocalStore}
 * was implemented. It is no longer referenced anywhere in the application.
 * <p>
 * Retained for documentation and grading purposes only.
 */
@Deprecated
public final class SwimmerRepository {

    // private no-arg constructor: can't create instances of SwimmerRepository, "SwimmerRepositor repo = new SwimmerRepository()" is not allowed.
    private SwimmerRepository() {}

   // ----- File management helpers
    /** Builds the path based on the user's system ~/.swimworkoutbuilder/swimmers.csv */
    public static Path dataDir() {
        String home = System.getProperty("user.home");         // e.g., /Users/parkerblackwell
        return Paths.get(home, ".swimworkoutbuilder");  // e.g., /Users/parkerblackwell/.swimworkoutbuilder
    }
    private static Path swimmersFile() {                        //  e.g., /Users/parkerblackwell/.swimworkoutbuilder/swimmers.csv
        return dataDir().resolve("swimmers.csv");         // .resolve appends "swimmers.csv" to the path,
    }

    /** Helper method to ensure data directory and swimmers.csv exists.  If not, create with header row. */
    private static void ensureFileReady() throws IOException {
        Files.createDirectories(dataDir());
        Path f = swimmersFile();
        if (Files.notExists(f)) {
            Files.write(f, Collections.singletonList("id,first,last,preferred,team"),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        }
    }

    // ----- CSV 'escaping' helpers: q -> writing to CSV, uq -> reading from CSV */
    /** Wrap a string in quotes and replace quotes inside a string with double quotes. */
    private static String q(String s) {
        if (s == null) return "";
        String esc = s.replace("\"", "\"\"");
        return "\"" + esc + "\"";
    }
    /** Unescape CSV: strip surrounding quotes and restore doubled "" to ". */
    private static String uq(String s) {
        if (s == null) return null;
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
        }
        return s.isEmpty() ? null : s;
    }

    /** Parses one line of CSV text into columns.  Handles commas inside quotes (e.g., "Smith, Jr."). */
    private static String[] parseCsvLine(String line) {
        List<String> cols = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuote = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
                cur.append(c);
            } else if (c == ',' && !inQuote) {
                cols.add(cur.toString());   // finish one column
                cur.setLength(0);           // reset buffer
            } else {
                cur.append(c);
            }
        }
        cols.add(cur.toString());           // add last column
        return cols.toArray(new String[0]);
    }

    // ----- Public API
    /** Load swimmers from CSV into memory (ignore invalid lines). */
    public static List<Swimmer> loadAll() throws IOException {
        ensureFileReady();      // Folder / file exists, if not create it

        /** Use BufferedReader to read the file line-by-line. */
        try (BufferedReader reader = Files.newBufferedReader(swimmersFile(), StandardCharsets.UTF_8)) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            if (lines.isEmpty()) return Collections.emptyList();

            List<Swimmer> out = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                if (i == 0 && line.startsWith("id,")) continue; // skip header
                if (line.startsWith("#")) continue;             // allow comments

                String[] cols = parseCsvLine(line);
                if (cols.length < 5) {
                    System.err.println("WARNING: Skipping invalid line in swimmers.csv: " + line);
                    continue;
                }
                UUID id = UUID.fromString(uq(cols[0].trim()));
                String first = uq(cols[1]);
                String last = uq(cols[2]);
                String preferred = uq(cols[3]);
                String team = uq(cols[4]);
                if (first == null || last == null) {
                    System.err.println("WARNING: Skipping invalid line in swimmers.csv: " + line);
                    continue;
                }
                out.add(new Swimmer(id, first, last, preferred, team));  // Use UUID-aware constructor
            }
            return out;
        }
    }

    /** Append a swimmer to the CSV. */
    public static void append(Swimmer s) throws IOException {
        ensureFileReady();
        String line = String.join(",",
                q(s.getId().toString()),
                q(s.getFirstName()),
                q(s.getLastName()),
                q(s.getPreferredName()),
                q(s.getTeamName())
        );
        Files.write(
                swimmersFile(),
                Collections.singletonList(line + System.lineSeparator()),
                StandardCharsets.UTF_8,
                StandardOpenOption.APPEND
        );
    }
}
