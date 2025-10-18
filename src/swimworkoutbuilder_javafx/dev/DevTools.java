package swimworkoutbuilder_javafx.dev;

import java.io.IOException;

/**
 * [UI Component] DevTools for the "swimworkoutbuilder_javafx" feature.
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
 * // Typical usage for DevTools
 * DevTools obj = new DevTools();
 * obj.toString(); // replace with real usage
 * }</pre>
 *
 * @author Parker Blackwell
 * @version 1.0
 * @since 2025-10-14
 */

public class DevTools {
    public static void main(String[] args) throws IOException {
        // allow: --backup-only, --force
        boolean backupOnly = java.util.Arrays.asList(args).contains("--backup-only");
        boolean force      = java.util.Arrays.asList(args).contains("--force");

        if (backupOnly) {
            RepositoryManager.backupBeforeDelete();
            return;
        }

        if (!force) {
            System.out.print("This will BACK UP then DELETE ~/.swimworkoutbuilder. Type YES to proceed: ");
            String answer = new java.io.BufferedReader(new java.io.InputStreamReader(System.in)).readLine();
            if (!"YES".equalsIgnoreCase(answer != null ? answer.trim() : "")) {
                System.out.println("Canceled.");
                return;
            }
        }

        RepositoryManager.clearAll();
    }
}
