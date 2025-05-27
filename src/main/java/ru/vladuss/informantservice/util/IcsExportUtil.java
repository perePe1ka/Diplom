package ru.vladuss.informantservice.util;

import ru.vladuss.informantservice.entity.Event;

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class IcsExportUtil {

    private IcsExportUtil() {
    }

    private static final String NL = "\r\n";
    private static final DateTimeFormatter ICS_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                    .withZone(ZoneOffset.UTC);

    public static byte[] toBytes(List<Event> events) {
        StringBuilder sb = new StringBuilder();

        sb.append("BEGIN:VCALENDAR").append(NL)
                .append("VERSION:2.0").append(NL)
                .append("PRODID:-//Informant//Events//RU").append(NL)
                .append("CALSCALE:GREGORIAN").append(NL);

        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);

        for (Event e : events) {
            sb.append("BEGIN:VEVENT").append(NL)
                    .append("UID:").append(e.getId()).append("@informant").append(NL)
                    .append("DTSTAMP:").append(ICS_FMT.format(nowUtc)).append(NL)
                    .append("DTSTART:").append(ICS_FMT.format(e.getStartsAt().toInstant())).append(NL)
                    .append("DTEND:").append(ICS_FMT.format(e.getEndsAt().toInstant())).append(NL)
                    .append("SUMMARY:").append(escape(e.getTitle())).append(NL);

            if (e.getDescription() != null && !e.getDescription().isBlank()) {
                sb.append("DESCRIPTION:").append(escape(e.getDescription())).append(NL);
            }
            if (e.getLocation() != null && !e.getLocation().isBlank()) {
                sb.append("LOCATION:").append(escape(e.getLocation())).append(NL);
            }

            sb.append("END:VEVENT").append(NL);
        }

        sb.append("END:VCALENDAR").append(NL);

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String escape(String s) {
        return s
                .replace("\\", "\\\\")
                .replace(",", "\\,")
                .replace(";", "\\;")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n");
    }
}