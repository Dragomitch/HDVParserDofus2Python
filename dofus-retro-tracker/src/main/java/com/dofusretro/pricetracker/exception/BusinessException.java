package com.dofusretro.pricetracker.exception;

/**
 * Base exception for business logic errors in the Dofus Retro Price Tracker.
 * <p>
 * This exception is thrown when business rules are violated or business
 * operations fail in a way that is expected and should be handled gracefully.
 * </p>
 * <p>
 * Examples of business exceptions:
 * <ul>
 *   <li>Invalid price data (negative values, out of range)</li>
 *   <li>Duplicate item registration attempts</li>
 *   <li>Circuit breaker open (service unavailable)</li>
 *   <li>Batch processing failures</li>
 * </ul>
 * </p>
 *
 * @author AGENT-BUSINESS
 * @version 1.0
 * @since Wave 2
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Error code for categorizing the exception.
     */
    private final String errorCode;

    /**
     * Constructs a new business exception with the specified detail message.
     *
     * @param message the detail message
     */
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
    }

    /**
     * Constructs a new business exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
    }

    /**
     * Constructs a new business exception with error code, message, and cause.
     *
     * @param errorCode a categorization code for the error
     * @param message   the detail message
     * @param cause     the cause of the exception
     */
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new business exception with error code and message.
     *
     * @param errorCode a categorization code for the error
     * @param message   the detail message
     */
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Gets the error code associated with this exception.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Creates a business exception for invalid price data.
     *
     * @param itemGid  the item GID
     * @param price    the invalid price
     * @param reason   the reason it's invalid
     * @return a BusinessException
     */
    public static BusinessException invalidPriceData(int itemGid, long price, String reason) {
        return new BusinessException(
                "INVALID_PRICE_DATA",
                String.format("Invalid price data for item %d: price=%d, reason=%s",
                        itemGid, price, reason)
        );
    }

    /**
     * Creates a business exception for database operation failures.
     *
     * @param operation the operation that failed
     * @param cause     the underlying cause
     * @return a BusinessException
     */
    public static BusinessException databaseError(String operation, Throwable cause) {
        return new BusinessException(
                "DATABASE_ERROR",
                String.format("Database operation failed: %s", operation),
                cause
        );
    }

    /**
     * Creates a business exception for circuit breaker open state.
     *
     * @param serviceName the service that is unavailable
     * @return a BusinessException
     */
    public static BusinessException circuitBreakerOpen(String serviceName) {
        return new BusinessException(
                "CIRCUIT_BREAKER_OPEN",
                String.format("Service %s is currently unavailable (circuit breaker open)", serviceName)
        );
    }

    /**
     * Creates a business exception for batch processing failures.
     *
     * @param batchSize     the size of the failed batch
     * @param successCount  the number of items that succeeded
     * @param failureCount  the number of items that failed
     * @return a BusinessException
     */
    public static BusinessException batchProcessingFailed(int batchSize, int successCount, int failureCount) {
        return new BusinessException(
                "BATCH_PROCESSING_FAILED",
                String.format("Batch processing completed with failures: total=%d, success=%d, failed=%d",
                        batchSize, successCount, failureCount)
        );
    }
}
