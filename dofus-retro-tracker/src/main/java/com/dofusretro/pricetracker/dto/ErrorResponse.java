package com.dofusretro.pricetracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized error response format for API exceptions.
 * Provides consistent error information across all endpoints.
 *
 * @author AGENT-API
 * @version 1.0
 * @since Wave 2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized error response containing error details")
public class ErrorResponse {

    @Schema(description = "Timestamp when the error occurred")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Error type/reason", example = "Not Found")
    private String error;

    @Schema(description = "Detailed error message", example = "Item with ID 999 not found")
    private String message;

    @Schema(description = "API path where error occurred", example = "/api/v1/items/999")
    private String path;

    @Schema(description = "List of validation errors (if applicable)")
    private List<ValidationError> validationErrors;

    /**
     * Nested class for validation error details.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Details of a validation error")
    public static class ValidationError {
        @Schema(description = "Field name that failed validation", example = "itemGid")
        private String field;

        @Schema(description = "Rejected value")
        private Object rejectedValue;

        @Schema(description = "Validation error message", example = "must not be null")
        private String message;
    }
}
