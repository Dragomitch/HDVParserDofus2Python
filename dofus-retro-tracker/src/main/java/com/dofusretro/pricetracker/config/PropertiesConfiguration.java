package com.dofusretro.pricetracker.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to enable all @ConfigurationProperties beans.
 *
 * This ensures that all property classes are scanned and validated on startup.
 * Any validation errors will prevent the application from starting.
 *
 * @author AGENT-INFRA
 * @since Wave 2
 */
@Configuration
@EnableConfigurationProperties({
    AppProperties.class,
    DatabaseProperties.class,
    CacheProperties.class
})
public class PropertiesConfiguration {
    // Properties will be automatically registered as Spring beans
}
