package com.dofusretro.pricetracker.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for Spring Cache with Caffeine.
 * <p>
 * This configuration sets up caching for frequently accessed data:
 * <ul>
 *   <li><strong>items</strong> - Item entities by itemGid (long TTL)</li>
 *   <li><strong>itemsWithPrices</strong> - Items with price history (medium TTL)</li>
 *   <li><strong>latestPrices</strong> - Latest prices by item and quantity (short TTL)</li>
 * </ul>
 * </p>
 * <p>
 * Caffeine provides:
 * <ul>
 *   <li>Time-based expiration (TTL)</li>
 *   <li>Size-based eviction</li>
 *   <li>Weak/soft reference support</li>
 *   <li>Excellent performance</li>
 * </ul>
 * </p>
 *
 * @author AGENT-BUSINESS
 * @version 1.0
 * @since Wave 2
 */
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    @Value("${dofus.retro.tracker.cache.ttl:3600}")
    private int defaultTtlSeconds;

    @Value("${dofus.retro.tracker.cache.max-size:10000}")
    private int defaultMaxSize;

    @Value("${dofus.retro.tracker.cache.items.ttl:7200}")
    private int itemsCacheTtl;

    @Value("${dofus.retro.tracker.cache.items.max-size:5000}")
    private int itemsCacheMaxSize;

    @Value("${dofus.retro.tracker.cache.latest-prices.ttl:300}")
    private int latestPricesTtl;

    @Value("${dofus.retro.tracker.cache.latest-prices.max-size:10000}")
    private int latestPricesMaxSize;

    /**
     * Configure the cache manager with Caffeine.
     * <p>
     * This creates a cache manager with default settings that can be
     * customized per cache name.
     * </p>
     *
     * @return configured cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        log.info("Configuring Caffeine cache manager");

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Default cache configuration
        cacheManager.setCaffeine(defaultCacheBuilder());

        // Register cache names
        cacheManager.setCacheNames(
                java.util.List.of(
                        "items",
                        "itemsWithPrices",
                        "latestPrices"
                )
        );

        log.info("Cache manager configured with {} caches", 3);
        log.info("  - items: TTL={}s, maxSize={}", itemsCacheTtl, itemsCacheMaxSize);
        log.info("  - itemsWithPrices: TTL={}s, maxSize={}", defaultTtlSeconds, defaultMaxSize);
        log.info("  - latestPrices: TTL={}s, maxSize={}", latestPricesTtl, latestPricesMaxSize);

        return cacheManager;
    }

    /**
     * Build default Caffeine cache configuration.
     *
     * @return Caffeine builder with default settings
     */
    private Caffeine<Object, Object> defaultCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(defaultTtlSeconds, TimeUnit.SECONDS)
                .maximumSize(defaultMaxSize)
                .recordStats()  // Enable statistics
                .removalListener((key, value, cause) -> {
                    log.trace("Cache entry removed: key={}, cause={}", key, cause);
                });
    }

    /**
     * Build Caffeine cache for items.
     * <p>
     * Items cache has a longer TTL since item metadata changes infrequently.
     * </p>
     *
     * @return Caffeine builder for items cache
     */
    @Bean("itemsCaffeine")
    public Caffeine<Object, Object> itemsCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(itemsCacheTtl, TimeUnit.SECONDS)
                .maximumSize(itemsCacheMaxSize)
                .recordStats()
                .removalListener((key, value, cause) -> {
                    log.trace("Item cache entry removed: key={}, cause={}", key, cause);
                });
    }

    /**
     * Build Caffeine cache for latest prices.
     * <p>
     * Latest prices cache has a shorter TTL since prices change frequently.
     * </p>
     *
     * @return Caffeine builder for latest prices cache
     */
    @Bean("latestPricesCaffeine")
    public Caffeine<Object, Object> latestPricesCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(latestPricesTtl, TimeUnit.SECONDS)
                .maximumSize(latestPricesMaxSize)
                .recordStats()
                .removalListener((key, value, cause) -> {
                    log.trace("Latest price cache entry removed: key={}, cause={}", key, cause);
                });
    }

    /**
     * Get cache statistics for monitoring.
     * <p>
     * This method can be used by monitoring endpoints to expose cache metrics.
     * </p>
     *
     * @param cacheManager the cache manager
     * @param cacheName    the cache name
     * @return formatted statistics string
     */
    public static String getCacheStats(CacheManager cacheManager, String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return "Cache not found: " + cacheName;
        }

        // Get native Caffeine cache
        var nativeCache = cache.getNativeCache();
        if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache) {
            var caffeineCache = (com.github.benmanes.caffeine.cache.Cache<?, ?>) nativeCache;
            var stats = caffeineCache.stats();

            return String.format(
                    "Cache '%s': hits=%d, misses=%d, hitRate=%.2f%%, evictions=%d, size=%d",
                    cacheName,
                    stats.hitCount(),
                    stats.missCount(),
                    stats.hitRate() * 100,
                    stats.evictionCount(),
                    caffeineCache.estimatedSize()
            );
        }

        return "Stats not available for cache: " + cacheName;
    }
}
