package swimworkoutbuilder_javafx.ui;

import javafx.scene.control.TextFormatter;

public final class UiUtil {
    private UiUtil() {}
    public static TextFormatter<String> maxLen(int max) {
        return new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            return (newText.length() <= max) ? change : null;
        });
    }
}