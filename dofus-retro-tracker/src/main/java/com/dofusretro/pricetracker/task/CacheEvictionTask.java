package com.dofusretro.pricetracker.task;

import com.dofusretro.pricetracker.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled background task for cache management and eviction.
 * <p>
 * This task runs periodically to:
 * <ul>
 *   <li>Monitor cache usage and statistics</li>
 *   <li>Log cache hit rates and eviction counts</li>
 *   <li>Perform manual cache cleanup if needed</li>
 *   <li>Detect and alert on cache performance issues</li>
 * </ul>
 * </p>
 * <p>
 * While Caffeine handles automatic eviction based on TTL and size limits,
 * this task provides visibility into cache behavior and can trigger
 * manual cleanup operations.
 * </p>
 *
 * @author AGENT-BUSINESS
 * @version 1.0
 * @since Wave 2
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        value = "dofus.retro.tracker.cache.monitoring.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class CacheEvictionTask {

    private final CacheManager cacheManager;

    @Value("${dofus.retro.tracker.cache.monitoring.log-stats:true}")
    private boolean logStats;

    @Value("${dofus.retro.tracker.cache.monitoring.hit-rate-warning-threshold:0.5}")
    private double hitRateWarningThreshold;

    /**
     * Monitor cache statistics.
     * <p>
     * This method runs on a fixed rate schedule (default: 5 minutes).
     * It logs cache statistics and alerts on performance issues.
     * </p>
     */
    @Scheduled(fixedRateString = "${dofus.retro.tracker.cache.monitoring.interval-ms:300000}")
    public void monitorCacheStatistics() {
        if (!logStats) {
            return;
        }

        try {
            log.info("=== Cache Statistics ===");

            // Get all cache names
            var cacheNames = getCacheNames();

            for (String cacheName : cacheNames) {
                String stats = CacheConfig.getCacheStats(cacheManager, cacheName);
                log.info(stats);

                // Check hit rate
                checkCacheHitRate(cacheName);
            }

            log.info("========================");

        } catch (Exception e) {
            log.error("Error monitoring cache statistics", e);
        }
    }

    /**
     * Perform periodic cache cleanup.
     * <p>
     * This method runs less frequently (default: 1 hour) to perform
     * manual cleanup operations if needed. Currently, Caffeine handles
     * automatic eviction, so this is mainly a placeholder for future
     * custom cleanup logic.
     * </p>
     */
    @Scheduled(fixedRateString = "${dofus.retro.tracker.cache.cleanup.interval-ms:3600000}")
    public void cleanupCaches() {
        try {
            log.debug("Starting cache cleanup");

            // Get all caches
            var cacheNames = getCacheNames();

            for (String cacheName : cacheNames) {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    // Caffeine automatically handles eviction
                    // We could add custom cleanup logic here if needed
                    log.trace("Cache '{}' cleanup (automatic eviction active)", cacheName);
                }
            }

            log.debug("Cache cleanup complete");

        } catch (Exception e) {
            log.error("Error during cache cleanup", e);
        }
    }

    /**
     * Clear all caches manually.
     * <p>
     * This method can be called on demand (e.g., via REST API) to
     * completely clear all caches. Useful for troubleshooting or
     * after configuration changes.
     * </p>
     */
    public void clearAllCaches() {
        log.warn("Clearing all caches manually");

        try {
            var cacheNames = getCacheNames();
            for (String cacheName : cacheNames) {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    log.info("Cleared cache: {}", cacheName);
                }
            }
            log.info("All caches cleared successfully");

        } catch (Exception e) {
            log.error("Error clearing caches", e);
        }
    }

    /**
     * Clear a specific cache.
     *
     * @param cacheName the cache name to clear
     * @return true if cache was cleared, false if not found
     */
    public boolean clearCache(String cacheName) {
        log.info("Clearing cache: {}", cacheName);

        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("Cache '{}' cleared successfully", cacheName);
            return true;
        }

        log.warn("Cache '{}' not found", cacheName);
        return false;
    }

    /**
     * Get statistics for a specific cache.
     *
     * @param cacheName the cache name
     * @return formatted statistics string
     */
    public String getCacheStatistics(String cacheName) {
        return CacheConfig.getCacheStats(cacheManager, cacheName);
    }

    /**
     * Get all cache names.
     *
     * @return list of cache names
     */
    public List<String> getCacheNames() {
        return cacheManager.getCacheNames().stream().sorted().toList();
    }

    /**
     * Check cache hit rate and alert if below threshold.
     *
     * @param cacheName the cache to check
     */
    private void checkCacheHitRate(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return;
        }

        // Get native Caffeine cache
        var nativeCache = cache.getNativeCache();
        if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache) {
            var caffeineCache = (com.github.benmanes.caffeine.cache.Cache<?, ?>) nativeCache;
            var stats = caffeineCache.stats();

            double hitRate = stats.hitRate();

            // Alert if hit rate is below threshold and we have enough samples
            long totalRequests = stats.hitCount() + stats.missCount();
            if (totalRequests > 100 && hitRate < hitRateWarningThreshold) {
                log.warn("Cache '{}' has low hit rate: {:.2f}% (threshold: {:.2f}%)",
                        cacheName, hitRate * 100, hitRateWarningThreshold * 100);
            }
        }
    }

    /**
     * Get cache size estimate.
     *
     * @param cacheName the cache name
     * @return estimated size, or -1 if not available
     */
    public long getCacheSize(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return -1;
        }

        var nativeCache = cache.getNativeCache();
        if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache) {
            var caffeineCache = (com.github.benmanes.caffeine.cache.Cache<?, ?>) nativeCache;
            return caffeineCache.estimatedSize();
        }

        return -1;
    }
}
