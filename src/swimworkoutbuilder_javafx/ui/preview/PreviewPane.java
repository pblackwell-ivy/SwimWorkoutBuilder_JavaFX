package swimworkoutbuilder_javafx.ui.preview;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import swimworkoutbuilder_javafx.model.pacing.DefaultPacePolicy;
import swimworkoutbuilder_javafx.state.AppState;

/**
 * Read-only preview of the current workout using WorkoutPrinter output.
 * Default ctor uses AppState.get() and DefaultPacePolicy so Main.java stays unchanged.
 */
public final class PreviewPane extends BorderPane {

    private final TextArea text = new TextArea();
    private final PreviewPresenter presenter;

    /** Legacy-friendly: no DI required. */
    public PreviewPane() {
        this(AppState.get(), new DefaultPacePolicy());
    }

    /** Preferred: explicit state + policy (handy for tests). */
    public PreviewPane(AppState appState, DefaultPacePolicy policy) {
        this.presenter = new PreviewPresenter(appState, policy);
        buildUI();
        bind();
    }

    private void buildUI() {
        setPadding(new Insets(8));
        setPadding(new Insets(8));
        getStyleClass().add("surface"); // new
        var title = new Label("Preview");
        title.getStyleClass().add("section-title");

        text.setEditable(false);
        text.setWrapText(false);
        text.setStyle("-fx-font-family: monospace; -fx-font-size: 12;");

        setTop(title);
        setCenter(text);
    }

    private void bind() {
        text.textProperty().bind(presenter.textProperty());
    }
}