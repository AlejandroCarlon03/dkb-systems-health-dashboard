package com.dkb.dashboard.client;

import com.dkb.dashboard.config.SnipeItProperties;
import com.dkb.dashboard.model.AssetSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Exercises {@link SnipeItClient}'s DTO mapping + normalization against a canned Snipe-IT response,
 * using {@link MockRestServiceServer} so no real HTTP happens.
 */
class SnipeItClientTest {

    private static final String HARDWARE_JSON = """
            {
              "total": 42,
              "rows": [
                {"id":1,"asset_tag":"DKB-001","name":"Laptop A",
                 "status_label":{"name":"Ready to Deploy","status_meta":"deployable"},
                 "assigned_to":null,"expected_checkin":null},
                {"id":2,"asset_tag":"DKB-002","name":"Laptop B",
                 "status_label":{"name":"Deployed","status_meta":"deployed"},
                 "assigned_to":{"id":5,"name":"Alice"},"expected_checkin":{"date":"2020-01-01"}},
                {"id":3,"asset_tag":"DKB-003","name":"Laptop C",
                 "status_label":{"name":"Deployed","status_meta":"deployed"},
                 "assigned_to":{"id":6,"name":"Bob"},"expected_checkin":{"date":"2999-01-01"}},
                {"id":4,"asset_tag":"DKB-004","name":"Monitor",
                 "status_label":{"name":"Ready to Deploy","status_meta":"deployable"},
                 "assigned_to":null,"expected_checkin":null},
                {"id":5,"asset_tag":"DKB-005","name":"Old Laptop",
                 "status_label":{"name":"Archived","status_meta":"archived"},
                 "assigned_to":null,"expected_checkin":null}
              ]
            }
            """;

    private RestClient.Builder builder;
    private MockRestServiceServer server;
    private SnipeItClient client;

    @BeforeEach
    void setUp() {
        builder = RestClient.builder().baseUrl("http://snipe.test/api/v1");
        server = MockRestServiceServer.bindTo(builder).build();
        SnipeItProperties props = new SnipeItProperties("http://snipe.test", "test-token", 500);
        client = new SnipeItClient(builder.build(), props);
    }

    @Test
    void normalizesHardwareIntoAssetSummary() {
        server.expect(requestTo(startsWith("http://snipe.test/api/v1/hardware")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(HARDWARE_JSON, MediaType.APPLICATION_JSON));

        AssetSummary summary = client.fetchAssetSummary();

        assertThat(summary.totalAssets()).isEqualTo(42);           // authoritative total from Snipe-IT
        assertThat(summary.unassignedAssets()).isEqualTo(2);       // rows 1 & 4; row 5 excluded (archived)
        assertThat(summary.overdueCheckouts()).isEqualTo(1);       // row 2 (2020) overdue; row 3 (2999) not
        assertThat(summary.assetsByStatus())
                .containsEntry("Ready to Deploy", 2L)
                .containsEntry("Deployed", 2L)
                .containsEntry("Archived", 1L);                    // archived still shown in the breakdown
        assertThat(summary.retrievedAt()).isNotNull();
        server.verify();
    }

    @Test
    void handlesEmptyBodyGracefully() {
        server.expect(requestTo(startsWith("http://snipe.test/api/v1/hardware")))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        AssetSummary summary = client.fetchAssetSummary();

        assertThat(summary.totalAssets()).isZero();
        assertThat(summary.assetsByStatus()).isEmpty();
        server.verify();
    }
}
