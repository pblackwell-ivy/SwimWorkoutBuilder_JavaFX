# SwimWorkoutBuilder — Post-MVP Documentation

**Version:** Main branch (last MVP commit)  
**Theme:** `styles-ocean-depth.css` (active)

## Purpose
SwimWorkoutBuilder helps Masters swimmers and coaches **design, edit, and persist structured swim workouts** with seed-based pacing cues, equipment flags, and organized set groups (Warmup, Drill, Main, Cooldown). The application targets **clarity, repeatability, and efficiency** for daily training.

## Target Users
- **Self-coached Masters swimmers** who want quick, structured workouts with seed-relative pacing.
- **Coaches** building or tweaking practice plans and printing/exporting workouts.
- **Athletes returning from injury** who benefit from clear effort targets and equipment toggles.

## Core Use Cases
- Create a **Swimmer** and set **seed times** (per-stroke, per-course).
- Build a **Workout** with **Set Groups** and **Swim Sets**, including stroke, distance, effort, notes.
- Persist and reload workouts through **LocalStore**.
- Edit in-place via **dialogs** for swimmer info, seed times, set group properties, and swim set details.
- Apply **equipment** (fins, pull buoy, paddles, snorkel, etc.) with consistent icons and persistence.
- Show **pacing / interval summaries** derived from seed times.

## Feature Summary (as of MVP)
- Swimmer CRUD; Seed editing and summary display.
- Workout builder: Groups (reps, rest-after), Sets (stroke, distance, effort, notes).
- Equipment persistence per set (icons aligned to UI theme).
- Save / delete logic with **LocalStore** (JSON-like or file-based storage).
- UI: **SwimmerPane**, **WorkoutPane** (+ **BuilderPane**), consistent header, role-based buttons.
- Theme: **`styles-ocean-depth.css`** with role-driven button styles and `.surface/.card` layout.
- Scrollable builder area; consistent spacing, margins, and header layout.

## Post-MVP Focus
- **Bug cleanup/stability** and **UI consistency refinements**.
- **Meters / yards conversions by laps** and display rules.
- **Drag-and-drop workout editing** (reorder groups/sets).
- **Richer dialogs** to customize effort, distance, equipment, stroke factors.
- Optional: Dialogs polish, performance tuning, **packaging for distribution** (Mac/Windows).

---

See **ARCHITECTURE.md** and **FILES_OVERVIEW.md** for deeper detail.
