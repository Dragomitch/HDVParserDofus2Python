package com.dofusretro.pricetracker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for the REST API.
 * Configures CORS settings to allow Angular 20 frontend access.
 *
 * @author AGENT-API
 * @version 1.0
 * @since Wave 2
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures CORS mappings to allow cross-origin requests from the Angular 20 frontend.
     *
     * @param registry the CORS registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:4200",  // Angular dev server (default)
                        "http://localhost:4201",  // Angular alternate port
                        "https://dofusretro-tracker.example.com"  // Production frontend
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
