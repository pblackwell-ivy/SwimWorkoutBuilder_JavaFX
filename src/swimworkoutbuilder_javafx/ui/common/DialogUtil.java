package swimworkoutbuilder_javafx.ui.common;

import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/** Small helpers to make dialogs center & not full-screen. */
public final class DialogUtil {
    private DialogUtil() { }

    /** Size and center a dialog over an owner window if present. */
    public static void prime(Stage dialog, Scene scene, Window owner, double width, double height, String title) {
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(title);
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.setWidth(width);
        dialog.setHeight(height);
        if (owner != null) dialog.initOwner(owner);
        dialog.centerOnScreen();
    }
}
