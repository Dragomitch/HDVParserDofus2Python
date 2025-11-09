package com.dofusretro.pricetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Dofus Retro Price Tracker.
 *
 * This application tracks auction house prices in Dofus Retro by:
 * - Capturing game network packets
 * - Automating GUI interactions for market data collection
 * - Storing price history in PostgreSQL
 * - Providing REST API for price analysis
 *
 * @author AGENT-INFRA
 * @version 0.1.0
 * @since 2025-11-09
 */
@SpringBootApplication
public class DofusRetroApplication {

    public static void main(String[] args) {
        SpringApplication.run(DofusRetroApplication.class, args);
    }
}
