# Files Overview

This inventory is based on the **main branch (MVP)** you provided.

## Java Sources
- **ActionBar** — *General*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/shell/ActionBar.java`  
  Package: `swimworkoutbuilder_javafx.ui.shell`  
  Kind: `class`  
  Notes: Top application toolbar.  <p>Responsibilities: <ul>   <li>Swimmer selection (combo) and swimmer quick actions.</li>   <li>Workout quick actions (new/open/print).</li>   <li>Bind enable/disable state to {@link AppState} (idempotent wiring).</li> </ul>  <p>Styling: relies on global theme classes: <pre>   .toolbar .button.primary / .secondary / .ghost / .accent / .sm </pre>  <p>No business logic lives here—this is purely a shell/launcher for dialogs and state changes.</p>  @since 1.0
- **AppState** — *Global App State / Singleton*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/state/AppState.java`  
  Package: `swimworkoutbuilder_javafx.state`  
  Kind: `class`  
  Notes: Global observable state for the application (MVVM-ish “store”).  <p>Holds the list of known swimmers and the currently-selected swimmer and workout. Views bind to these properties; presenters update them in response to user actions.</p>  <p><b>Design notes</b></p> <ul>   <li>Singleton with simple observable properties (no frameworks).</li>   <li>When the current swimmer changes, an existing workout that       belongs to a different swimmer is cleared to avoid edits       against the wrong swimme…
- **Course** — *Domain Model*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/enums/Course.java`  
  Package: `swimworkoutbuilder_javafx.model.enums`  
  Kind: `enum`  
  Notes: Course enumerates the three most common competition pool lengths in the world and their impact on goal and rest time calculations.  This enum is used to define the swimming course length for a workout and is an essential UI/logic component of the app.  Future capabilities will include user defined custom course lengths to accomodate any swimming course length.  SCY = Short Course Yards (25 yards) SCM = Short Course Meters (25 meters) LCM = Long Course Meters (50 meters)  v2: added performance mu…
- **CourseUnit** — *Domain Model*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/enums/CourseUnit.java`  
  Package: `swimworkoutbuilder_javafx.model.enums`  
  Kind: `enum`  
  Notes: CourseUnit = Pool length unit of measure
- **DateFmt** — *General*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/DateFmt.java`  
  Package: `swimworkoutbuilder_javafx.ui`  
  Kind: `class`  
  Notes: Utility class for consistent local date/time formatting in the UI.
- **DefaultPacePolicy** — *Domain Model*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/pacing/DefaultPacePolicy.java`  
  Package: `swimworkoutbuilder_javafx.model.pacing`  
  Kind: `class`  
  Notes: Multiplier-based MVP policy for computing goal, interval, and rest.
- **DevTools** — *General*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/dev/DevTools.java`  
  Package: `swimworkoutbuilder_javafx.dev`  
  Kind: `class`  
  Notes: [UI Component] DevTools for the "swimworkoutbuilder_javafx" feature.  <p><b>Responsibilities:</b> <ul>   <li>Render nodes and bind to observable state</li>   <li>Expose minimal API for host containers</li>   <li>Integrate canonical button roles and theming</li> </ul>  <p><b>Design Notes:</b> <ul>   <li>Encapsulate layout and styling concerns</li>   <li>Prefer composition over inheritance</li>   <li>Avoid side effects; pure UI behavior</li> </ul>  <p><b>Usage Example:</b> <pre>{@code // Typical u…
- **DialogUtil** — *Dialog / Modal UI (JavaFX)*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/common/DialogUtil.java`  
  Package: `swimworkoutbuilder_javafx.ui.common`  
  Kind: `class`  
  Notes: Small helpers to make dialogs center & not full-screen.
- **DistanceFactors** — *Domain Model*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/enums/DistanceFactors.java`  
  Package: `swimworkoutbuilder_javafx.model.enums`  
  Kind: `enum`  
  Notes: DistanceFactors provides multipliers to adjust target pace depending on repeat distance. Short reps are faster than seed times, longer reps trend slower.  Distances are stored as exact Distance values (canonical 0.0001 m units). Multipliers are applied during pace calculations.  To do: add a fallback calculation for distances outside the buckets.
- **Effort** — *Domain Model*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/enums/Effort.java`  
  Package: `swimworkoutbuilder_javafx.model.enums`  
  Kind: `enum`  
  Notes: Effort levels are essential for creating goal-oriented structured workouts and are commonly used by top coaches.  The enumerated values represent the most common swimming efforts around the world.  v1: enum name + long description v2: added label and short description for UI v3: added paceMultiplier (scales seed /100) and restAllowanceSec (legacy interval hook)  Notes in v4:  • paceMultiplier is still included for legacy/future experimentation, but DefaultPacePolicy    primarily drives rest/inte…
- **Equipment** — *Domain Model*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/enums/Equipment.java`  
  Package: `swimworkoutbuilder_javafx.model.enums`  
  Kind: `enum`  
  Notes: Equipment factors adjust target pace calculations to account for training aids. Multipliers are applied as part of the pace policy, and multiple pieces of equipment can be combined (multipliers multiplied together).  Typical defaults (tunable):  - FINS:       0.88 (faster, strong kick propulsion)  - PADDLES:    0.96 (slightly faster, more pull power)  - PULL_BUOY:  1.05 (slower overall pace, less kick drive)  - SNORKEL:    0.99 (neutral to slightly slower)  - DRAG_SOCKS: 1.15 (slower due to resi…
- **FilesUtil** — *Persistence / Storage*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/store/FilesUtil.java`  
  Package: `swimworkoutbuilder_javafx.store`  
  Kind: `class`  
  Notes: [UI Component] FilesUtil for the "swimworkoutbuilder_javafx" feature.  <p><b>Responsibilities:</b> <ul>   <li>Render nodes and bind to observable state</li>   <li>Expose minimal API for host containers</li>   <li>Integrate canonical button roles and theming</li> </ul>  <p><b>Design Notes:</b> <ul>   <li>Encapsulate layout and styling concerns</li>   <li>Prefer composition over inheritance</li>   <li>Avoid side effects; pure UI behavior</li> </ul>  <p><b>Usage Example:</b> <pre>{@code // Typical …
- **Icons** — *General*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/Icons.java`  
  Package: `swimworkoutbuilder_javafx.ui`  
  Kind: `class`  
  Notes: Utility for loading and sizing icons consistently.  Example:     Button edit = new Button();     edit.setGraphic(Icons.make("pencil", 16));
- **LoadWorkoutDialog** — *Dialog / Modal UI (JavaFX)*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/LoadWorkoutDialog.java`  
  Package: `swimworkoutbuilder_javafx.ui.workout`  
  Kind: `class`  
  Notes: Dialog that presents the list of workouts for the selected / current swimmer.  <p><b>Responsibilities:</b> <ul>   <li>Verify that a swimmer has been selected</li>   <li>Display available workouts (name, notes) from LocalStore</li>   <li>Enables user to open a workout or cancel</li>   <li>Return a result to the caller</li> </ul>  @author Parker Blackwell @version 1.0 @since 2025-10-14
- **LocalStore** — *Persistence / Storage*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/store/LocalStore.java`  
  Package: `swimworkoutbuilder_javafx.store`  
  Kind: `class`  
  Notes: [UI Component] LocalStore for the "swimworkoutbuilder_javafx" feature.  <p><b>Responsibilities:</b> <ul>   <li>Render nodes and bind to observable state</li>   <li>Expose minimal API for host containers</li>   <li>Integrate canonical button roles and theming</li> </ul>  <p><b>Design Notes:</b> <ul>   <li>Encapsulate layout and styling concerns</li>   <li>Prefer composition over inheritance</li>   <li>Avoid side effects; pure UI behavior</li> </ul>  <p><b>Usage Example:</b> <pre>{@code // Typical…
- **Main** — *General*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/Main.java`  
  Package: `swimworkoutbuilder_javafx`  
  Kind: `class`  
  Notes: Entry point for the SwimWorkoutBuilder JavaFX application.  <p>This class initializes global state ({@link AppState}), loads saved swimmers from disk, restores the last selected swimmer if available, and sets up the main UI layout via {@link MainView}.</p>  <h2>Responsibilities</h2> <ul>   <li>Initialize JavaFX application stage and scene.</li>   <li>Load swimmers from {@link LocalStore} and populate {@link AppState}.</li>   <li>Ensure graceful recovery if no swimmers can be loaded.</li>   <li>A…
- **MainView** — *General*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/MainView.java`  
  Package: `swimworkoutbuilder_javafx.ui`  
  Kind: `class`  
  Notes: MainView for the "swimworkoutbuilder_javafx" feature that hosts the main application layout.  <p><b>Responsibilities:</b> <ul>   <li>Render nodes and bind to observable state</li>   <li>Expose minimal API for host containers</li>   <li>Integrate canonical button roles and theming</li> </ul>  <p><b>Design Notes:</b> <ul>   <li>Encapsulate layout and styling concerns</li>   <li>Prefer composition over inheritance</li>   <li>Avoid side effects; pure UI behavior</li> </ul>   @author Parker Blackwell…
- **ManageSwimmersDialog** — *Dialog / Modal UI (JavaFX)*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/swimmers/ManageSwimmersDialog.java`  
  Package: `swimworkoutbuilder_javafx.ui.swimmers`  
  Kind: `class`  
  Notes: [Dialog] ManageSwimmersDialog for the "dialogs" feature.  <p><b>Responsibilities:</b> <ul>   <li>Collect user input with clear primary/secondary actions</li>   <li>Validate inputs and surface errors accessibly</li>   <li>Return a result to the caller</li> </ul>  <p><b>Design Notes:</b> <ul>   <li>Follows canonical roles (primary/secondary/tertiary/destructive)</li>   <li>ESC/Enter keys match platform expectations</li>   <li>Validation separated from presentation</li> </ul>  <p><b>Usage Example:<…
- **Mode** — *General*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/swimmers/SwimmerPresenter.java`  
  Package: `swimworkoutbuilder_javafx.ui.swimmers`  
  Kind: `enum`  
  Notes: Presenter for creating/editing swimmers.  <p>Owns the UI state (fields, mode flags) and updates the in-memory {@link AppState}. Persistence (save) is delegated to {@link LocalStore}. This class is UI-toolkit agnostic besides JavaFX properties.</p>
- **PacePolicy** — *Domain Model*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/pacing/PacePolicy.java`  
  Package: `swimworkoutbuilder_javafx.model.pacing`  
  Kind: `interface`  
  Notes: Strategy interface for turning sets + seeds into concrete timing.  Policies define how goal times, rests, and intervals are computed from:  • the workout context (course, modifiers),  • the swimmer's seed pace,  • and the set definition (stroke, reps, distance, effort, equipment).
- **PreviewPane** — *UI Pane / Controller (JavaFX)*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/preview/PreviewPane.java`  
  Package: `swimworkoutbuilder_javafx.ui.preview`  
  Kind: `class`  
  Notes: Read-only preview of the current workout using WorkoutPrinter output. Default ctor uses AppState.get() and DefaultPacePolicy so Main.java stays unchanged.
- **PreviewPresenter** — *General*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/preview/PreviewPresenter.java`  
  Package: `swimworkoutbuilder_javafx.ui.preview`  
  Kind: `class`  
  Notes: Presenter for the read-only Workout preview/print text. Captures WorkoutPrinter's console output into a String for UI display.
- **RepositoryManager** — *Persistence / Storage*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/dev/RepositoryManager.java`  
  Package: `swimworkoutbuilder_javafx.dev`  
  Kind: `class`  
  Notes: Utility class for managing and resetting local repository data during development.  <p>This helper provides safe ways to delete or back up all serialized data files (e.g., swimmers.dat, workouts.dat) stored in the application's data directory. It is intended strictly for use during development and testing — not for production builds.</p>  <p><b>Default storage location:</b> <code>~/.swimworkoutbuilder/</code></p>  <p><b>Example usage:</b></p> <pre>{@code // Back up and clear all saved data Repos…
- **SeedFormDialog** — *Dialog / Modal UI (JavaFX)*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/seeds/SeedFormDialog.java`  
  Package: `swimworkoutbuilder_javafx.ui.seeds`  
  Kind: `class`  
  Notes: [Dialog] SeedFormDialog for the "dialogs" feature.  <p><b>Responsibilities:</b> <ul>   <li>Collect user input with clear primary/secondary actions</li>   <li>Validate inputs and surface errors accessibly</li>   <li>Return a result to the caller</li> </ul>  <p><b>Design Notes:</b> <ul>   <li>Follows canonical roles (primary/secondary/tertiary/destructive)</li>   <li>ESC/Enter keys match platform expectations</li>   <li>Validation separated from presentation</li> </ul>  <p><b>Usage Example:</b> <p…
- **SeedGridPane** — *UI Pane / Controller (JavaFX)*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/seeds/SeedGridPane.java`  
  Package: `swimworkoutbuilder_javafx.ui.seeds`  
  Kind: `class`  
  Notes: SeedGridPane — compact card showing/editing 100-distance seed times by stroke. View mode shows an edit icon; edit mode shows cancel/save icons. Buttons: ✎ (edit), ↩ (cancel), 💾 (save)
- **SeedPace** — *Domain Model*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/pacing/SeedPace.java`  
  Package: `swimworkoutbuilder_javafx.model.pacing`  
  Kind: `class`  
  Notes: Represents a swimmer’s *baseline pace** (seed time) for a specific stroke and distance.  <p>A {@code SeedPace} defines how fast a swimmer can complete a known distance, serving as the foundation for all pace, goal, and interval calculations in the app. It captures both the measured distance and the time required, and derives the canonical swim speed in meters per second (m/s) for cross-course computations.</p>  <h2>Responsibilities</h2> <ul>   <li>Store the original test distance as a {@link swi…
- **SeedTimesPresenter** — *General*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/seeds/SeedTimesPresenter.java`  
  Package: `swimworkoutbuilder_javafx.ui.seeds`  
  Kind: `class`  
  Notes: Presenter for the Seed Times pane. Manages editing state and Save/Cancel enablement.
- **SetFormDialog** — *Dialog / Modal UI (JavaFX)*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/SetFormDialog.java`  
  Package: `swimworkoutbuilder_javafx.ui.workout`  
  Kind: `class`  
  Notes: Add/Edit Set dialog (MVP). Creates or edits a single {@link SwimSet}. - Shows live Goal/Interval suggestions (policy math stays elsewhere). - Lets the user toggle equipment (purely presentational icons here). - Returns the set on OK or empty on cancel.
- **SetFormPresenter** — *General*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/SetFormPresenter.java`  
  Package: `swimworkoutbuilder_javafx.ui.workout`  
  Kind: `class`  
  Notes: Presenter for the "Edit Set" dialog: holds user inputs and exposes calculated Interval (@) and Goal time using the current PacePolicy. The dialog (view) should bind controls to these properties and remain dumb.
- **SetGroup** — *Domain Model*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/SetGroup.java`  
  Package: `swimworkoutbuilder_javafx.model`  
  Kind: `class`  
  Notes: Structured workouts are made up of one or more {@code SetGroup}s that contain one or more {@code SwimSet}s. SetGroups are ordered and may be repeated. Supports the logic for defining rests between the sets in a group and between the groups themselves.  A SetGroup defines the name of the group (e.g., Warmup, Main, Cooldown), the number of repetitions, the order relative to other groups, and may include an optional "notes" that typically are used for contextual information.
- **SetGroupFormDialog** — *Dialog / Modal UI (JavaFX)*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/SetGroupFormDialog.java`  
  Package: `swimworkoutbuilder_javafx.ui.workout`  
  Kind: `class`  
  Notes: package swimworkoutbuilder_javafx.ui.workout; import javafx.geometry.Insets; import javafx.geometry.Pos; import javafx.scene.Scene; import javafx.scene.control.*;
- **StrokeType** — *Domain Model*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/enums/StrokeType.java`  
  Package: `swimworkoutbuilder_javafx.model.enums`  
  Kind: `enum`  
  Notes: StrokeType is a UI/logic component of the app and is an enumeration of supported swimming stroke types. Using this enum enables the addition of more stroke types in the future.  <p>Each stroke has:</p> <ul>   <li>a canonical enum constant</li>   <li>a user-friendly full label</li>   <li>a short label (common shorthand for UI)</li>   <li>optional aliases for parsing input</li> </ul>  <p>This allows flexible parsing of user input (e.g. "Free", "Fr", "Fly", or "IM") without losing the canonical for…
- **Swimmer** — *Domain Model*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/Swimmer.java`  
  Package: `swimworkoutbuilder_javafx.model`  
  Kind: `class`  
  Notes: Represents an individual swimmer, including personal information, team affiliation, and baseline seed times used for workout pacing.  <p>This class serves as a core domain model within the SwimWorkoutBuilder application. It stores identifying information and per-stroke performance benchmarks ({@link swimworkoutbuilder_javafx.model.pacing.SeedPace}) that other components use to calculate goal times, intervals, and pacing plans.</p>  <p><b>Key features:</b> <ul>   <li>Stores personal and team deta…
- **SwimmerCard** — *General*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/swimmers/SwimmerCard.java`  
  Package: `swimworkoutbuilder_javafx.ui.swimmers`  
  Kind: `class`  
  Notes: SwimmerCard — shows/edit the current swimmer. Contains the form (First, Last, Team, Created, Updated) and the action row. Self-contained UI + behavior. Host calls node() to embed.
- **SwimmerFormDialog** — *Dialog / Modal UI (JavaFX)*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/swimmers/SwimmerFormDialog.java`  
  Package: `swimworkoutbuilder_javafx.ui.swimmers`  
  Kind: `class`  
  Notes: [Dialog] SwimmerFormDialog for the "dialogs" feature.  <p><b>Responsibilities:</b> <ul>   <li>Collect user input with clear primary/secondary actions</li>   <li>Validate inputs and surface errors accessibly</li>   <li>Return a result to the caller</li> </ul>  <p><b>Design Notes:</b> <ul>   <li>Follows canonical roles (primary/secondary/tertiary/destructive)</li>   <li>ESC/Enter keys match platform expectations</li>   <li>Validation separated from presentation</li> </ul>  <p><b>Usage Example:</b>…
- **SwimmerPane** — *UI Pane / Controller (JavaFX)*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/swimmers/SwimmerPane.java`  
  Package: `swimworkoutbuilder_javafx.ui.swimmers`  
  Kind: `class`  
  Notes: {@code SwimmerPane} composes the full left column of the main application view. It displays the current swimmer’s information and seed times within a scrollable layout.  <p>Structure:</p> <ul>   <li>{@link SwimmerCard} — shows and edits swimmer details (name, team, timestamps)</li>   <li>{@link SeedGridPane} — displays and edits the swimmer’s seed times</li> </ul>  <p>The {@link ScrollPane} is the root node, ensuring the entire column scrolls vertically when content exceeds the available space. …
- **SwimmerRepository** — *Domain Model*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/repository/SwimmerRepository.java`  
  Package: `swimworkoutbuilder_javafx.model.repository`  
  Kind: `class`  
  Notes: @deprecated Legacy persistence prototype. <p> This class was part of an early experiment for saving and loading swimmers to a CSV file before {@link swimworkoutbuilder_javafx.store.LocalStore} was implemented. It is no longer referenced anywhere in the application. <p> Retained for documentation and grading purposes only.
- **SwimSet** — *Domain Model*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/SwimSet.java`  
  Package: `swimworkoutbuilder_javafx.model`  
  Kind: `class`  
  Notes: One swim training set (leaf of the workout tree), e.g. “8×50 Free @ Threshold”.  <p>Holds the core attributes needed for pacing and display: stroke, reps, per-rep distance (canonical via {@link Distance}), effort, course, equipment, and optional per-rep goal/interval times.</p>  <p><b>Key features:</b> <ul>   <li>Distance is snapped up to a legal multiple of the pool length for the {@link Course}.</li>   <li>Mutable model with validated setters; safe defaults in the no-arg ctor.</li>   <li>Deep-…
- **Theme** — *General*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/Theme.java`  
  Package: `swimworkoutbuilder_javafx.ui`  
  Kind: `class`  
  Notes: Utility for applying the global SwimWorkoutBuilder CSS theme to any Scene. Initially used in Main.java, but now also used in dialogs and secondary stages that don't inherit the main stylesheet.
- **TimeSpan** — *Domain Model*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/units/TimeSpan.java`  
  Package: `swimworkoutbuilder_javafx.model.units`  
  Kind: `class`  
  Notes: Represents an immutable span of time stored in *milliseconds**.  <p>{@code TimeSpan} is a lightweight utility class for expressing durations (e.g., 1:23.45) in a canonical, integer-based format. It provides arithmetic helpers, conversion utilities, and consistent string formatting for display in a human-readable form.</p>  <h2>Design Notes</h2> <ul>   <li>Internally stores time as a {@code long} number of milliseconds for exact arithmetic.</li>   <li>Immutable — all operations return new {@code …
- **UiUtil** — *Utilities / Helpers*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/UiUtil.java`  
  Package: `swimworkoutbuilder_javafx.ui`  
  Kind: `class`  
  Notes: [UI Component] UiUtil for the "swimworkoutbuilder_javafx" feature.  <p><b>Responsibilities:</b> <ul>   <li>Render nodes and bind to observable state</li>   <li>Expose minimal API for host containers</li>   <li>Integrate canonical button roles and theming</li> </ul>  <p><b>Design Notes:</b> <ul>   <li>Encapsulate layout and styling concerns</li>   <li>Prefer composition over inheritance</li>   <li>Avoid side effects; pure UI behavior</li> </ul>  <p><b>Usage Example:</b> <pre>{@code // Typical usa…
- **Unit** — *Domain Model*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/units/Distance.java`  
  Package: `swimworkoutbuilder_javafx.model.units`  
  Kind: `enum`  
  Notes: Represents an immutable swimming distance with canonical storage in meters using fixed-point precision of 0.0001 meters (0.1 mm).  <p>The {@code Distance} class uses a canonical internal representation: all distances are stored as integer counts of 1/10,000 of a meter, regardless of how they were originally entered or displayed. This design simplifies the model, ensures unit consistency, and prevents rounding drift when converting between yards and meters.</p>  <p>Conversion follows the exact fa…
- **Workout** — *Domain Model*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/Workout.java`  
  Package: `swimworkoutbuilder_javafx.model`  
  Kind: `class`  
  Notes: Represents a structured swim workout for a specific swimmer.  <p>{@code Workout} acts as a top-level container for all workout content — including metadata (name, notes, course) and a sequence of {@link SetGroup}s. It does not perform any pace or timing logic; those calculations are handled by the {@link swimworkoutbuilder_javafx.model.pacing.PacePolicy} implementations.</p>  <h2>Responsibilities</h2> <ul>   <li>Encapsulate all metadata describing a workout (name, course, notes, swimmer ID).</li…
- **WorkoutBuilderPane** — *UI Pane / Controller (JavaFX)*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/WorkoutBuilderPane.java`  
  Package: `swimworkoutbuilder_javafx.ui.workout`  
  Kind: `class`  
  Notes: Central “Workout Builder” pane. Displays workout groups and their sets, allowing add/edit/delete operations. Pure view layer; all logic is delegated to {@link WorkoutBuilderPresenter}.
- **WorkoutBuilderPresenter** — *General*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/WorkoutBuilderPresenter.java`  
  Package: `swimworkoutbuilder_javafx.ui.workout`  
  Kind: `class`  
  Notes: [Presenter] WorkoutBuilderPresenter for the "workout" feature.  <p><b>Responsibilities:</b> <ul>   <li>Expose observable state to the view</li>   <li>Handle user intents and orchestrate updates</li>   <li>Coordinate with stores/services for data operations</li> </ul>  <p><b>Design Notes:</b> <ul>   <li>Lightweight MVP (Presenter + Pane)</li>   <li>No blocking work on FX thread; delegate background tasks</li>   <li>Business logic lives in services/stores</li> </ul>  <p><b>Usage Example:</b> <pre>…
- **WorkoutEditorState** — *General*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/WorkoutEditorState.java`  
  Package: `swimworkoutbuilder_javafx.ui.workout`  
  Kind: `class`  
  Notes: Minimal editing state holder for a Workout.  <p>Provides a staged copy for in-progress edits and tracks a dirty flag. Call {@link #begin(Workout)} to start, mutate the staged model via the presenter, then {@link #commit()} to copy staged -> original or {@link #cancel()} to discard.</p>  <p>Deliberately persistence-agnostic: saving to disk is the presenter's job.</p>
- **WorkoutFormDialog** — *Dialog / Modal UI (JavaFX)*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/WorkoutFormDialog.java`  
  Package: `swimworkoutbuilder_javafx.ui.workout`  
  Kind: `class`  
  Notes: Create or edit a Workout (name, course, notes). Reps belong to SetGroup, so the Workout dialog intentionally does NOT include reps.
- **WorkoutHeaderPane** — *UI Pane / Controller (JavaFX)*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/WorkoutHeaderPane.java`  
  Package: `swimworkoutbuilder_javafx.ui.workout`  
  Kind: `class`  
  Notes: WorkoutHeaderPane  Visual + interaction model for the workout header area.  Layout philosophy: - Row A (global bar):  "Current Workout"  [Unsaved chip]   …………………  [Save] [Delete]   • Save/Delete here apply to the ENTIRE workout (header + groups/sets).   • The Unsaved chip reflects the presenter's dirty flag. - Row B: Name + header edit actions (Edit/Cancel/Save) — these affect ONLY name/notes/course.   • Header Save applies edits to the model and marks the workout dirty, but does not persist. - …
- **WorkoutPane** — *UI Pane / Controller (JavaFX)*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/WorkoutPane.java`  
  Package: `swimworkoutbuilder_javafx.ui.workout`  
  Kind: `class`  
  Notes: Scrollable center column that hosts the workout header + builder. Mirrors SwimmerPane's structure; only extra responsibility is creating a shared WorkoutBuilderPresenter so header/builder stay in sync.
- **WorkoutPersistenceProbe** — *Persistence / Storage*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/dev/WorkoutPersistenceProbe.java`  
  Package: `swimworkoutbuilder_javafx.dev`  
  Kind: `class`  
  Notes: [UI Component] WorkoutPersistenceProbe for the "dev" feature.  <p><b>Responsibilities:</b> <ul>   <li>Render nodes and bind to observable state</li>   <li>Expose minimal API for host containers</li>   <li>Integrate canonical button roles and theming</li> </ul>  <p><b>Design Notes:</b> <ul>   <li>Encapsulate layout and styling concerns</li>   <li>Prefer composition over inheritance</li>   <li>Avoid side effects; pure UI behavior</li> </ul>  <p><b>Usage Example:</b> <pre>{@code // Typical usage fo…
- **WorkoutPresenter** — *General*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/WorkoutPresenter.java`  
  Package: `swimworkoutbuilder_javafx.ui.workout`  
  Kind: `class`  
  Notes: Presenter for the central Workout builder area. Keeps header and list of SetGroups in sync with the current Workout.
- **WorkoutPrinter** — *Domain Model*  
  Path: `SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/utils/WorkoutPrinter.java`  
  Package: `swimworkoutbuilder_javafx.model.utils`  
  Kind: `class`  
  Notes: [UI Component] WorkoutPrinter for the "utils" feature.  <p><b>Responsibilities:</b> <ul>   <li>Render nodes and bind to observable state</li>   <li>Expose minimal API for host containers</li>   <li>Integrate canonical button roles and theming</li> </ul>  <p><b>Design Notes:</b> <ul>   <li>Encapsulate layout and styling concerns</li>   <li>Prefer composition over inheritance</li>   <li>Avoid side effects; pure UI behavior</li> </ul>  <p><b>Usage Example:</b> <pre>{@code // Typical usage for Worko…

## FXML Layouts
_No FXML files detected in archive._

## CSS
- `SwimWorkoutBuilder_JavaFX-main/src/resources/ui/styles-ocean-depth.css` — notes: =========================================    SwimWorkoutBuilder – Theme: Ocean Depth    =========================================

## Project Tree (selected file types)
```
SwimWorkoutBuilder_JavaFX-main/src/resources/icons/circle-x-swim-text.png
SwimWorkoutBuilder_JavaFX-main/src/resources/icons/move-down-swim-text.png
SwimWorkoutBuilder_JavaFX-main/src/resources/icons/move-up-swim-text.png
SwimWorkoutBuilder_JavaFX-main/src/resources/icons/pencil-swim-text.png
SwimWorkoutBuilder_JavaFX-main/src/resources/icons/save-swim-text.png
SwimWorkoutBuilder_JavaFX-main/src/resources/icons/save-white.png
SwimWorkoutBuilder_JavaFX-main/src/resources/icons/square-pen-swim-text.png
SwimWorkoutBuilder_JavaFX-main/src/resources/icons/square-pen.png
SwimWorkoutBuilder_JavaFX-main/src/resources/icons/square-pen.svg
SwimWorkoutBuilder_JavaFX-main/src/resources/icons/trash-2-danger.png
SwimWorkoutBuilder_JavaFX-main/src/resources/icons/trash-2-swim-text.png
SwimWorkoutBuilder_JavaFX-main/src/resources/icons/x-swim-text.png
SwimWorkoutBuilder_JavaFX-main/src/resources/images/Drag_Socks.png
SwimWorkoutBuilder_JavaFX-main/src/resources/images/fins.png
SwimWorkoutBuilder_JavaFX-main/src/resources/images/kick_board.png
SwimWorkoutBuilder_JavaFX-main/src/resources/images/paddles.png
SwimWorkoutBuilder_JavaFX-main/src/resources/images/parachute.png
SwimWorkoutBuilder_JavaFX-main/src/resources/images/pull_buoy.png
SwimWorkoutBuilder_JavaFX-main/src/resources/images/snorkel.png
SwimWorkoutBuilder_JavaFX-main/src/resources/ui/styles-ocean-depth.css
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/dev/DevTools.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/dev/RepositoryManager.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/dev/Swim_Workout_Builder_MVP-SwimWorkoutBuilder — Class Diagram (MVP).png
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/dev/WorkoutPersistenceProbe.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/Main.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/enums/Course.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/enums/CourseUnit.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/enums/DistanceFactors.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/enums/Effort.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/enums/Equipment.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/enums/StrokeType.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/pacing/DefaultPacePolicy.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/pacing/PacePolicy.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/pacing/SeedPace.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/repository/SwimmerRepository.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/SetGroup.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/Swimmer.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/SwimSet.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/units/Distance.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/units/TimeSpan.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/utils/WorkoutPrinter.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/model/Workout.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/state/AppState.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/store/FilesUtil.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/store/LocalStore.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/common/DialogUtil.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/DateFmt.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/Icons.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/MainView.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/preview/PreviewPane.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/preview/PreviewPresenter.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/seeds/SeedFormDialog.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/seeds/SeedGridPane.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/seeds/SeedTimesPresenter.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/shell/ActionBar.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/swimmers/ManageSwimmersDialog.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/swimmers/SwimmerCard.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/swimmers/SwimmerFormDialog.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/swimmers/SwimmerPane.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/swimmers/SwimmerPresenter.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/Theme.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/UiUtil.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/LoadWorkoutDialog.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/SetFormDialog.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/SetFormPresenter.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/SetGroupFormDialog.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/WorkoutBuilderPane.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/WorkoutBuilderPresenter.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/WorkoutEditorState.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/WorkoutFormDialog.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/WorkoutHeaderPane.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/WorkoutPane.java
SwimWorkoutBuilder_JavaFX-main/src/swimworkoutbuilder_javafx/ui/workout/WorkoutPresenter.java
SwimWorkoutBuilder_JavaFX-main/Swim_Workout_Builder_MVP-SwimWorkoutBuilder — Class Diagram (MVP).png
```
