package swimworkoutbuilder_javafx.dev;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for managing and resetting local repository data during development.
 *
 * <p>This helper provides safe ways to delete or back up all serialized data files
 * (e.g., swimmers.dat, workouts.dat) stored in the application's data directory.
 * It is intended strictly for use during development and testing — not for production builds.</p>
 *
 * <p><b>Default storage location:</b>
 * <code>~/.swimworkoutbuilder/</code></p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * // Back up and clear all saved data
 * RepositoryManager.clearAll();
 *
 * // Or manually trigger backup only
 * RepositoryManager.backupBeforeDelete();
 * }</pre>
 *
 * @author Parker Blackwell
 * @version 1.1 (with backup support)
 * @since 2025-10-11
 */
public final class RepositoryManager {

    private RepositoryManager() {}

    /** Base data directory (default = ~/.swimworkoutbuilder) */
    private static final Path DATA_DIR =
            Path.of(System.getProperty("user.home"), ".swimworkoutbuilder");

    /** Backup directory (default = ~/.swimworkoutbuilder_backups) */
    private static final Path BACKUP_DIR =
            Path.of(System.getProperty("user.home"), ".swimworkoutbuilder_backups");

    // ----------------------------------------------------------
    // Public API
    // ----------------------------------------------------------

    /**
     * Deletes all repository data under {@code ~/.swimworkoutbuilder}.
     * Automatically creates a ZIP backup before deletion.
     */
    public static void clearAll() {
        if (!Files.exists(DATA_DIR)) {
            System.out.println("ℹ️  No repository data found — nothing to clear.");
            return;
        }

        System.out.println("🧹 Preparing to clear all repository data in: " + DATA_DIR.toAbsolutePath());
        backupBeforeDelete();

        try {
            Files.walk(DATA_DIR)
                    .sorted(Comparator.reverseOrder()) // delete children first
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (file.delete()) {
                            System.out.println("   ✅ Deleted: " + file.getName());
                        } else {
                            System.err.println("   ⚠️ Could not delete: " + file.getName());
                        }
                    });

            if (!Files.exists(DATA_DIR)) {
                System.out.println("✅ All repository data successfully cleared.\n");
            } else {
                System.out.println("⚠️ Some files could not be deleted. Check permissions.\n");
            }

        } catch (IOException e) {
            System.err.println("❌ Error clearing repository data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a ZIP archive backup of the current repository folder.
     * The backup file is stored under {@code ~/.swimworkoutbuilder_backups/}
     * and named with a timestamp (e.g., {@code backup_2025-10-11_1530.zip}).
     */
    public static void backupBeforeDelete() {
        if (!Files.exists(DATA_DIR)) {
            System.out.println("ℹ️  No data directory to back up.");
            return;
        }

        try {
            Files.createDirectories(BACKUP_DIR);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"));
            Path zipPath = BACKUP_DIR.resolve("backup_" + timestamp + ".zip");

            System.out.println("📦 Creating backup: " + zipPath.toAbsolutePath());

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
                Files.walk(DATA_DIR)
                        .filter(Files::isRegularFile)
                        .forEach(path -> {
                            String relative = DATA_DIR.relativize(path).toString();
                            try (InputStream in = Files.newInputStream(path)) {
                                zos.putNextEntry(new ZipEntry(relative));
                                in.transferTo(zos);
                                zos.closeEntry();
                            } catch (IOException e) {
                                System.err.println("⚠️ Failed to back up file: " + path + " (" + e.getMessage() + ")");
                            }
                        });
            }

            System.out.println("✅ Backup complete: " + zipPath.toAbsolutePath() + "\n");
        } catch (IOException e) {
            System.err.println("❌ Failed to create backup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Deletes a specific repository file (e.g., "swimmers.dat" or "workouts.dat").
     *
     * @param fileName the name of the file to delete (case-sensitive)
     */
    public static void clearFile(String fileName) {
        Path target = DATA_DIR.resolve(fileName);
        try {
            if (Files.deleteIfExists(target)) {
                System.out.println("✅ Deleted file: " + target);
            } else {
                System.out.println("ℹ️  File not found: " + target);
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to delete file " + fileName + ": " + e.getMessage());
        }
    }
}
