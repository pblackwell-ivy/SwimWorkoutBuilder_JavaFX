SwimWorkoutBuilder (JavaFX-only)

This project demonstrates a complete end-to-end JavaFX application following MVVM architecture principles, using observable state, modular presenters, and persistable domain models. The user interface is fully responsive, themeable, and adheres to consistent design conventions.

A desktop MVP for creating structured swim workouts. Coaches (or swimmers) can create swimmers with seed times, then build workouts composed of groups and sets. The app computes goal and interval times from seed times, stroke, effort, equipment, and pool length.

Tech: Java 21+ (tested with JDK 24), JavaFX 21, Gradle/Maven not required (IntelliJ run config OK).
OS: Built and tested on macOS; should run anywhere JavaFX 21 is available.

⸻

Features (MVP)
	•	Swimmer management
	•	Create, edit, delete swimmers
	•	Per-stroke seed times (yd/m), editable as seconds or mm:ss.hh
	•	Persisted locally (no network/database)
	•	Workout builder
	•	Workout header: name, notes, pool length (25y, 25m, 50m), created/updated timestamps, totals (distance, swim/rest, duration)
	•	Groups contain sets; sets include reps × distance, stroke, effort level, equipment, and notes
	•	Live goal and interval time suggestions (policy-based)
	•	Reorder groups/sets; edit & delete in place
	•	“Unsaved” chip and Save/Delete actions with clear dirty-state behavior
	•	Equipment
	•	Toggle fins, paddles, kickboard, pull buoy, snorkel, parachute, drag socks
	•	Icons render in the builder rows
	•	Usability / UI
	•	Clean mac-style theme (ocean depth)
	•	Header remains fixed while the groups/sets panel scrolls
	•	Text formatting: bold workout name, italic notes, compact rows

⸻

How to Run

Option A — IntelliJ IDEA (recommended)
	1.	Open project in IntelliJ.
	2.	Ensure Project SDK is Java 21+ (tested with JDK 24).
	3.	Download JavaFX 21 SDK and set Run/Debug Configuration for the app:
	•	Main class: swimworkoutbuilder_javafx.Main
	•	VM options (adjust path to your JavaFX SDK):

--module-path /path/to/javafx-sdk-21/lib
--add-modules javafx.controls,javafx.fxml
-Djava.library.path=/path/to/javafx-sdk-21/lib

	4.	Run ▶️

Option B — Command line
java \
  --module-path /path/to/javafx-sdk-21/lib \
  --add-modules javafx.controls,javafx.fxml \
  -Djava.library.path=/path/to/javafx-sdk-21/lib \
  -cp out/production/swimworkoutbuilder_javafx:/path/to/javafx-sdk-21/lib/* \
  swimworkoutbuilder_javafx.Main


⸻

Quick Start (User Flow)
	1.	Create a swimmer
	•	Click New Swimmer, enter name/team.
	•	In Seed Times, click the pencil to edit. Enter seconds (75) or mm:ss.hh (1:15, 1:15.20). Leave blank to clear.
	•	Switch swimmers via the Swimmer combo in the top bar.
	2.	Create a workout
	•	Click New Workout.
	•	In Current Workout, edit name/notes if you like (pencil).
	•	Choose Pool length (25y/25m/50m).
	3.	Add groups & sets
	•	Click + Add Group → give it a name (e.g., “Warmup”).
	•	In the group row, click + Set → fill reps × distance, stroke, effort, equipment, and notes.
	•	The dialog shows live Goal and Interval suggestions based on seed times and inputs.
	4.	Save
	•	When the “Unsaved” chip appears, click Save in the workout header.
	•	Totals update and the chip disappears.

⸻

Data & Persistence
	•	Files are stored under ~/.swimworkoutbuilder:
	•	swimmers/ and workouts/ subfolders contain binary records
	•	last.properties keeps “last opened” pointers

Developer utility

A tiny CLI tool backs up and clears local data:
	•	Run (IntelliJ run config or CLI):
	•	Main class: swimworkoutbuilder_javafx.dev.DevTools
	•	It creates a zip backup in ~/.swimworkoutbuilder_backups/ and then deletes ~/.swimworkoutbuilder.

⸻

Project Layout (selected)
swimworkoutbuilder_javafx/
  Main.java
  state/
    AppState.java           # single source of truth (swimmer/workout lists, selection)
    LocalStore.java         # file persistence
  model/
    Swimmer.java
    Workout.java
    SetGroup.java
    SwimSet.java
    enums/ (Course, StrokeType, Effort, Equipment)
    units/ (TimeSpan, Distance)
    pacing/
      PacePolicy.java
      DefaultPacePolicy.java
  ui/
    ActionBar.java
    Theme.java
    Icons.java
    swimmers/
      SwimmerPane.java      # swimmer card + seed-time grid
    workout/
      WorkoutPane.java      # scroll container (header fixed, groups/sets scroll)
      WorkoutHeaderPane.java
      WorkoutBuilderPane.java
      WorkoutBuilderPresenter.java
      SetFormDialog.java
      SetGroupFormDialog.java
  resources/
    styles-ocean-depth.css
    images/ (equipment PNGs, icons)
  dev/
    DevTools.java


⸻

Time Formats (MVP)
	•	Input accepts:
	•	seconds (e.g., 75)
	•	mm:ss (e.g., 1:15)
	•	mm:ss.hh (e.g., 1:15.20)
	•	Display in the UI is mm:ss (no hundredths).

⸻

Known Limitations (MVP cut)
	•	No printing/export yet (button is placeholder).
	•	Dialogs center on the main screen by default; multi-monitor “owner” parenting is a TODO (attach dialogs to the main window’s stage for better positioning).
	•	Policy math is basic; future work could add per-distance curves and personal best imports.

⸻

Troubleshooting
	•	JavaFX path errors: confirm the --module-path points to <javafx-sdk-21>/lib and --add-modules includes javafx.controls,javafx.fxml.
	•	“A bound value cannot be set.”: if you see this in custom builds, it usually means trying to set a property that is currently bound; in the app we bind managed to visible and only set visible.
	•	Missing equipment icons: make sure resources/images/*.png are on the runtime classpath.

⸻

Screenshots

Add your demo screenshots here:
	•	docs/screens/main.png
	•	docs/screens/set-dialog.png
	•	docs/screens/scrolling-groups.png

(Reference them with ![caption](docs/screens/main.png).)

⸻

License

For academic use as part of coursework. Third-party assets:
	•	JavaFX (GPL+CE)
	•	Icon PNGs: included under project assets for demo purposes.

⸻

Author

Parker Blackwell — Fall 2025 — SwimWorkoutBuilder (JavaFX-only MVP)

## 🎥 Demo Video

Watch the full demo of **SwimWorkoutBuilder (JavaFX)** in action:

👉 [View Demo on iCloud Drive](https://www.icloud.com/iclouddrive/015z7KKiwqrkLvAD754ECe8Ow#SwimWorkoutBuilder_Demo)
