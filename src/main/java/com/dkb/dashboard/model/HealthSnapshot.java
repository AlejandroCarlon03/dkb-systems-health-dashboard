package com.dkb.dashboard.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * A persisted point-in-time record of overall health, written on each scheduled poll. These rows
 * back the history/uptime view. Source-specific metrics are nullable because a source may have been
 * unavailable when the snapshot was captured.
 */
@Entity
@Table(name = "health_snapshot", indexes = @Index(name = "idx_captured_at", columnList = "capturedAt"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant capturedAt;

    @Enumerated(EnumType.STRING)
    private HealthSummary.Status status;

    /** How many sources responded (0–2). */
    private int sourcesUp;

    private Long totalAssets;
    private Long unassignedAssets;
    private Long overdueCheckouts;
    private Long openOpportunities;
    private Long recentActivityCount;

    public HealthSnapshot(Instant capturedAt, HealthSummary.Status status, int sourcesUp,
                          Long totalAssets, Long unassignedAssets, Long overdueCheckouts,
                          Long openOpportunities, Long recentActivityCount) {
        this.capturedAt = capturedAt;
        this.status = status;
        this.sourcesUp = sourcesUp;
        this.totalAssets = totalAssets;
        this.unassignedAssets = unassignedAssets;
        this.overdueCheckouts = overdueCheckouts;
        this.openOpportunities = openOpportunities;
        this.recentActivityCount = recentActivityCount;
    }

    /** Build a snapshot from a live summary, flattening the nullable source sections. */
    public static HealthSnapshot from(HealthSummary summary) {
        AssetSummary a = summary.assets();
        CrmSummary c = summary.crm();
        int sourcesUp = (a != null ? 1 : 0) + (c != null ? 1 : 0);
        return new HealthSnapshot(
                summary.generatedAt(),
                summary.status(),
                sourcesUp,
                a != null ? a.totalAssets() : null,
                a != null ? a.unassignedAssets() : null,
                a != null ? a.overdueCheckouts() : null,
                c != null ? c.openOpportunities() : null,
                c != null ? c.recentActivityCount() : null);
    }
}
