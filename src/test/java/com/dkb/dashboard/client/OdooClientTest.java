package com.dkb.dashboard.client;

import com.dkb.dashboard.config.OdooProperties;
import com.dkb.dashboard.model.CrmSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Exercises {@link OdooClient}'s JSON-2 flow: three {@code search_count} POSTs against
 * {@code /json/2/crm.lead/search_count}, verified with {@link MockRestServiceServer}.
 * Responses are queued in call order (leads, open opportunities, recent activity).
 */
class OdooClientTest {

    private static final String COUNT_URL = "http://odoo.test/json/2/crm.lead/search_count";

    private RestClient.Builder builder;
    private MockRestServiceServer server;
    private OdooClient client;

    @BeforeEach
    void setUp() {
        builder = RestClient.builder().baseUrl("http://odoo.test/json/2");
        server = MockRestServiceServer.bindTo(builder).build();
        OdooProperties props = new OdooProperties("http://odoo.test", "secret-key", 7);
        client = new OdooClient(builder.build(), props);
    }

    @Test
    void aggregatesCrmCountsViaSearchCount() {
        // 1) open opportunities -> 3, wrapped as {"result": 3} to exercise unwrapping.
        //    Assert the domain filters to non-won opportunities.
        server.expect(requestTo(COUNT_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.domain[0][2]").value("opportunity"))
                .andExpect(jsonPath("$.domain[2][0]").value("probability"))
                .andRespond(withSuccess("{\"result\":3}", MediaType.APPLICATION_JSON));
        // 2) recent activity -> 5 (bare number)
        server.expect(requestTo(COUNT_URL))
                .andRespond(withSuccess("5", MediaType.APPLICATION_JSON));

        CrmSummary summary = client.fetchCrmSummary();

        assertThat(summary.openOpportunities()).isEqualTo(3);
        assertThat(summary.recentActivityCount()).isEqualTo(5);
        assertThat(summary.retrievedAt()).isNotNull();
        server.verify();
    }

    @Test
    void throwsOnHttpError() {
        server.expect(requestTo(COUNT_URL))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> client.fetchCrmSummary())
                .isInstanceOf(RestClientException.class);
        server.verify();
    }
}
