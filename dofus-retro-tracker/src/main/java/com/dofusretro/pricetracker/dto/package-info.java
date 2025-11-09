/**
 * Data Transfer Objects (DTOs) for API communication.
 *
 * <p>This package contains DTO classes used for:
 * <ul>
 *   <li>API request/response payloads</li>
 *   <li>Data validation</li>
 *   <li>Decoupling API contracts from domain models</li>
 *   <li>Projections for specific use cases</li>
 * </ul>
 *
 * <p>DTOs include:
 * <ul>
 *   <li>ItemDTO - Item data transfer</li>
 *   <li>PriceSnapshotDTO - Price data transfer</li>
 *   <li>MarketListingDTO - Market listing transfer</li>
 *   <li>PriceStatisticsDTO - Aggregated price statistics</li>
 *   <li>ErrorResponseDTO - Error response structure</li>
 * </ul>
 *
 * <p>All DTOs use Jackson for JSON serialization and include validation annotations.
 *
 * @since 0.1.0
 */
package com.dofusretro.pricetracker.dto;
