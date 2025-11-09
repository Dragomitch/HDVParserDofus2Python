package com.dofusretro.pricetracker.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Configuration for async task execution.
 * <p>
 * This configuration sets up thread pools for:
 * <ul>
 *   <li><strong>packetProcessor</strong> - Background packet processing tasks</li>
 *   <li><strong>taskExecutor</strong> - General async operations</li>
 * </ul>
 * </p>
 * <p>
 * Thread pool sizing:
 * <ul>
 *   <li>Core pool size: minimum threads kept alive</li>
 *   <li>Max pool size: maximum threads when busy</li>
 *   <li>Queue capacity: pending tasks before rejection</li>
 * </ul>
 * </p>
 *
 * @author AGENT-BUSINESS
 * @version 1.0
 * @since Wave 2
 */
@Configuration
@EnableAsync
@Slf4j
public class TaskExecutorConfig {

    @Value("${dofus.retro.tracker.executor.core-pool-size:2}")
    private int corePoolSize;

    @Value("${dofus.retro.tracker.executor.max-pool-size:4}")
    private int maxPoolSize;

    @Value("${dofus.retro.tracker.executor.queue-capacity:100}")
    private int queueCapacity;

    @Value("${dofus.retro.tracker.executor.thread-name-prefix:DofusRetro-}")
    private String threadNamePrefix;

    @Value("${dofus.retro.tracker.executor.await-termination-seconds:30}")
    private int awaitTerminationSeconds;

    /**
     * Configure the packet processor executor.
     * <p>
     * This executor is used specifically for background packet processing tasks.
     * It has dedicated threads to ensure packet processing doesn't block
     * other application operations.
     * </p>
     *
     * @return configured packet processor executor
     */
    @Bean("packetProcessorExecutor")
    public ThreadPoolTaskExecutor packetProcessorExecutor() {
        log.info("Configuring packet processor executor");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Thread pool sizing
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);

        // Thread naming
        executor.setThreadNamePrefix("PacketProcessor-");

        // Rejection policy: CallerRuns (backup policy - run in caller thread)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // Graceful shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);

        // Initialize
        executor.initialize();

        log.info("Packet processor executor configured: " +
                        "corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                corePoolSize, maxPoolSize, queueCapacity);

        return executor;
    }

    /**
     * Configure the general task executor.
     * <p>
     * This executor is used for general async operations like cache updates,
     * scheduled tasks, and other background work.
     * </p>
     *
     * @return configured task executor
     */
    @Bean("taskExecutor")
    public Executor taskExecutor() {
        log.info("Configuring general task executor");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Thread pool sizing (smaller than packet processor)
        executor.setCorePoolSize(Math.max(1, corePoolSize - 1));
        executor.setMaxPoolSize(Math.max(2, maxPoolSize - 1));
        executor.setQueueCapacity(queueCapacity);

        // Thread naming
        executor.setThreadNamePrefix(threadNamePrefix);

        // Rejection policy: CallerRuns
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // Graceful shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);

        // Initialize
        executor.initialize();

        log.info("General task executor configured: " +
                        "corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), queueCapacity);

        return executor;
    }

    /**
     * Get executor statistics for monitoring.
     *
     * @param executor the thread pool task executor
     * @return formatted statistics string
     */
    public static String getExecutorStats(ThreadPoolTaskExecutor executor) {
        if (executor == null) {
            return "Executor not found";
        }

        var threadPool = executor.getThreadPoolExecutor();

        return String.format(
                "Executor: active=%d, poolSize=%d, coreSize=%d, maxSize=%d, " +
                        "queueSize=%d, completed=%d",
                threadPool.getActiveCount(),
                threadPool.getPoolSize(),
                threadPool.getCorePoolSize(),
                threadPool.getMaximumPoolSize(),
                threadPool.getQueue().size(),
                threadPool.getCompletedTaskCount()
        );
    }
}
