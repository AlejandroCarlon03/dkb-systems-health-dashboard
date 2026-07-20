package com.dkb.dashboard.service;

import com.dkb.dashboard.model.HealthSnapshot;
import com.dkb.dashboard.model.HealthSummary;
import com.dkb.dashboard.model.HealthSummary.Status;
import com.dkb.dashboard.model.history.HistoryResponse;
import com.dkb.dashboard.repository.HealthSnapshotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists health snapshots and summarizes them into an uptime/event history.
 */
@Service
public class HealthHistoryService {

    private static final Logger log = LoggerFactory.getLogger(HealthHistoryService.class);
    private static final int TIMELINE_BUCKETS = 100;

    private final HealthSnapshotRepository repository;
    private final int retentionDays;

    public HealthHistoryService(HealthSnapshotRepository repository,
                                @Value("${dkb.history.retention-days:30}") int retentionDays) {
        this.repository = repository;
        this.retentionDays = retentionDays;
    }

    /** Persist a snapshot of the current summary (called by the scheduled poller). */
    @Transactional
    public void record(HealthSummary summary) {
        repository.save(HealthSnapshot.from(summary));
    }

    /** Summarize snapshots over the last {@code days} into uptime stats, events, and a timeline. */
    @Transactional(readOnly = true)
    public HistoryResponse history(int days) {
        Instant now = Instant.now();
        Instant since = now.minus(Duration.ofDays(days));
        List<HealthSnapshot> snapshots = repository.findByCapturedAtAfterOrderByCapturedAtAsc(since);
        return summarize(snapshots, days, since, now);
    }

    /** Delete snapshots older than the retention window; runs nightly. */
    @Transactional
    @Scheduled(cron = "${dkb.history.prune-cron:0 30 3 * * *}")
    public void prune() {
        long removed = repository.deleteByCapturedAtBefore(Instant.now().minus(Duration.ofDays(retentionDays)));
        if (removed > 0) {
            log.info("Pruned {} health snapshots older than {} days", removed, retentionDays);
        }
    }

    /**
     * Pure summarization of an ordered snapshot list — no DB access, so it is easy to unit-test.
     *
     * @param snapshots snapshots ordered by capture time ascending
     */
    static HistoryResponse summarize(List<HealthSnapshot> snapshots, int days, Instant since, Instant now) {
        long up = 0;
        long degraded = 0;
        long down = 0;
        List<HistoryResponse.Event> events = new ArrayList<>();
        Status previous = null;

        for (HealthSnapshot s : snapshots) {
            switch (s.getStatus()) {
                case UP -> up++;
                case DEGRADED -> degraded++;
                case DOWN -> down++;
            }
            if (previous != null && previous != s.getStatus()) {
                events.add(new HistoryResponse.Event(s.getCapturedAt(), previous, s.getStatus()));
            }
            previous = s.getStatus();
        }

        int sampleCount = snapshots.size();
        double uptimePercent = sampleCount == 0 ? 100.0 : round1(up * 100.0 / sampleCount);
        Instant first = sampleCount == 0 ? null : snapshots.getFirst().getCapturedAt();
        Instant last = sampleCount == 0 ? null : snapshots.getLast().getCapturedAt();

        return new HistoryResponse(days, sampleCount, up, degraded, down, uptimePercent,
                first, last, events, buildTimeline(snapshots, since, now));
    }

    private static List<HistoryResponse.Bucket> buildTimeline(List<HealthSnapshot> snapshots, Instant since, Instant now) {
        long windowMs = Math.max(1, now.toEpochMilli() - since.toEpochMilli());
        long bucketMs = Math.max(1, windowMs / TIMELINE_BUCKETS);
        Status[] worst = new Status[TIMELINE_BUCKETS];

        for (HealthSnapshot s : snapshots) {
            long offset = s.getCapturedAt().toEpochMilli() - since.toEpochMilli();
            int idx = (int) (offset / bucketMs);
            idx = Math.min(TIMELINE_BUCKETS - 1, Math.max(0, idx));
            worst[idx] = worse(worst[idx], s.getStatus());
        }

        List<HistoryResponse.Bucket> timeline = new ArrayList<>(TIMELINE_BUCKETS);
        for (int i = 0; i < TIMELINE_BUCKETS; i++) {
            timeline.add(new HistoryResponse.Bucket(since.plusMillis(i * bucketMs), worst[i]));
        }
        return timeline;
    }

    /** Higher severity wins (DOWN > DEGRADED > UP); nulls are ignored. */
    private static Status worse(Status a, Status b) {
        if (a == null) return b;
        if (b == null) return a;
        return severity(a) >= severity(b) ? a : b;
    }

    private static int severity(Status s) {
        return switch (s) {
            case DOWN -> 3;
            case DEGRADED -> 2;
            case UP -> 1;
        };
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
