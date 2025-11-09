package com.dofusretro.pricetracker.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Cache configuration properties for application-wide caching.
 *
 * Binds to 'dofus.retro.tracker.cache' in application.yml files.
 * Supports both Caffeine (in-memory) and Redis (distributed) caching.
 *
 * @author AGENT-INFRA
 * @since Wave 2
 */
@ConfigurationProperties(prefix = "dofus.retro.tracker.cache")
@Validated
public class CacheProperties {

    @NotNull
    @Min(value = 60, message = "Cache TTL must be at least 60 seconds")
    @Max(value = 86400, message = "Cache TTL must not exceed 86400 seconds (24 hours)")
    private Integer ttl = 3600;

    @NotNull
    @Min(value = 100, message = "Cache max size must be at least 100")
    @Max(value = 100000, message = "Cache max size must not exceed 100000")
    private Integer maxSize = 10000;

    private boolean enabled = true;

    private String type = "caffeine";  // caffeine or redis

    // Redis-specific properties
    private RedisProperties redis = new RedisProperties();

    // Getters and Setters
    public Integer getTtl() {
        return ttl;
    }

    public void setTtl(Integer ttl) {
        this.ttl = ttl;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public RedisProperties getRedis() {
        return redis;
    }

    public void setRedis(RedisProperties redis) {
        this.redis = redis;
    }

    /**
     * Redis cache configuration (optional).
     */
    public static class RedisProperties {
        private String host = "localhost";

        @Min(value = 1, message = "Redis port must be at least 1")
        @Max(value = 65535, message = "Redis port must not exceed 65535")
        private int port = 6379;

        private String password;

        @Min(value = 0, message = "Database index must be at least 0")
        @Max(value = 15, message = "Database index must not exceed 15")
        private int database = 0;

        @Min(value = 1000, message = "Timeout must be at least 1000ms")
        private long timeout = 5000L;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getDatabase() {
            return database;
        }

        public void setDatabase(int database) {
            this.database = database;
        }

        public long getTimeout() {
            return timeout;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }
    }
}
