package swimworkoutbuilder_javafx.store;

import java.io.File;
/**
 * [UI Component] FilesUtil for the "swimworkoutbuilder_javafx" feature.
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
 * // Typical usage for FilesUtil
 * FilesUtil obj = new FilesUtil();
 * obj.toString(); // replace with real usage
 * }</pre>
 *
 * @author Parker Blackwell
 * @version 1.0
 * @since 2025-10-14
 */

public class FilesUtil {
    private FilesUtil() {}

    public static File baseDir() {
        String home = System.getProperty("user.home");
        File dir = new File(home, "Documents/SwimWorkoutBuilder");
        if(!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
    public static File swimmersDir() { return ensure(new File(baseDir(), "swimmers")); }
    public static File workoutsDir() { return ensure(new File(baseDir(), "workouts")); }
    public static File appFile() { return new File(baseDir(), "app.properties"); }

    private static File ensure(File f) {
        if(!f.exists()) f.mkdirs();
        return f;
    }
}
