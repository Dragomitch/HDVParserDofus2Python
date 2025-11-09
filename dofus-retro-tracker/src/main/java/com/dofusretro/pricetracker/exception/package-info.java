/**
 * Custom exception classes and global exception handling.
 *
 * <p>This package contains:
 * <ul>
 *   <li>Custom exception types for domain-specific errors</li>
 *   <li>Global exception handler (@ControllerAdvice)</li>
 *   <li>Error response builders</li>
 * </ul>
 *
 * <p>Exception hierarchy:
 * <ul>
 *   <li>PriceTrackerException - Base exception</li>
 *   <li>ItemNotFoundException - Item not found</li>
 *   <li>InvalidPriceDataException - Invalid price data</li>
 *   <li>PacketCaptureException - Packet capture errors</li>
 *   <li>AutomationException - GUI automation errors</li>
 *   <li>DataValidationException - Data validation errors</li>
 * </ul>
 *
 * <p>All exceptions include proper HTTP status codes and error messages.
 *
 * @since 0.1.0
 */
package com.dofusretro.pricetracker.exception;
