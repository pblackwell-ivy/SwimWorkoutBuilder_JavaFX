package swimworkoutbuilder_javafx.ui.workout;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import swimworkoutbuilder_javafx.state.AppState;

/**
 * Scrollable center column that hosts the workout header + builder.
 * Mirrors SwimmerPane's structure; only extra responsibility is
 * creating a shared WorkoutBuilderPresenter so header/builder stay in sync.
 */
public final class WorkoutPane {

    private final ScrollPane root;

    private final WorkoutHeaderPane headerPane;
    private final WorkoutBuilderPane builderPane;
    private final WorkoutBuilderPresenter presenter;

    public WorkoutPane() {
        // Same pattern as other panes: fetch app state via singleton
        AppState app = AppState.get();

        // Create the shared presenter and wire both sub-panes
        presenter  = new WorkoutBuilderPresenter(app);
        headerPane = new WorkoutHeaderPane(app);
        builderPane = new WorkoutBuilderPane(presenter);
        headerPane.bindPresenter(presenter);

        // Column VBox to hold header and builder panes
        var column = new VBox(headerPane.node(), builderPane.node());
        column.setFillWidth(true);
        column.getStyleClass().add("column-content");
        VBox.setVgrow(builderPane.node(), Priority.ALWAYS);

        // Scroll container to enable vertical scrolling when workout is too long to fit on screen
        root = new ScrollPane(column);
        root.setFitToWidth(true);
        root.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        root.getStyleClass().add("column-transparent");
    }

    public Node node() { return root; }

    // Optional getters if you need them later
    public WorkoutBuilderPresenter presenter() { return presenter; }
    public WorkoutHeaderPane headerPane() { return headerPane; }
    public WorkoutBuilderPane builderPane() { return builderPane; }
}