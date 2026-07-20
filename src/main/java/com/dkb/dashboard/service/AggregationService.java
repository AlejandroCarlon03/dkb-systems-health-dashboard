package com.dkb.dashboard.service;

import com.dkb.dashboard.client.OdooClient;
import com.dkb.dashboard.client.SnipeItClient;
import com.dkb.dashboard.model.AssetSummary;
import com.dkb.dashboard.model.CrmSummary;
import com.dkb.dashboard.model.HealthSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Merges each source client's normalized output into a single {@link HealthSummary}.
 *
 * <p>Each source is fetched independently and defensively: a failure in one source is logged and
 * recorded, but does not prevent the others from contributing. This is what lets the dashboard
 * report {@code DEGRADED} instead of returning an error when, say, Odoo is unreachable.
 */
@Service
public class AggregationService {

    private static final Logger log = LoggerFactory.getLogger(AggregationService.class);

    private final SnipeItClient snipeItClient;
    private final OdooClient odooClient;

    public AggregationService(SnipeItClient snipeItClient, OdooClient odooClient) {
        this.snipeItClient = snipeItClient;
        this.odooClient = odooClient;
    }

    /** Fetch every source live and merge into one snapshot. */
    public HealthSummary buildSummary() {
        List<String> errors = new ArrayList<>();

        AssetSummary assets = null;
        try {
            assets = snipeItClient.fetchAssetSummary();
        } catch (RuntimeException e) {
            log.warn("Snipe-IT source failed: {}", e.getMessage());
            errors.add("Snipe-IT: " + e.getMessage());
        }

        CrmSummary crm = null;
        try {
            crm = odooClient.fetchCrmSummary();
        } catch (RuntimeException e) {
            log.warn("Odoo source failed: {}", e.getMessage());
            errors.add("Odoo: " + e.getMessage());
        }

        HealthSummary.Status status = deriveStatus(assets != null, crm != null);
        return new HealthSummary(status, Instant.now(), assets, crm, List.copyOf(errors));
    }

    private HealthSummary.Status deriveStatus(boolean assetsOk, boolean crmOk) {
        if (assetsOk && crmOk) {
            return HealthSummary.Status.UP;
        }
        if (!assetsOk && !crmOk) {
            return HealthSummary.Status.DOWN;
        }
        return HealthSummary.Status.DEGRADED;
    }
}
