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
import swimworkoutbuilder_javafx.ui.workout.WorkoutHeaderPane;

/**
 * Minimal shell for the application main layout.
 * Top:   ActionBar
 * Left:  SwimmerSection + SeedGridPane
 * Center:WorkoutHeaderPane + (placeholder) Workout Builder
 * Right: (placeholder) Preview
 */
public final class MainView extends BorderPane {

    public MainView() {
        // Top action bar
        ActionBar actionBar = new ActionBar();
        setTop(actionBar.node());

        // ---------- Left column ----------
        VBox leftCol = new VBox(12);
        leftCol.setPadding(new Insets(12));
        leftCol.setFillWidth(true);

        // Swimmer selector + details
        SwimmerSection swimmerSection = new SwimmerSection(AppState.get());
        Node swimmerNode = swimmerSection.node();

        // Seed grid
        SeedGridPane seedGrid = new SeedGridPane();
        Node seedNode = seedGrid;

        leftCol.getChildren().addAll(swimmerNode, seedNode);

        // ---------- Center column ----------
        VBox centerCol = new VBox(12);
        centerCol.setPadding(new Insets(12));
        centerCol.setFillWidth(true);

        // NEW: Workout header editor
        WorkoutHeaderPane workoutHeader = new WorkoutHeaderPane();

        // Placeholder for the upcoming workout builder surface
        Label builderPlaceholder = new Label("Workout Builder (coming next)");
        builderPlaceholder.getStyleClass().add("muted");
        builderPlaceholder.setMaxWidth(Double.MAX_VALUE);
        builderPlaceholder.setAlignment(Pos.CENTER_LEFT);

        centerCol.getChildren().addAll(workoutHeader.node(), builderPlaceholder);

        // ---------- Right column ----------
        VBox rightCol = new VBox(12);
        rightCol.setPadding(new Insets(12));
        rightCol.setFillWidth(true);

        Label previewPlaceholder = new Label("Preview (coming soon)");
        previewPlaceholder.getStyleClass().add("muted");
        rightCol.getChildren().add(previewPlaceholder);

        // ---------- 3-column layout ----------
        GridPane columns = new GridPane();
        columns.setHgap(12);
        columns.setVgap(0);
        columns.setPadding(new Insets(0, 12, 12, 12));

        ColumnConstraints cLeft   = new ColumnConstraints();
        ColumnConstraints cCenter = new ColumnConstraints();
        ColumnConstraints cRight  = new ColumnConstraints();

        cLeft.setPercentWidth(28);
        cCenter.setPercentWidth(44);
        cRight.setPercentWidth(28);

        cLeft.setHgrow(Priority.ALWAYS);
        cCenter.setHgrow(Priority.ALWAYS);
        cRight.setHgrow(Priority.ALWAYS);

        columns.getColumnConstraints().addAll(cLeft, cCenter, cRight);

        columns.add(leftCol,   0, 0);
        columns.add(centerCol, 1, 0);
        columns.add(rightCol,  2, 0);

        setCenter(columns);
    }
}