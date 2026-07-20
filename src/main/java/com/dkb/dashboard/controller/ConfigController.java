package com.dkb.dashboard.controller;

import com.dkb.dashboard.config.OdooProperties;
import com.dkb.dashboard.config.SnipeItProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Exposes non-secret configuration the frontend needs — currently the source system base URLs so
 * the UI can link out to Snipe-IT and Odoo. Deliberately returns only URLs, never tokens.
 */
@RestController
@RequestMapping("/api")
public class ConfigController {

    private final SnipeItProperties snipeIt;
    private final OdooProperties odoo;

    public ConfigController(SnipeItProperties snipeIt, OdooProperties odoo) {
        this.snipeIt = snipeIt;
        this.odoo = odoo;
    }

    /** External links for the sidebar. */
    @GetMapping("/config")
    public Map<String, String> config() {
        return Map.of(
                "snipeItUrl", snipeIt.baseUrl(),
                "odooUrl", odoo.baseUrl());
    }
}
