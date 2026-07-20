package com.dkb.dashboard.service;

import com.dkb.dashboard.client.OdooClient;
import com.dkb.dashboard.client.SnipeItClient;
import com.dkb.dashboard.model.AssetSummary;
import com.dkb.dashboard.model.CrmSummary;
import com.dkb.dashboard.model.HealthSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Verifies {@link AggregationService}'s per-source resilience: a failing source degrades the
 * overall status instead of failing the whole request.
 */
@ExtendWith(MockitoExtension.class)
class AggregationServiceTest {

    @Mock
    SnipeItClient snipeItClient;

    @Mock
    OdooClient odooClient;

    @InjectMocks
    AggregationService aggregationService;

    private static AssetSummary sampleAssets() {
        return new AssetSummary(10, 4, 1, Map.of("Deployed", 6L), Instant.now());
    }

    private static CrmSummary sampleCrm() {
        return new CrmSummary(5, 8, Instant.now());
    }

    @Test
    void bothSourcesOk_statusUp() {
        when(snipeItClient.fetchAssetSummary()).thenReturn(sampleAssets());
        when(odooClient.fetchCrmSummary()).thenReturn(sampleCrm());

        HealthSummary summary = aggregationService.buildSummary();

        assertThat(summary.status()).isEqualTo(HealthSummary.Status.UP);
        assertThat(summary.assets()).isNotNull();
        assertThat(summary.crm()).isNotNull();
        assertThat(summary.sourceErrors()).isEmpty();
    }

    @Test
    void oneSourceFails_statusDegradedWithError() {
        when(snipeItClient.fetchAssetSummary()).thenReturn(sampleAssets());
        when(odooClient.fetchCrmSummary()).thenThrow(new IllegalStateException("Odoo unreachable"));

        HealthSummary summary = aggregationService.buildSummary();

        assertThat(summary.status()).isEqualTo(HealthSummary.Status.DEGRADED);
        assertThat(summary.assets()).isNotNull();
        assertThat(summary.crm()).isNull();
        assertThat(summary.sourceErrors()).hasSize(1);
        assertThat(summary.sourceErrors().getFirst()).contains("Odoo").contains("unreachable");
    }

    @Test
    void bothSourcesFail_statusDown() {
        when(snipeItClient.fetchAssetSummary()).thenThrow(new RuntimeException("Snipe down"));
        when(odooClient.fetchCrmSummary()).thenThrow(new RuntimeException("Odoo down"));

        HealthSummary summary = aggregationService.buildSummary();

        assertThat(summary.status()).isEqualTo(HealthSummary.Status.DOWN);
        assertThat(summary.assets()).isNull();
        assertThat(summary.crm()).isNull();
        assertThat(summary.sourceErrors()).hasSize(2);
    }
}
