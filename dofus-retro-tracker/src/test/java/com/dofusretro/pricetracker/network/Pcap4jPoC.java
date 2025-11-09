package com.dofusretro.pricetracker.network;

import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;

import java.sql.Timestamp;
import java.util.List;

/**
 * Proof of Concept for Pcap4j packet capture.
 * This class demonstrates basic packet capture capabilities and serves as
 * a testing ground for understanding Pcap4j API.
 *
 * IMPORTANT: Requires appropriate permissions to capture packets:
 * - Linux: Run as root or set CAP_NET_RAW capability
 * - Windows: Run as Administrator with Npcap installed
 * - macOS: Run with sudo
 *
 * Usage: mvn exec:java -Dexec.mainClass="com.dofusretro.pricetracker.network.Pcap4jPoC"
 */
public class Pcap4jPoC {

    public static void main(String[] args) {
        System.out.println("=== Pcap4j Proof of Concept ===\n");

        try {
            // List all network interfaces
            List<PcapNetworkInterface> devices = Pcaps.findAllDevs();

            if (devices.isEmpty()) {
                System.err.println("No network interfaces found!");
                System.err.println("This might be a permissions issue. Try running with sudo/administrator privileges.");
                System.exit(1);
            }

            System.out.println("Available network interfaces:");
            for (int i = 0; i < devices.size(); i++) {
                PcapNetworkInterface dev = devices.get(i);
                System.out.printf("  [%d] %s", i, dev.getName());
                if (dev.getDescription() != null) {
                    System.out.printf(": %s", dev.getDescription());
                }
                System.out.println();

                // Show addresses if available
                if (!dev.getAddresses().isEmpty()) {
                    dev.getAddresses().forEach(addr ->
                        System.out.println("      Address: " + addr.getAddress())
                    );
                }
            }

            // Select first non-loopback interface
            PcapNetworkInterface nif = devices.stream()
                .filter(dev -> {
                    String name = dev.getName().toLowerCase();
                    return !name.contains("lo") &&
                           !name.contains("loopback") &&
                           !dev.getAddresses().isEmpty();
                })
                .findFirst()
                .orElse(devices.get(0)); // Fallback to first device

            System.out.println("\n=== Selected Interface ===");
            System.out.println("Name: " + nif.getName());
            if (nif.getDescription() != null) {
                System.out.println("Description: " + nif.getDescription());
            }

            // Open interface for capture
            int snapLen = 65536;  // Maximum bytes to capture per packet
            int timeout = 1000;   // Read timeout in milliseconds

            System.out.println("\n=== Opening Capture ===");
            System.out.println("Snapshot length: " + snapLen + " bytes");
            System.out.println("Timeout: " + timeout + " ms");

            PcapHandle handle = nif.openLive(
                snapLen,
                PcapNetworkInterface.PromiscuousMode.PROMISCUOUS,
                timeout
            );

            // Set BPF filter for Dofus Retro traffic
            // Default port 5555, but can be changed
            int dofusPort = 5555;
            String filter = String.format("tcp port %d", dofusPort);

            System.out.println("\n=== Setting BPF Filter ===");
            System.out.println("Filter: " + filter);
            System.out.println("(This will capture TCP traffic on port " + dofusPort + ")");

            try {
                handle.setFilter(filter, BpfCompileMode.OPTIMIZE);
                System.out.println("Filter applied successfully!");
            } catch (Exception e) {
                System.err.println("Warning: Could not set filter. Capturing all traffic.");
                System.err.println("Error: " + e.getMessage());
            }

            // Capture packets
            System.out.println("\n=== Starting Packet Capture ===");
            System.out.println("Capturing up to 10 packets (or press Ctrl+C to stop)...");
            System.out.println("NOTE: If no packets appear, ensure:");
            System.out.println("  1. Dofus client is running");
            System.out.println("  2. Client is connected to server on port " + dofusPort);
            System.out.println("  3. You have the correct permissions");
            System.out.println();

            PacketListener listener = new PacketListener() {
                private int packetCount = 0;

                @Override
                public void gotPacket(Packet packet) {
                    packetCount++;
                    System.out.println("--- Packet #" + packetCount + " ---");
                    System.out.println("  Total length: " + packet.length() + " bytes");
                    System.out.println("  Timestamp: " + new Timestamp(handle.getTimestamp().getTime()));

                    // Check if it's a TCP packet
                    if (packet.contains(TcpPacket.class)) {
                        TcpPacket tcp = packet.get(TcpPacket.class);
                        TcpPacket.TcpHeader header = tcp.getHeader();

                        System.out.println("  Protocol: TCP");
                        System.out.println("  Source Port: " + header.getSrcPort());
                        System.out.println("  Destination Port: " + header.getDstPort());
                        System.out.println("  Sequence Number: " + header.getSequenceNumber());
                        System.out.println("  Acknowledgment Number: " + header.getAcknowledgmentNumber());
                        System.out.println("  Flags: " +
                            (header.getSyn() ? "SYN " : "") +
                            (header.getAck() ? "ACK " : "") +
                            (header.getFin() ? "FIN " : "") +
                            (header.getRst() ? "RST " : "") +
                            (header.getPsh() ? "PSH " : ""));

                        // Extract payload
                        Packet payload = tcp.getPayload();
                        if (payload != null) {
                            byte[] data = payload.getRawData();
                            System.out.println("  Payload length: " + data.length + " bytes");

                            if (data.length > 0) {
                                // Show first 16 bytes in hex
                                System.out.print("  First bytes (hex): ");
                                int displayLen = Math.min(data.length, 16);
                                for (int i = 0; i < displayLen; i++) {
                                    System.out.printf("%02X ", data[i]);
                                }
                                System.out.println();
                            }
                        } else {
                            System.out.println("  Payload: None (control packet)");
                        }
                    } else {
                        System.out.println("  Protocol: " + packet.getClass().getSimpleName());
                    }
                    System.out.println();
                }
            };

            // Capture 10 packets (or until interrupted)
            try {
                handle.loop(10, listener);
            } catch (InterruptedException e) {
                System.out.println("\nCapture interrupted by user");
            }

            // Close handle
            handle.close();

            System.out.println("\n=== PoC Complete ===");
            System.out.println("Successfully demonstrated Pcap4j capabilities:");
            System.out.println("  [x] Listed network interfaces");
            System.out.println("  [x] Selected appropriate interface");
            System.out.println("  [x] Opened live capture");
            System.out.println("  [x] Applied BPF filter");
            System.out.println("  [x] Captured and parsed packets");
            System.out.println("  [x] Extracted TCP payload data");

        } catch (PcapNativeException e) {
            System.err.println("\nError: " + e.getMessage());
            System.err.println("\nPossible causes:");
            System.err.println("  1. Insufficient permissions (try running with sudo)");
            System.err.println("  2. libpcap not installed (install libpcap-dev on Linux)");
            System.err.println("  3. Npcap not installed (install Npcap on Windows)");
            System.err.println("  4. Network interface not available");
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("\nUnexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
