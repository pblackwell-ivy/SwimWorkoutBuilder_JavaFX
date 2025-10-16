package swimworkoutbuilder_javafx.ui;

import javafx.scene.Scene;
import java.net.URL;

/**
 * Utility for applying the global SwimWorkoutBuilder CSS theme to any Scene.
 * Use this in dialogs or secondary stages that don't inherit the main stylesheet.
 */

public final class Theme {
    private Theme() {}

    // Match Main.java
    private static final String CSS_NAME = "styles-ocean-depth-opt-b.css";
    private static final String CSS_PATH = "/swimworkoutbuilder_javafx/ui/" + CSS_NAME;

    /** Convenient default that uses Theme.class for resource lookup. */
    public static void apply(Scene scene) {
        apply(scene, Theme.class);
    }

    /**
     * Attach the global stylesheet to a Scene using the given anchor class
     * for classloader/resource lookup (useful when called from other modules).
     */
    public static void apply(Scene scene, Class<?> anchor) {
        if (scene == null) return;
        Class<?> lookup = (anchor != null) ? anchor : Theme.class;

        URL cssUrl = lookup.getResource(CSS_PATH);
        if (cssUrl == null) {
            System.err.println("WARNING: " + CSS_NAME + " stylesheet not found at " + CSS_PATH);
            return;
        }

        // Prevent duplicates if applied multiple times
        scene.getStylesheets().removeIf(s -> s.endsWith("/" + CSS_NAME) || s.contains(CSS_NAME));
        scene.getStylesheets().add(cssUrl.toExternalForm());
    }
}
