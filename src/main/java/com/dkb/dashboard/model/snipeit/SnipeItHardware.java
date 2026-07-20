package com.dkb.dashboard.model.snipeit;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single hardware asset row from Snipe-IT. Only the fields the dashboard needs are mapped;
 * unknown fields are ignored (see {@code spring.jackson.deserialization.fail-on-unknown-properties}).
 *
 * @param id             Snipe-IT internal id
 * @param assetTag       human-facing asset tag
 * @param name           asset name
 * @param statusLabel    status label with its meta category (deployable, deployed, etc.)
 * @param assignedTo     who/what the asset is checked out to; {@code null} when unassigned
 * @param expectedCheckin expected check-in date; {@code null} when not checked out or open-ended
 */
public record SnipeItHardware(
        long id,
        @JsonProperty("asset_tag") String assetTag,
        String name,
        @JsonProperty("status_label") StatusLabel statusLabel,
        @JsonProperty("assigned_to") AssignedTo assignedTo,
        @JsonProperty("expected_checkin") SnipeItDate expectedCheckin
) {

    /** Human-readable status name, or {@code "Unknown"} when Snipe-IT omitted the label. */
    public String statusName() {
        return statusLabel == null || statusLabel.name() == null ? "Unknown" : statusLabel.name();
    }

    /** True when the asset is not currently checked out to anyone. */
    public boolean isUnassigned() {
        return assignedTo == null;
    }

    /** True when the asset is archived (retired), based on its status label's meta category. */
    public boolean isArchived() {
        return statusLabel != null && "archived".equalsIgnoreCase(statusLabel.statusMeta());
    }

    /** @param name status label text (e.g. "Ready to Deploy")
     *  @param statusMeta coarse Snipe-IT category: deployable, deployed, archived, pending, undeployable */
    public record StatusLabel(
            String name,
            @JsonProperty("status_meta") String statusMeta
    ) {}

    /** Assignee target; presence is what matters for the "unassigned" count. */
    public record AssignedTo(long id, String name) {}
}
