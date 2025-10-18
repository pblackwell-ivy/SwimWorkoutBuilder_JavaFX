# Key Design Decisions

1. **AppState as the single source of truth** for current swimmer and workout selection, reducing coupling between panes.
2. **LocalStore** for persistence: simple, inspectable, and good for offline desktop use.
3. **Role-based CSS**: consistent visual language across themes (currently `styles-ocean-depth.css`), enabling future theming.
4. **Pane-per-responsibility**: SwimmerPane (athlete data), WorkoutPane (header/actions), BuilderPane (scrollable editor).
5. **Dialog-centered edits**: predictable modals to minimize accidental edits and encourage confirmation.
6. **Course Unit Model**: Keep canonical storage simple; conversions and formatting handled at display-time (post-MVP: laps-based rules).
7. **Extensibility**: Clear separation of **model** (Swimmer, Workout, SetGroup, SwimSet) from **UI** (panes, dialogs), enabling feature growth.
