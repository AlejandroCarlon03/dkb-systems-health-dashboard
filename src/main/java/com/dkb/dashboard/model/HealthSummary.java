package com.dkb.dashboard.model;

import java.time.Instant;
import java.util.List;

/**
 * Company-wide health snapshot merging every data source into one object.
 *
 * <p>Resilient by design: a source that fails to respond leaves its section {@code null} and adds a
 * note to {@link #sourceErrors()} rather than failing the whole request. {@link #status()} reflects
 * how many sources responded.
 *
 * @param status       overall status: {@code UP} (all sources ok), {@code DEGRADED} (some failed),
 *                     or {@code DOWN} (all failed)
 * @param generatedAt  when this snapshot was assembled
 * @param assets       asset health from Snipe-IT; {@code null} if that source failed
 * @param crm          CRM health from Odoo; {@code null} if that source failed
 * @param sourceErrors human-readable notes for any source that failed
 */
public record HealthSummary(
        Status status,
        Instant generatedAt,
        AssetSummary assets,
        CrmSummary crm,
        List<String> sourceErrors
) {
    public enum Status { UP, DEGRADED, DOWN }
}
