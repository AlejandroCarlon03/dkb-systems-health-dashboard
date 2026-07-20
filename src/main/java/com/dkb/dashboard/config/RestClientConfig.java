package com.dkb.dashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * Builds pre-configured {@link RestClient} beans for each external system.
 *
 * <p>Each bean is qualified so clients can inject exactly the one they need. Auth headers and
 * base URLs are baked in here from typed properties, keeping the client classes thin.
 */
@Configuration
public class RestClientConfig {

    /**
     * RestClient targeting the Snipe-IT v1 API with Bearer auth pre-applied.
     */
    @Bean
    RestClient snipeItRestClient(SnipeItProperties props) {
        return RestClient.builder()
                .baseUrl(normalizeBaseUrl(props.baseUrl()) + "/api/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.apiToken())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * RestClient targeting Odoo's JSON-2 API ({@code /json/2}) with the API key applied as a
     * Bearer token. Model calls are {@code POST /json/2/<model>/<method>} with a JSON kwargs body.
     */
    @Bean
    RestClient odooRestClient(OdooProperties props) {
        return RestClient.builder()
                .baseUrl(normalizeBaseUrl(props.baseUrl()) + "/json/2")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.apiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Strip a trailing slash so a configured base URL like {@code https://host/} doesn't produce a
     * double slash ({@code https://host//api/v1}) when a path suffix is appended.
     */
    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null) {
            return "";
        }
        String trimmed = baseUrl.strip();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }
}
