package com.dofusretro.pricetracker.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Results in HTTP 404 Not Found response.
 *
 * @author AGENT-API
 * @version 1.0
 * @since Wave 2
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a ResourceNotFoundException for an item not found by ID.
     *
     * @param id the item ID
     * @return the exception instance
     */
    public static ResourceNotFoundException forItem(Long id) {
        return new ResourceNotFoundException("Item with ID " + id + " not found");
    }

    /**
     * Creates a ResourceNotFoundException for a category not found by ID.
     *
     * @param id the category ID
     * @return the exception instance
     */
    public static ResourceNotFoundException forCategory(Long id) {
        return new ResourceNotFoundException("Category with ID " + id + " not found");
    }
}
