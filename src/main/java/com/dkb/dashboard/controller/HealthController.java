package com.dkb.dashboard.controller;

import com.dkb.dashboard.model.HealthSummary;
import com.dkb.dashboard.model.history.HistoryResponse;
import com.dkb.dashboard.service.HealthCacheService;
import com.dkb.dashboard.service.HealthHistoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for dashboard health signals.
 *
 * <p>{@code /api/health/summary} returns the current company-wide snapshot (served from cache).
 * {@code /api/health/history} returns persisted uptime/event history over a window of days.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private static final int MAX_HISTORY_DAYS = 90;

    private final HealthCacheService cache;
    private final HealthHistoryService history;

    public HealthController(HealthCacheService cache, HealthHistoryService history) {
        this.cache = cache;
        this.history = history;
    }

    /** Aggregated company-wide health across all sources, served from cache. */
    @GetMapping("/summary")
    public HealthSummary summary() {
        return cache.getLatest();
    }

    /** Uptime + status-change history over the last {@code days} (default 7, clamped to 1–90). */
    @GetMapping("/history")
    public HistoryResponse history(@RequestParam(defaultValue = "7") int days) {
        int clamped = Math.min(MAX_HISTORY_DAYS, Math.max(1, days));
        return history.history(clamped);
    }
}
