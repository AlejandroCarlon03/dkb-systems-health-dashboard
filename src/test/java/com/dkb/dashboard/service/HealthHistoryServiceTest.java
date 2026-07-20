package com.dkb.dashboard.service;

import com.dkb.dashboard.model.HealthSnapshot;
import com.dkb.dashboard.model.HealthSummary.Status;
import com.dkb.dashboard.model.history.HistoryResponse;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the pure summarization logic (no DB): uptime %, event detection, and timeline.
 */
class HealthHistoryServiceTest {

    private static final Instant NOW = Instant.parse("2026-07-20T00:00:00Z");
    private static final Instant SINCE = NOW.minus(Duration.ofDays(1));

    private static HealthSnapshot snap(long secondsIn, Status status) {
        return new HealthSnapshot(SINCE.plusSeconds(secondsIn), status, 2, 99L, 62L, 0L, 1343L, 91L);
    }

    @Test
    void summarizesCountsUptimeAndEvents() {
        List<HealthSnapshot> snaps = List.of(
                snap(0, Status.UP),
                snap(60, Status.UP),
                snap(120, Status.DEGRADED),
                snap(180, Status.DOWN),
                snap(240, Status.UP));

        HistoryResponse r = HealthHistoryService.summarize(snaps, 1, SINCE, NOW);

        assertThat(r.sampleCount()).isEqualTo(5);
        assertThat(r.upCount()).isEqualTo(3);
        assertThat(r.degradedCount()).isEqualTo(1);
        assertThat(r.downCount()).isEqualTo(1);
        assertThat(r.uptimePercent()).isEqualTo(60.0);

        // Three transitions: UP->DEGRADED, DEGRADED->DOWN, DOWN->UP (repeated UP->UP is not an event).
        assertThat(r.events()).hasSize(3);
        assertThat(r.events().getFirst().from()).isEqualTo(Status.UP);
        assertThat(r.events().getFirst().to()).isEqualTo(Status.DEGRADED);

        assertThat(r.timeline()).hasSize(100);
        assertThat(r.firstSampleAt()).isEqualTo(SINCE);
        assertThat(r.lastSampleAt()).isEqualTo(SINCE.plusSeconds(240));
    }

    @Test
    void emptyHistoryReportsFullUptimeAndNoEvents() {
        HistoryResponse r = HealthHistoryService.summarize(List.of(), 7, SINCE, NOW);

        assertThat(r.sampleCount()).isZero();
        assertThat(r.uptimePercent()).isEqualTo(100.0);
        assertThat(r.events()).isEmpty();
        assertThat(r.firstSampleAt()).isNull();
        assertThat(r.timeline()).hasSize(100);
        assertThat(r.timeline()).allSatisfy(b -> assertThat(b.status()).isNull());
    }
}
