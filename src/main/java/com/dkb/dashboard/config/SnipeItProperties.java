package com.dkb.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed binding for Snipe-IT connection settings.
 *
 * <p>Bound from the {@code dkb.snipeit.*} namespace in application.properties, which in turn
 * pulls secrets from environment variables (e.g. {@code ${SNIPEIT_API_TOKEN}}) so tokens are
 * never committed.
 *
 * @param baseUrl base URL of the Snipe-IT instance, without the {@code /api/v1} suffix
 *                (e.g. {@code https://assets.example.com})
 * @param apiToken personal API token used as a Bearer credential
 * @param hardwarePageLimit max number of hardware rows to pull per poll when deriving summaries
 */
@ConfigurationProperties(prefix = "dkb.snipeit")
public record SnipeItProperties(
        String baseUrl,
        String apiToken,
        int hardwarePageLimit
) {
    public SnipeItProperties {
        if (hardwarePageLimit <= 0) {
            hardwarePageLimit = 500;
        }
    }
}
