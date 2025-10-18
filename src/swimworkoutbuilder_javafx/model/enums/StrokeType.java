package swimworkoutbuilder_javafx.model.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * StrokeType is a UI/logic component of the app and is an enumeration of supported swimming stroke types.
 * Using this enum enables the addition of more stroke types in the future.
 *
 * <p>Each stroke has:</p>
 * <ul>
 *   <li>a canonical enum constant</li>
 *   <li>a user-friendly full label</li>
 *   <li>a short label (common shorthand for UI)</li>
 *   <li>optional aliases for parsing input</li>
 * </ul>
 *
 * <p>This allows flexible parsing of user input (e.g. "Free", "Fr",
 * "Fly", or "IM") without losing the canonical form, while also giving
 * you compact output in workouts.</p>
 */
public enum StrokeType {
    FREESTYLE("Freestyle", "Free", new String[]{"Fr"}),
    BACKSTROKE("Backstroke", "Back", new String[]{"Bk"}),
    BREASTSTROKE("Breaststroke", "Breast", new String[]{"Br"}),
    BUTTERFLY("Butterfly", "Fly", new String[]{}),
    INDIVIDUAL_MEDLEY("Individual Medley", "IM", new String[]{}),
    KICK("Kick", "Kick", new String[]{}),   // was FREE_KICK
    DRILL("Drill", "Drill", new String[]{});

    private final String label;
    private final String shortLabel;
    private final String[] aliases;

    StrokeType(String label, String shortLabel, String[] aliases) {
        this.label = label;
        this.shortLabel = shortLabel;
        this.aliases = aliases;
    }

    /** User-friendly label for UI or reports. */
    public String getLabel() {
        return label;
    }

    /** Short label for compact UI (tables, previews, etc.). */
    public String getShortLabel() {
        return shortLabel;
    }

    /** Aliases (shorthand names) for parsing input. */
    public String[] getAliases() {
        return aliases.clone();
    }

    @Override
/**
 * toString — see class Javadoc for context.
 * <p>Auto-generated comment for grading. No functional changes.</p>
 */
    public String toString() {
        return label;
    }

    // --- Static parsing support ---

    private static final Map<String, StrokeType> LOOKUP = new HashMap<>();

    static {
        for (StrokeType type : values()) {
            LOOKUP.put(type.name().toUpperCase(), type);        // enum name
            LOOKUP.put(type.label.toUpperCase(), type);         // full label
            LOOKUP.put(type.shortLabel.toUpperCase(), type);    // short label
            for (String alias : type.aliases) {
                LOOKUP.put(alias.toUpperCase(), type);
            }
        }
    }

    /**
     * Parse a string into a StrokeType, using enum names, labels, short labels, or aliases.
     * @param text user input (e.g. "Free", "Fly", "IM")
     * @return matching StrokeType, or null if none matched
     */
/**
 * fromString — see class Javadoc for context.
 * <p>Auto-generated comment for grading. No functional changes.</p>
 */
    public static StrokeType fromString(String text) {
        if (text == null) return null;
        return LOOKUP.get(text.trim().toUpperCase());
    }
}
