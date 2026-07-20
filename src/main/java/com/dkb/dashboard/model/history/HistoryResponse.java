package com.dkb.dashboard.model.history;

import com.dkb.dashboard.model.HealthSummary.Status;

import java.time.Instant;
import java.util.List;

/**
 * Summarized health history over a window of days: sample counts, uptime, a log of status-change
 * events, and a downsampled timeline suitable for rendering a status strip.
 *
 * @param days          the requested window in days
 * @param sampleCount   number of snapshots in the window
 * @param upCount       snapshots with status UP
 * @param degradedCount snapshots with status DEGRADED
 * @param downCount     snapshots with status DOWN
 * @param uptimePercent percentage of samples that were fully UP (100 when there are no samples)
 * @param firstSampleAt earliest sample instant, or null when empty
 * @param lastSampleAt  latest sample instant, or null when empty
 * @param events        status transitions in chronological order
 * @param timeline      fixed-width buckets, each holding the worst status seen in that slice (or null)
 */
public record HistoryResponse(
        int days,
        int sampleCount,
        long upCount,
        long degradedCount,
        long downCount,
        double uptimePercent,
        Instant firstSampleAt,
        Instant lastSampleAt,
        List<Event> events,
        List<Bucket> timeline
) {
    /** A status transition from one status to another at a point in time. */
    public record Event(Instant at, Status from, Status to) {}

    /** One time slice of the timeline; {@code status} is null when no samples fell in the slice. */
    public record Bucket(Instant at, Status status) {}
}
