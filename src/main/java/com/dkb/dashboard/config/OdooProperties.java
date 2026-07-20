package com.dkb.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed binding for Odoo JSON-2 API connection settings ({@code dkb.odoo.*}).
 *
 * <p>The JSON-2 API ({@code POST /json/2/<model>/<method>}) authenticates with an API key sent as a
 * Bearer token. The key is already bound to a specific Odoo user and database on the server, so —
 * unlike the classic JSON-RPC API — no database name, username, or separate authenticate step is
 * needed here.
 *
 * @param baseUrl            Odoo base URL, no path suffix (e.g. {@code https://odoo.example.com});
 *                           the {@code /json/2} segment is appended by the RestClient bean
 * @param apiKey             API key sent as {@code Authorization: Bearer <apiKey>}
 * @param recentActivityDays lookback window (days) for the "recent activity" count
 */
@ConfigurationProperties(prefix = "dkb.odoo")
public record OdooProperties(
        String baseUrl,
        String apiKey,
        int recentActivityDays
) {
    public OdooProperties {
        if (recentActivityDays <= 0) {
            recentActivityDays = 7;
        }
    }
}
