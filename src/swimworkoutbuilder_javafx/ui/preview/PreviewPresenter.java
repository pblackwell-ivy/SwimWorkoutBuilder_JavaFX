package swimworkoutbuilder_javafx.ui.preview;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.model.pacing.PacePolicy;
import swimworkoutbuilder_javafx.model.utils.WorkoutPrinter;
import swimworkoutbuilder_javafx.state.AppState;

/**
 * Presenter for the read-only Workout preview/print text.
 * Captures WorkoutPrinter's console output into a String for UI display.
 */
public final class PreviewPresenter {

    private final AppState app;
    private final PacePolicy policy;
    private final StringProperty text = new SimpleStringProperty("");

    public PreviewPresenter(AppState appState, PacePolicy pacePolicy) {
        this.app = appState;
        this.policy = pacePolicy;

        // Re-render when either the workout or swimmer changes.
        app.currentWorkoutProperty().addListener((o, a, b) -> render());
        app.currentSwimmerProperty().addListener((o, a, b) -> render());

        render(); // initial
    }

    private void render() {
        Workout w = app.getCurrentWorkout();
        Swimmer s = app.getCurrentSwimmer();
        if (w == null || s == null) {
            text.set("");
            return;
        }
        text.set(renderToString(w, s));
    }

    private String renderToString(Workout w, Swimmer s) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream ps = new PrintStream(baos)) {
            System.setOut(ps);
            WorkoutPrinter.printWorkout(w, s, policy);
        } finally {
            System.setOut(original);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    public ReadOnlyStringProperty textProperty() { return text; }
}
