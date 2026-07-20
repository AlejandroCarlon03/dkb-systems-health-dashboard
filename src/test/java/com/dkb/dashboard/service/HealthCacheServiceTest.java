package com.dkb.dashboard.service;

import com.dkb.dashboard.model.HealthSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies the caching contract: first access loads synchronously, subsequent accesses are served
 * from cache without re-hitting aggregation, and {@code refresh()} replaces the cached snapshot.
 */
@ExtendWith(MockitoExtension.class)
class HealthCacheServiceTest {

    @Mock
    AggregationService aggregationService;

    @InjectMocks
    HealthCacheService cache;

    private static HealthSummary summary(HealthSummary.Status status) {
        return new HealthSummary(status, Instant.now(), null, null, List.of());
    }

    @Test
    void firstGetLoadsOnce_thenServesFromCache() {
        when(aggregationService.buildSummary()).thenReturn(summary(HealthSummary.Status.UP));

        HealthSummary first = cache.getLatest();
        HealthSummary second = cache.getLatest();

        assertThat(first).isSameAs(second);
        verify(aggregationService, times(1)).buildSummary();  // only the first call hits aggregation
    }

    @Test
    void refreshReplacesCachedSnapshot() {
        when(aggregationService.buildSummary())
                .thenReturn(summary(HealthSummary.Status.UP))
                .thenReturn(summary(HealthSummary.Status.DEGRADED));

        assertThat(cache.getLatest().status()).isEqualTo(HealthSummary.Status.UP);
        HealthSummary refreshed = cache.refresh();

        assertThat(refreshed.status()).isEqualTo(HealthSummary.Status.DEGRADED);
        assertThat(cache.getLatest().status()).isEqualTo(HealthSummary.Status.DEGRADED);
        verify(aggregationService, times(2)).buildSummary();
    }
}
