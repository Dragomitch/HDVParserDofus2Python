/**
 * Network protocol handling and packet parsing.
 *
 * <p>This package contains classes for:
 * <ul>
 *   <li>Capturing network packets from Dofus Retro client</li>
 *   <li>Parsing Dofus protocol messages</li>
 *   <li>Extracting market data from network traffic</li>
 *   <li>Binary data reading and interpretation</li>
 * </ul>
 *
 * <p>Key components:
 * <ul>
 *   <li>PacketCapture - pcap4j wrapper for packet capture</li>
 *   <li>PacketParser - Dofus protocol parser</li>
 *   <li>BinaryReader - Binary data reading utilities</li>
 *   <li>ProtocolMessage - Base class for protocol messages</li>
 *   <li>MarketDataExtractor - Extract market data from packets</li>
 * </ul>
 *
 * <p>Based on the original Python implementation using labot protocol definitions.
 *
 * @since 0.1.0
 */
package com.dofusretro.pricetracker.protocol;
