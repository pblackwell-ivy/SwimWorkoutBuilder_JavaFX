package swimworkoutbuilder_javafx.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.ui.seeds.SeedGridPane;
import swimworkoutbuilder_javafx.ui.shell.ActionBar;
import swimworkoutbuilder_javafx.ui.swimmers.SwimmerSection;
import swimworkoutbuilder_javafx.ui.workout.WorkoutBuilderPane;
import swimworkoutbuilder_javafx.ui.workout.WorkoutBuilderPresenter;
import swimworkoutbuilder_javafx.ui.workout.WorkoutHeaderPane;
/**
 * [UI Component] MainView for the "swimworkoutbuilder_javafx" feature.
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
 * // Typical usage for MainView
 * MainView obj = new MainView();
 * obj.toString(); // replace with real usage
 * }</pre>
 *
 * @author Parker Blackwell
 * @version 1.0
 * @since 2025-10-14
 */

public final class MainView extends BorderPane {

    private final AppState app = AppState.get();


    private final SwimmerSection swimmerSection = new SwimmerSection();
    private final SeedGridPane seedGridPane = new SeedGridPane();

    private final WorkoutBuilderPresenter builderPresenter = new WorkoutBuilderPresenter(app);
    private final WorkoutHeaderPane headerPane = new WorkoutHeaderPane(app);
    private final WorkoutBuilderPane builderPane = new WorkoutBuilderPane(builderPresenter);
    private final ActionBar actionBar = new ActionBar(); // ✅ passes presenter


    public MainView() {
        setTop(pad(actionBar.node(), 8, 12, 6, 12));

        // LEFT: Swimmer info + seed grid
        VBox leftColumn = new VBox(12, swimmerSection.node(), seedGridPane);
        leftColumn.getStyleClass().add("surface");              // new
        leftColumn.setPadding(new Insets(8, 12, 8, 12));
        leftColumn.setPrefWidth(300);
        setLeft(leftColumn);

        // CENTER: Workout header + builder
        VBox centerColumn = new VBox(12, headerPane.node(), builderPane.node());
        centerColumn.getStyleClass().add("surface");            // new
        VBox.setVgrow(builderPane.node(), Priority.ALWAYS);
        centerColumn.setPadding(new Insets(8));
        setCenter(centerColumn);

        headerPane.bindPresenter(builderPresenter); // forces live header update when groups/sets change

        // RIGHT: Preview placeholder
        VBox rightColumn = new VBox();
        rightColumn.getStyleClass().add("surface");             // new
        rightColumn.setPadding(new Insets(8, 12, 8, 8));
        Label previewLabel = new Label("Preview (coming soon)");
        previewLabel.getStyleClass().add("muted");              // was text-subtle -> muted
        rightColumn.getChildren().add(previewLabel);
        setRight(rightColumn);

        setPadding(new Insets(6));
    }

    private static Node pad(Node n, double t, double r, double b, double l) {
        HBox box = new HBox(n);
        box.setPadding(new Insets(t, r, b, l));
        return box;
    }
}
