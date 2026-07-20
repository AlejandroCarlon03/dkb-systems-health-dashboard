package com.dkb.dashboard.model.snipeit;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Snipe-IT serializes date fields as a small object, e.g.
 * {@code {"date": "2025-06-04", "formatted": "Wed Jun 04, 2025"}} — or as a datetime variant.
 * We only need the machine-readable value and parse it defensively.
 *
 * @param date     ISO-ish date string ({@code yyyy-MM-dd}), may be null
 * @param datetime datetime string ({@code yyyy-MM-dd HH:mm:ss}), may be null
 */
public record SnipeItDate(String date, String datetime) {

    /** Best-effort parse to a {@link LocalDate}; empty when absent or unparseable. */
    public Optional<LocalDate> toLocalDate() {
        String raw = date != null ? date : datetime;
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        // Take the date portion of a possible "yyyy-MM-dd HH:mm:ss" value.
        String datePart = raw.length() >= 10 ? raw.substring(0, 10) : raw;
        try {
            return Optional.of(LocalDate.parse(datePart));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }
}
