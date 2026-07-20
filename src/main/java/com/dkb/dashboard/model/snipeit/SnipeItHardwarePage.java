package com.dkb.dashboard.model.snipeit;

import java.util.List;

/**
 * A page of the Snipe-IT {@code GET /api/v1/hardware} response.
 *
 * @param total total number of hardware assets matching the query (across all pages)
 * @param rows  the assets on this page
 */
public record SnipeItHardwarePage(
        long total,
        List<SnipeItHardware> rows
) {
    public List<SnipeItHardware> rows() {
        return rows == null ? List.of() : rows;
    }
}
