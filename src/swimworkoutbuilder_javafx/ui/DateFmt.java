package swimworkoutbuilder_javafx.ui;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for consistent local date/time formatting in the UI.
 */
public final class DateFmt {

    private DateFmt() {}  // prevent instantiation

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.systemDefault());          // "yyy-MM-dd hh:mm aa"

    /** Formats an Instant to a readable local string or “—” if null. */
    public static String local(Instant i) {
        return (i == null) ? "—" : FMT.format(i);
    }
}