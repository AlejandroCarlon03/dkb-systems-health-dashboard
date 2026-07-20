package com.dkb.dashboard.model;

import java.time.Instant;

/**
 * Normalized, source-agnostic view of CRM health derived from Odoo.
 *
 * @param openOpportunities   count of open (in-pipeline, not-yet-won) opportunities
 * @param recentActivityCount CRM records created within the recent-activity lookback window
 * @param retrievedAt         when this snapshot was pulled from Odoo
 */
public record CrmSummary(
        long openOpportunities,
        long recentActivityCount,
        Instant retrievedAt
) {}
