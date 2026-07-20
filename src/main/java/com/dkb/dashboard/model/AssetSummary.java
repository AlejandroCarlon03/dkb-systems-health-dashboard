package com.dkb.dashboard.model;

import java.time.Instant;
import java.util.Map;

/**
 * Normalized, source-agnostic view of asset health derived from Snipe-IT.
 *
 * <p>This is the shape the dashboard exposes; it deliberately hides Snipe-IT's raw response
 * structure so other sources or a future asset backend can populate the same model.
 *
 * @param totalAssets      total assets known to the system
 * @param unassignedAssets assets not currently checked out to anyone
 * @param overdueCheckouts checked-out assets whose expected check-in date has passed
 * @param assetsByStatus   count of assets grouped by their status label
 * @param retrievedAt      when this snapshot was pulled from the source
 */
public record AssetSummary(
        long totalAssets,
        long unassignedAssets,
        long overdueCheckouts,
        Map<String, Long> assetsByStatus,
        Instant retrievedAt
) {}
