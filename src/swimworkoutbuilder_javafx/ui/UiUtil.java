package swimworkoutbuilder_javafx.ui;

import javafx.scene.control.TextFormatter;
/**
 * [UI Component] UiUtil for the "swimworkoutbuilder_javafx" feature.
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
 * // Typical usage for UiUtil
 * UiUtil obj = new UiUtil();
 * obj.toString(); // replace with real usage
 * }</pre>
 *
 * @author Parker Blackwell
 * @version 1.0
 * @since 2025-10-14
 */

public final class UiUtil {
    private UiUtil() {}
    public static TextFormatter<String> maxLen(int max) {
        return new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            return (newText.length() <= max) ? change : null;
        });
    }
}
