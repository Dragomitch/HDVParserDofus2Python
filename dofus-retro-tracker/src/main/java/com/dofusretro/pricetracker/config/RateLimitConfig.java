package com.dofusretro.pricetracker.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration for API rate limiting using Bucket4j.
 * Provides token bucket rate limiting to prevent API abuse.
 *
 * @author AGENT-API
 * @version 1.0
 * @since Wave 2
 */
@Configuration
public class RateLimitConfig {

    /**
     * Creates a cache for storing rate limit buckets per client IP.
     *
     * @return the bucket cache
     */
    @Bean
    public ConcurrentHashMap<String, Bucket> rateLimitBuckets() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Creates a default rate limit bucket configuration.
     * Allows 100 requests per minute per client.
     *
     * @return the rate limit bucket
     */
    @Bean
    public Bucket defaultRateLimitBucket() {
        // Allow 100 requests per minute
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Gets or creates a rate limit bucket for a specific client.
     *
     * @param clientId the client identifier (usually IP address)
     * @param buckets  the bucket cache
     * @return the bucket for this client
     */
    public Bucket resolveBucket(String clientId, ConcurrentHashMap<String, Bucket> buckets) {
        return buckets.computeIfAbsent(clientId, k -> createNewBucket());
    }

    /**
     * Creates a new rate limit bucket with default configuration.
     *
     * @return the new bucket
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
