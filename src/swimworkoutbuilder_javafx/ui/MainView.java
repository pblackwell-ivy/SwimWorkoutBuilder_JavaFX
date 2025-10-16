package swimworkoutbuilder_javafx.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.ui.seeds.SeedGridPane;
import swimworkoutbuilder_javafx.ui.shell.ActionBar;
import swimworkoutbuilder_javafx.ui.swimmers.SwimmerPane;
import swimworkoutbuilder_javafx.ui.workout.WorkoutBuilderPane;
import swimworkoutbuilder_javafx.ui.workout.WorkoutBuilderPresenter;
import swimworkoutbuilder_javafx.ui.workout.WorkoutHeaderPane;
import swimworkoutbuilder_javafx.ui.workout.WorkoutPane;

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


    private final SwimmerPane swimmerPane = new SwimmerPane();  // swimmerPane contains the SwimmerCard and the SeedGridPane

    private final WorkoutBuilderPresenter builderPresenter = new WorkoutBuilderPresenter(app);
    private final WorkoutHeaderPane headerPane = new WorkoutHeaderPane(app);
    private final WorkoutBuilderPane builderPane = new WorkoutBuilderPane(builderPresenter);
    private final ActionBar actionBar = new ActionBar();

    private final WorkoutPane workoutPane = new WorkoutPane();

    public MainView() {
        setTop(pad(actionBar.node(), 8, 12, 6, 12));

        // LEFT COLUMN: Swimmer info + seed grid
        VBox leftColumn = new VBox(12, swimmerPane.node());
        leftColumn.getStyleClass().add("column-sheet");
        leftColumn.setPrefWidth(300);
        setLeft(leftColumn);

        // CENTER: wrapper sheet + scrollable workout column
        VBox centerColumn = new VBox(12, headerPane.node(), builderPane.node());
        centerColumn.getStyleClass().add("column-sheet");
        setCenter(centerColumn);

        headerPane.bindPresenter(builderPresenter); // forces live header update when groups/sets change

        // RIGHT: Preview placeholder
        VBox rightColumn = new VBox();
        rightColumn.getStyleClass().add("column-sheet");             // new
        rightColumn.setPadding(new Insets(8, 12, 8, 8));
        Label previewLabel = new Label("Preview (coming soon)");
        previewLabel.getStyleClass().add("muted");              // was text-subtle -> muted
        rightColumn.getChildren().add(previewLabel);
        setRight(rightColumn);

        setPadding(new Insets(6));
        // After you create the wrappers:
        BorderPane.setMargin(leftColumn,   new Insets(0, 3, 0, 0));  // right gutter
        BorderPane.setMargin(centerColumn, new Insets(0, 3, 0, 3));  // left+right gutter
        BorderPane.setMargin(rightColumn,  new Insets(0, 0, 0, 3));  // left gutter
    }

    private static Node pad(Node n, double t, double r, double b, double l) {
        HBox box = new HBox(n);
        box.setPadding(new Insets(t, r, b, l));
        return box;
    }
}
