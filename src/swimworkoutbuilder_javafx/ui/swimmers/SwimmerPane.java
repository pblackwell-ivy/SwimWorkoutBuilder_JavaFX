package swimworkoutbuilder_javafx.ui.swimmers;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import swimworkoutbuilder_javafx.ui.seeds.SeedGridPane;

/**
 * {@code SwimmerPane} composes the full left column of the main application view.
 * It displays the current swimmer’s information and seed times within a scrollable layout.
 *
 * <p>Structure:</p>
 * <ul>
 *   <li>{@link SwimmerCard} — shows and edits swimmer details (name, team, timestamps)</li>
 *   <li>{@link SeedGridPane} — displays and edits the swimmer’s seed times</li>
 * </ul>
 *
 * <p>The {@link ScrollPane} is the root node, ensuring the entire column scrolls
 * vertically when content exceeds the available space. Visual appearance and spacing
 * are defined by the active theme CSS classes:
 * <code>column-scroll</code>, <code>column-vbox</code>, and <code>card</code>.</p>
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Provide a scrollable container for the swimmer details and seed times</li>
 *   <li>Synchronize updated timestamps when seed times are saved</li>
 *   <li>Delegate presentation logic to {@code SwimmerCard} and {@code SeedGridPane}</li>
 * </ul>
 */
public final class SwimmerPane {

    private final ScrollPane root;

    public SwimmerPane() {
        var swimmerCard = new SwimmerCard();
        var seedPane = new SeedGridPane();
        seedPane.getStyleClass().add("card");

        // if seeds save updates the swimmer updated timestamp, reflect it
        seedPane.setOnSeedsSaved(swimmerCard::refreshUpdatedFromApp);

        // Construct scrollable column with swimmer card and seed grid
        var column = new VBox(swimmerCard.node(), seedPane);
        root = new ScrollPane(column);

        // Configure scrollable column
        column.setFillWidth(true);
       // column.getStyleClass().add("card");
        column.getStyleClass().add("column-content");
        root.setFitToWidth(true);
        root.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        root.getStyleClass().add("column-transparent");
    }

    public Node node() { return root; }
}