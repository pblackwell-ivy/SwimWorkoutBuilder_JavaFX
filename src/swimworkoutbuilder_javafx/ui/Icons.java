package swimworkoutbuilder_javafx.ui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.InputStream;

/**
 * Utility for loading and sizing icons consistently.
 *
 * Example:
 *     Button edit = new Button();
 *     edit.setGraphic(Icons.make("pencil", 16));
 */
public final class Icons {
    private Icons() {}

    public static ImageView make(String name, double size) {
        String path = "/icons/" + name + ".png";  // or .svg if you use SVG loader
        InputStream is = Icons.class.getResourceAsStream(path);
        if (is == null) {
            System.err.println("⚠️ Icon not found: " + path);
            return new ImageView();
        }

        ImageView iv = new ImageView(new Image(is, size, size, true, true));
        iv.getStyleClass().add("icon");
        return iv;
    }
}