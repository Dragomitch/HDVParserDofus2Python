package com.dofusretro.pricetracker.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Configuration for HikariCP connection pooling.
 * <p>
 * HikariCP is the default connection pool in Spring Boot 2+, but this
 * configuration allows fine-tuning of pool parameters for optimal performance.
 * </p>
 * <p>
 * Key configurations:
 * <ul>
 *   <li><strong>maximumPoolSize</strong> - Max number of connections in pool</li>
 *   <li><strong>minimumIdle</strong> - Min number of idle connections</li>
 *   <li><strong>connectionTimeout</strong> - Max time to wait for connection</li>
 *   <li><strong>idleTimeout</strong> - Max time a connection can be idle</li>
 *   <li><strong>maxLifetime</strong> - Max lifetime of a connection</li>
 * </ul>
 * </p>
 * <p>
 * For high-throughput scenarios (like packet processing), proper connection
 * pool sizing is critical to avoid blocking and ensure good performance.
 * </p>
 *
 * @author AGENT-BUSINESS
 * @version 1.0
 * @since Wave 2
 */
@Configuration
@Slf4j
public class HikariConfig {

    @Value("${spring.datasource.hikari.maximum-pool-size:10}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:2}")
    private int minimumIdle;

    @Value("${spring.datasource.hikari.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.idle-timeout:600000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:1800000}")
    private long maxLifetime;

    @Value("${spring.datasource.hikari.pool-name:DofusRetroHikariPool}")
    private String poolName;

    /**
     * Configure data source properties.
     * <p>
     * This bean loads the standard Spring Boot datasource properties
     * from application.yml (spring.datasource.*).
     * </p>
     *
     * @return data source properties
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Configure HikariCP data source.
     * <p>
     * This creates a HikariDataSource with optimized settings for
     * the Dofus Retro Price Tracker workload.
     * </p>
     *
     * @param properties the data source properties
     * @return configured HikariCP data source
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource(DataSourceProperties properties) {
        log.info("Configuring HikariCP data source");

        // Create HikariDataSource from properties
        HikariDataSource dataSource = properties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();

        // Apply custom settings
        dataSource.setPoolName(poolName);
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setMinimumIdle(minimumIdle);
        dataSource.setConnectionTimeout(connectionTimeout);
        dataSource.setIdleTimeout(idleTimeout);
        dataSource.setMaxLifetime(maxLifetime);

        // Performance optimizations
        dataSource.addDataSourceProperty("cachePrepStmts", "true");
        dataSource.addDataSourceProperty("prepStmtCacheSize", "250");
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource.addDataSourceProperty("useServerPrepStmts", "true");
        dataSource.addDataSourceProperty("useLocalSessionState", "true");
        dataSource.addDataSourceProperty("rewriteBatchedStatements", "true");
        dataSource.addDataSourceProperty("cacheResultSetMetadata", "true");
        dataSource.addDataSourceProperty("cacheServerConfiguration", "true");
        dataSource.addDataSourceProperty("elideSetAutoCommits", "true");
        dataSource.addDataSourceProperty("maintainTimeStats", "false");

        log.info("HikariCP data source configured:");
        log.info("  Pool name: {}", poolName);
        log.info("  Maximum pool size: {}", maximumPoolSize);
        log.info("  Minimum idle: {}", minimumIdle);
        log.info("  Connection timeout: {}ms", connectionTimeout);
        log.info("  Idle timeout: {}ms", idleTimeout);
        log.info("  Max lifetime: {}ms", maxLifetime);

        return dataSource;
    }

    /**
     * Get connection pool statistics for monitoring.
     *
     * @param dataSource the data source
     * @return formatted statistics string
     */
    public static String getPoolStats(DataSource dataSource) {
        if (!(dataSource instanceof HikariDataSource hikariDataSource)) {
            return "Not a HikariDataSource";
        }

        var poolStats = hikariDataSource.getHikariPoolMXBean();

        return String.format(
                "Pool '%s': active=%d, idle=%d, total=%d, waiting=%d",
                hikariDataSource.getPoolName(),
                poolStats.getActiveConnections(),
                poolStats.getIdleConnections(),
                poolStats.getTotalConnections(),
                poolStats.getThreadsAwaitingConnection()
        );
    }
}
