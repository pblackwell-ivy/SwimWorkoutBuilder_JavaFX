# Architecture

## High-Level
The app is a **JavaFX desktop application** with:
- A **global AppState** (current swimmer, active workout, collections).
- A **LocalStore** layer for persistence (save/load swimmers, workouts).
- **UI Panes** for the main screen: **SwimmerPane**, **WorkoutPane**, **BuilderPane**. Dialogs provide focused editing UX.
- **CSS theme** `styles-ocean-depth.css` enforcing consistent button roles, surfaces, spacing.

## Key Design Decisions
- **Single source of truth:** `AppState` manages currently selected entities; UI observes state.
- **Declarative styling:** role classes (`.btn-primary`, `.btn-danger`, `.surface`, `.card`) rather than inline styling.
- **Scrollable builder:** Workout construction occurs in a scrollable center pane; header stays tidy and consistent.
- **Local persistence:** Human-readable store for easy backups and portability.
- **Unit model:** Store canonical distances; UI presents **course unit** (SCY/SCM/LCM) and (post-MVP) **laps-based conversion**.
- **Dialog-based edits:** Consistent modals for swimmer, seeds, set groups, and sets.

## Primary UI Flow
1. **SwimmerPane** → Create/edit swimmer & seed times.
2. **WorkoutPane** → See workout header & actions.
3. **BuilderPane** → Add/Edit **SetGroups**; for each, add **SwimSets** with stroke, distance, effort, equipment, notes.

## Theme
Active theme: `styles-ocean-depth.css`. It defines:
- Role-based buttons (primary, neutral, warn, danger).
- `.surface`/`.card` containers with measured paddings and rounded corners.
- Spacing rules consistent with MVP screens.

## Packaging Approach (Overview)
- **Fat JAR** with JavaFX runtime; or
- **jlink + jpackage** for native installers (DMG/PKG for macOS, MSI/EXE for Windows).
See `PACKAGING_NOTES.md` for exact commands.
