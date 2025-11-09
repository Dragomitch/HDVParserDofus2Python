package com.dofusretro.pricetracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for OpenAPI 3.0 (Swagger) documentation.
 * Configures API metadata and documentation settings.
 *
 * @author AGENT-API
 * @version 1.0
 * @since Wave 2
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures the OpenAPI specification for the REST API.
     *
     * @return the OpenAPI configuration
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dofus Retro Price Tracker API")
                        .version("1.0.0")
                        .description("REST API for tracking and analyzing Dofus Retro auction house prices. " +
                                "Provides endpoints for querying items, categories, and price history.")
                        .contact(new Contact()
                                .name("Dofus Retro Price Tracker Team")
                                .email("support@dofusretro-tracker.example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server"),
                        new Server()
                                .url("https://api.dofusretro-tracker.example.com")
                                .description("Production server")
                ));
    }
}
