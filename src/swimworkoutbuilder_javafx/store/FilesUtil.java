package swimworkoutbuilder_javafx.store;

import java.io.File;

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
