package com.dkb.dashboard.client;

import com.dkb.dashboard.config.SnipeItProperties;
import com.dkb.dashboard.model.AssetSummary;
import com.dkb.dashboard.model.snipeit.SnipeItHardware;
import com.dkb.dashboard.model.snipeit.SnipeItHardwarePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Thin wrapper around the Snipe-IT v1 REST API.
 *
 * <p>Fetches hardware and normalizes it into an {@link AssetSummary}. The RestClient it uses
 * already has the base URL and Bearer auth applied (see
 * {@link com.dkb.dashboard.config.RestClientConfig}).
 *
 * <p><b>MVP note:</b> {@code totalAssets} is Snipe-IT's authoritative total across all pages,
 * while the derived counts (unassigned/overdue/by-status) are computed from the first page of up
 * to {@code hardwarePageLimit} rows. For a large fleet this should paginate; that's a stretch item.
 */
@Component
public class SnipeItClient {

    private static final Logger log = LoggerFactory.getLogger(SnipeItClient.class);

    private final RestClient client;
    private final int pageLimit;

    public SnipeItClient(RestClient snipeItRestClient, SnipeItProperties props) {
        this.client = snipeItRestClient;
        this.pageLimit = props.hardwarePageLimit();
    }

    /**
     * Pull hardware from Snipe-IT and normalize it into an {@link AssetSummary}.
     *
     * @throws org.springframework.web.client.RestClientException if the API call fails
     */
    public AssetSummary fetchAssetSummary() {
        SnipeItHardwarePage page = client.get()
                .uri(uri -> uri.path("/hardware")
                        .queryParam("limit", pageLimit)
                        .queryParam("offset", 0)
                        .build())
                .retrieve()
                .body(SnipeItHardwarePage.class);

        if (page == null) {
            log.warn("Snipe-IT returned an empty body for /hardware");
            return new AssetSummary(0, 0, 0, Map.of(), Instant.now());
        }

        List<SnipeItHardware> rows = page.rows();
        LocalDate today = LocalDate.now();

        // "Unassigned" means spare/available gear — exclude archived (retired) assets.
        long unassigned = rows.stream()
                .filter(SnipeItHardware::isUnassigned)
                .filter(h -> !h.isArchived())
                .count();

        long overdue = rows.stream()
                .filter(h -> !h.isUnassigned())
                .filter(h -> h.expectedCheckin() != null)
                .filter(h -> h.expectedCheckin().toLocalDate()
                        .map(d -> d.isBefore(today))
                        .orElse(false))
                .count();

        Map<String, Long> byStatus = rows.stream()
                .collect(Collectors.groupingBy(
                        SnipeItHardware::statusName,
                        Collectors.counting()));

        return new AssetSummary(page.total(), unassigned, overdue, byStatus, Instant.now());
    }
}
