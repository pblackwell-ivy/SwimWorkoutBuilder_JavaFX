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

public final class MainView extends BorderPane {

    private final AppState app = AppState.get();


    private final SwimmerSection swimmerSection = new SwimmerSection();
    private final SeedGridPane seedGridPane = new SeedGridPane();

    private final WorkoutBuilderPresenter builderPresenter = new WorkoutBuilderPresenter(app);
    private final WorkoutHeaderPane headerPane = new WorkoutHeaderPane(app);
    private final WorkoutBuilderPane builderPane = new WorkoutBuilderPane(builderPresenter);
    private final ActionBar actionBar = new ActionBar(builderPresenter); // ✅ passes presenter


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