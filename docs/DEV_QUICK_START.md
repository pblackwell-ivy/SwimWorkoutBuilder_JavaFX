# Developer Quick Start

## Prereqs
- Java 21+ (or project’s configured JDK)
- Gradle (wrapper recommended)
- JavaFX 21 libraries

## Run
```bash
./gradlew run
```
Or launch from your IDE with the correct JavaFX VM args if not using the plugin.

## Project Conventions
- **AppState** orchestrates selection/current entities
- **LocalStore** persists swimmers/workouts
- **Panes**: SwimmerPane, WorkoutPane, BuilderPane (scrollable)
- **Dialogs**: focused edits; commit or cancel explicitly
- **Theme**: `styles-ocean-depth.css` with role-based classes
