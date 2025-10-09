package swimworkoutbuilder_javafx.ui.workout;

import javafx.beans.property.*;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.model.enums.Course;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.ui.workout.SetFormPresenter;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.model.SwimSet;
import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.model.Workout;
import swimworkoutbuilder_javafx.model.enums.Course;
import swimworkoutbuilder_javafx.model.enums.Effort;
import swimworkoutbuilder_javafx.model.enums.Equipment;
import swimworkoutbuilder_javafx.model.enums.StrokeType;
import swimworkoutbuilder_javafx.model.pacing.PacePolicy;
import swimworkoutbuilder_javafx.model.units.Distance;
import swimworkoutbuilder_javafx.model.units.TimeSpan;

import java.util.EnumSet;
import java.util.Objects;

/**
 * Presenter for the "Edit Set" dialog: holds user inputs and exposes
 * calculated Interval (@) and Goal time using the current PacePolicy.
 * The dialog (view) should bind controls to these properties and remain dumb.
 */
public final class SetFormPresenter {

    private final AppState app;
    private final PacePolicy pace;

    // ---- Inputs driven by the dialog controls ----------------------------
    private final ObjectProperty<StrokeType> stroke = new SimpleObjectProperty<>(StrokeType.FREESTYLE);
    private final ObjectProperty<Effort>     effort = new SimpleObjectProperty<>(Effort.EASY);
    private final IntegerProperty            reps   = new SimpleIntegerProperty(1);
    private final ObjectProperty<Distance>   distancePerRep =
            new SimpleObjectProperty<>(Distance.ofYards(25));
    private final ObjectProperty<EnumSet<Equipment>> equipment =
            new SimpleObjectProperty<>(EnumSet.noneOf(Equipment.class));
    private final ObjectProperty<Course>     course = new SimpleObjectProperty<>(Course.SCY);

    // ---- Outputs (read-only to the view) ---------------------------------
    private final StringProperty intervalText = new SimpleStringProperty("—");
    private final StringProperty goalText     = new SimpleStringProperty("—");

    public SetFormPresenter(AppState appState, PacePolicy pacePolicy) {
        this.app  = Objects.requireNonNull(appState);
        this.pace = Objects.requireNonNull(pacePolicy);

        // Recalc whenever inputs or current swimmer/workout change.
        app.currentSwimmerProperty().addListener((o, a, b) -> recalc());
        app.currentWorkoutProperty().addListener((o, a, b) -> recalc());
        stroke.addListener((o, a, b) -> recalc());
        effort.addListener((o, a, b) -> recalc());
        reps.addListener((o, a, b) -> recalc());
        distancePerRep.addListener((o, a, b) -> recalc());
        equipment.addListener((o, a, b) -> recalc());
        course.addListener((o, a, b) -> recalc());

        recalc();
    }

    // ---- Properties for the view to bind ---------------------------------
    public ObjectProperty<StrokeType> strokeProperty() { return stroke; }
    public ObjectProperty<Effort>     effortProperty() { return effort; }
    public IntegerProperty            repsProperty()   { return reps; }
    public ObjectProperty<Distance>   distancePerRepProperty() { return distancePerRep; }
    public ObjectProperty<EnumSet<Equipment>> equipmentProperty() { return equipment; }
    public ObjectProperty<Course>     courseProperty() { return course; }

    public ReadOnlyStringProperty intervalTextProperty() { return intervalText; }
    public ReadOnlyStringProperty goalTextProperty()     { return goalText; }

    // ---- Core calc --------------------------------------------------------
    private void recalc() {
        Swimmer s  = app.getCurrentSwimmer();
        Workout w  = app.getCurrentWorkout();

        // If we lack context or inputs, show dashes and bail.
        if (s == null || w == null || stroke.get() == null || effort.get() == null
                || distancePerRep.get() == null || course.get() == null || reps.get() <= 0) {
            intervalText.set("—");
            goalText.set("—");
            return;
        }

        // Build a temporary set snapshot from current inputs
        SwimSet tmp = new SwimSet();
        tmp.setStroke(stroke.get());
        tmp.setEffort(effort.get());
        tmp.setReps(Math.max(1, reps.get()));
        tmp.setDistancePerRep(distancePerRep.get());
        tmp.setEquipment(equipment.get() == null ? EnumSet.noneOf(Equipment.class) : equipment.get());
        tmp.setCourse(course.get());

        try {
            // Use repIndex = 1 as the representative calculation for the dialog.
            double goalSec  = pace.goalSeconds(w, tmp, s, 1);
            int    sendOff  = pace.intervalSeconds(w, tmp, s, 1);

            // Format using your TimeSpan.toString() (m:ss.hh)
            goalText.set(TimeSpan.ofSeconds(goalSec).toString());
            intervalText.set(TimeSpan.ofSeconds(sendOff).toString());
        } catch (RuntimeException ex) {
            // Missing seed or any policy error: show dashes so the user sees it
            intervalText.set("—");
            goalText.set("—");
        }
    }
}