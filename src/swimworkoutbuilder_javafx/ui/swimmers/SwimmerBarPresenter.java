package swimworkoutbuilder_javafx.ui.swimmers;

import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.collections.ListChangeListener;

import swimworkoutbuilder_javafx.model.Swimmer;
import swimworkoutbuilder_javafx.state.AppState;
import swimworkoutbuilder_javafx.store.LocalStore;
import swimworkoutbuilder_javafx.ui.dialogs.SwimmerFormDialog;

import java.util.Optional;

/**
 * Presenter for the swimmer chooser.
 * Owns the list and selected swimmer; wires Add/Edit/Delete via dialogs + LocalStore.
 */
public final class SwimmerBarPresenter {

    private final AppState app;
    private final ObservableList<Swimmer> swimmers = FXCollections.observableArrayList();

    public SwimmerBarPresenter(AppState appState) {
        this.app = appState;

        // keep local list in sync with AppState.getSwimmers()
        swimmers.setAll(app.getSwimmers());

        // listener that mirrors any add/remove on the AppState list
        final ListChangeListener<Swimmer> listListener = change -> {
            // Simple + robust: just mirror the whole list when anything changes
            swimmers.setAll(app.getSwimmers());
        };

        // 1) If the AppState replaces the whole list object, swap listeners and mirror
        app.swimmersProperty().addListener((obs, oldList, newList) -> {
            if (oldList != null) oldList.removeListener(listListener);
            if (newList != null) newList.addListener(listListener);
            swimmers.setAll(app.getSwimmers());
        });

        // 2) Also watch the current list instance for item changes
        if (app.getSwimmers() != null) {
            app.getSwimmers().addListener(listListener);
        }
    }

    /** Observable list of swimmers for the ComboBox. */
    public ObservableList<Swimmer> swimmers() { return swimmers; }

    /** Currently selected swimmer (bind bidirectionally to ComboBox). */
    public ObjectProperty<Swimmer> selectedProperty() { return app.currentSwimmerProperty(); }

    // ---- Commands ---------------------------------------------------------

    public void addNew() {
        Swimmer created = SwimmerFormDialog.show(null);
        if (created == null) return;

        try { LocalStore.saveSwimmer(created); } catch (Exception ignored) {}

        app.getSwimmers().add(created);
        app.setCurrentSwimmer(created);

        // remember last selection (workout may be null)
        try {
            LocalStore.saveLast(
                    created.getId(),
                    app.getCurrentWorkout() == null ? null : app.getCurrentWorkout().getId()
            );
        } catch (Exception ignored) {}
    }

    public void editSelected() {
        Swimmer cur = app.getCurrentSwimmer();
        if (cur == null) return;

        Swimmer edited = SwimmerFormDialog.show(cur);
        if (edited == null) return;

        // copy edited name fields back into existing instance
        cur.setFirstName(edited.getFirstName());
        cur.setLastName(edited.getLastName());
        cur.setPreferredName(edited.getPreferredName());

        try { LocalStore.saveSwimmer(cur); } catch (Exception ignored) {}
        // selection remains the same
    }

    public void deleteSelected() {
        Swimmer cur = app.getCurrentSwimmer();
        if (cur == null) return;

        boolean confirm = confirm("Delete swimmer",
                "Delete " + nameOf(cur) + "? This cannot be undone.");
        if (!confirm) return;

        // Try to delete from store if supported
        try { LocalStore.deleteSwimmer(cur.getId()); } catch (Exception ignored) {}

        app.getSwimmers().remove(cur);

        // pick a new selection
        Swimmer next = app.getSwimmers().isEmpty() ? null : app.getSwimmers().get(0);
        app.setCurrentSwimmer(next);

        try {
            LocalStore.saveLast(
                    next == null ? null : next.getId(),
                    app.getCurrentWorkout() == null ? null : app.getCurrentWorkout().getId()
            );
        } catch (Exception ignored) {}
    }

    // ---- helpers ----------------------------------------------------------

    private static boolean confirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL);
        a.setTitle(title);
        a.setHeaderText(null);
        Optional<ButtonType> r = a.showAndWait();
        return r.isPresent() && r.get() == ButtonType.OK;
    }

    private static String nameOf(Swimmer s) {
        String base = (s.getFirstName() + " " + s.getLastName()).trim();
        String pref = s.getPreferredName();
        return (pref == null || pref.isBlank()) ? base : pref + " (" + base + ")";
    }
}