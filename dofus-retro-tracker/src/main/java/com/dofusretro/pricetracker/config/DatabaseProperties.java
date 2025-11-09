package com.dofusretro.pricetracker.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Database and connection pool configuration properties.
 *
 * Binds to Spring's datasource properties and provides validation.
 * Optimized for HikariCP connection pooling.
 *
 * @author AGENT-INFRA
 * @since Wave 2
 */
@ConfigurationProperties(prefix = "spring.datasource")
@Validated
public class DatabaseProperties {

    @NotBlank(message = "Database URL must not be blank")
    private String url;

    @NotBlank(message = "Database username must not be blank")
    private String username;

    private String password;

    private HikariProperties hikari = new HikariProperties();

    // Getters and Setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public HikariProperties getHikari() {
        return hikari;
    }

    public void setHikari(HikariProperties hikari) {
        this.hikari = hikari;
    }

    /**
     * HikariCP connection pool configuration.
     */
    public static class HikariProperties {

        @NotNull
        @Min(value = 1, message = "Maximum pool size must be at least 1")
        @Max(value = 100, message = "Maximum pool size must not exceed 100")
        private Integer maximumPoolSize = 10;

        @NotNull
        @Min(value = 0, message = "Minimum idle must be at least 0")
        private Integer minimumIdle = 2;

        @NotNull
        @Min(value = 1000, message = "Connection timeout must be at least 1000ms")
        @Max(value = 300000, message = "Connection timeout must not exceed 300000ms")
        private Long connectionTimeout = 30000L;

        @NotNull
        @Min(value = 60000, message = "Idle timeout must be at least 60000ms")
        private Long idleTimeout = 600000L;

        @NotNull
        @Min(value = 60000, message = "Max lifetime must be at least 60000ms")
        private Long maxLifetime = 1800000L;

        @Min(value = 0, message = "Leak detection threshold must be at least 0")
        private Long leakDetectionThreshold = 0L;

        // Getters and Setters
        public Integer getMaximumPoolSize() {
            return maximumPoolSize;
        }

        public void setMaximumPoolSize(Integer maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
        }

        public Integer getMinimumIdle() {
            return minimumIdle;
        }

        public void setMinimumIdle(Integer minimumIdle) {
            this.minimumIdle = minimumIdle;
        }

        public Long getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(Long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public Long getIdleTimeout() {
            return idleTimeout;
        }

        public void setIdleTimeout(Long idleTimeout) {
            this.idleTimeout = idleTimeout;
        }

        public Long getMaxLifetime() {
            return maxLifetime;
        }

        public void setMaxLifetime(Long maxLifetime) {
            this.maxLifetime = maxLifetime;
        }

        public Long getLeakDetectionThreshold() {
            return leakDetectionThreshold;
        }

        public void setLeakDetectionThreshold(Long leakDetectionThreshold) {
            this.leakDetectionThreshold = leakDetectionThreshold;
        }
    }
}
