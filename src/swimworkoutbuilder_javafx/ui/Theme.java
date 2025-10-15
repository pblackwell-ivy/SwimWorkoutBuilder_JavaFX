package swimworkoutbuilder_javafx.ui;

import javafx.scene.Scene;

/**
 * Utility for applying the global SwimWorkoutBuilder CSS theme to any Scene.
 * Use this in dialogs or secondary stages that don't inherit the main stylesheet.
 */
public final class Theme {
    private Theme() {}

    // Absolute classpath path to the app-wide stylesheet
    private static final String CSS_PATH = "/swimworkoutbuilder_javafx/ui/styles.css";

    /**
     * Attach the global stylesheet to a Scene.
     * Safe to call even if the file isn't found (logs a warning instead of throwing).
     *
     * @param scene  the Scene to apply the stylesheet to
     * @param anchor the class to use for classloader lookup (usually caller.class)
     */
    public static void apply(Scene scene, Class<?> anchor) {
        var cssUrl = anchor.getResource(CSS_PATH);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("⚠️ Theme: styles.css not found at " + CSS_PATH);
        }
    }
}
