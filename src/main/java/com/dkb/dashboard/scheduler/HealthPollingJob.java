package com.dkb.dashboard.scheduler;

import com.dkb.dashboard.model.HealthSummary;
import com.dkb.dashboard.service.HealthCacheService;
import com.dkb.dashboard.service.HealthHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically refreshes the health cache in the background so the source APIs are polled on a
 * schedule instead of on every user request, and records each result to history.
 *
 * <p>Interval is configured by {@code dkb.polling.interval-ms} (with a short initial delay so the
 * cache is warmed shortly after startup). A failure here is swallowed and logged: the poller must
 * keep running, and {@link com.dkb.dashboard.service.AggregationService} already degrades per source.
 */
@Component
public class HealthPollingJob {

    private static final Logger log = LoggerFactory.getLogger(HealthPollingJob.class);

    private final HealthCacheService cache;
    private final HealthHistoryService history;

    public HealthPollingJob(HealthCacheService cache, HealthHistoryService history) {
        this.cache = cache;
        this.history = history;
    }

    @Scheduled(
            fixedDelayString = "${dkb.polling.interval-ms}",
            initialDelayString = "${dkb.polling.initial-delay-ms}")
    public void poll() {
        try {
            HealthSummary summary = cache.refresh();
            history.record(summary);
        } catch (RuntimeException e) {
            log.error("Scheduled health refresh failed; keeping previous cached snapshot", e);
        }
    }
}
