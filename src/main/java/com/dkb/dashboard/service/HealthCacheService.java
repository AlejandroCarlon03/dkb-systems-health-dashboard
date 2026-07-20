package com.dkb.dashboard.service;

import com.dkb.dashboard.model.HealthSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Holds the most recent {@link HealthSummary} in memory so REST requests are served from cache
 * rather than hitting the live source APIs on every call.
 *
 * <p>The cache is populated by the scheduled poller (see
 * {@link com.dkb.dashboard.scheduler.HealthPollingJob}). If a request arrives before the first
 * scheduled poll completes, {@link #getLatest()} performs a one-off synchronous refresh so callers
 * never receive an empty response.
 */
@Service
public class HealthCacheService {

    private static final Logger log = LoggerFactory.getLogger(HealthCacheService.class);

    private final AggregationService aggregationService;
    private final AtomicReference<HealthSummary> cache = new AtomicReference<>();

    public HealthCacheService(AggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    /** Return the cached snapshot, doing a first synchronous load if the cache is still empty. */
    public HealthSummary getLatest() {
        HealthSummary current = cache.get();
        if (current != null) {
            return current;
        }
        log.debug("Health cache empty; performing first synchronous refresh");
        return refresh();
    }

    /** Rebuild the snapshot from all sources and store it. Called by the scheduler and on first load. */
    public HealthSummary refresh() {
        HealthSummary summary = aggregationService.buildSummary();
        cache.set(summary);
        log.debug("Health cache refreshed: status={}", summary.status());
        return summary;
    }
}
