package com.dkb.dashboard.client;

import com.dkb.dashboard.config.OdooProperties;
import com.dkb.dashboard.model.CrmSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Thin wrapper around Odoo's JSON-2 API.
 *
 * <p>Calls take the form {@code POST /json/2/<model>/<method>} with a JSON kwargs body and an
 * {@code Authorization: Bearer <apiKey>} header (applied by the RestClient bean). The API key is
 * bound to a user + database server-side, so there is no separate authenticate/uid step.
 *
 * <p>We use {@code search_count} to pull cheap aggregate numbers without transferring record
 * bodies, and normalize them into a {@link CrmSummary}. Some Odoo deployments wrap responses as
 * {@code {"result": ...}}; {@link #extractCount(JsonNode)} handles both wrapped and bare values.
 */
@Component
public class OdooClient {

    private static final Logger log = LoggerFactory.getLogger(OdooClient.class);
    private static final String LEAD_MODEL = "crm.lead";

    private final RestClient client;
    private final OdooProperties props;

    public OdooClient(RestClient odooRestClient, OdooProperties props) {
        this.client = odooRestClient;
        this.props = props;
    }

    /**
     * Pull CRM aggregates from Odoo and normalize them into a {@link CrmSummary}.
     *
     * @throws org.springframework.web.client.RestClientException if a call fails (non-2xx)
     */
    public CrmSummary fetchCrmSummary() {
        // Open pipeline: active opportunities that are not yet won. Odoo keeps won deals active
        // with probability == 100, so "< 100" excludes them (lost deals are already active == false).
        long openOpportunities = searchCount(List.of(
                List.of("type", "=", "opportunity"),
                List.of("active", "=", true),
                List.of("probability", "<", 100)));

        String since = LocalDate.now().minusDays(props.recentActivityDays()).toString();
        long recentActivity = searchCount(List.of(
                List.of("create_date", ">=", since)));

        return new CrmSummary(openOpportunities, recentActivity, Instant.now());
    }

    /** Run {@code search_count} on the CRM lead model for the given Odoo domain. */
    private long searchCount(List<Object> domain) {
        JsonNode response = client.post()
                .uri("/" + LEAD_MODEL + "/search_count")
                .body(Map.of("domain", domain))
                .retrieve()
                .body(JsonNode.class);
        return extractCount(response);
    }

    /** JSON-2 may return the count bare ({@code 42}) or wrapped ({@code {"result": 42}}). */
    private long extractCount(JsonNode response) {
        if (response == null) {
            log.warn("Odoo returned an empty body for search_count");
            return 0L;
        }
        JsonNode value = response.has("result") ? response.get("result") : response;
        return value.isNumber() ? value.longValue() : 0L;
    }
}
