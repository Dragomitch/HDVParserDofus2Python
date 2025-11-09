# Pcap4j Setup Guide

This guide explains how to set up and use Pcap4j for network packet capture in the Dofus Retro Price Tracker.

## Overview

Pcap4j is a Java library for capturing, crafting, and sending packets. It provides a wrapper around the native libpcap/WinPcap/Npcap libraries, which handle the low-level packet capture operations.

## System Requirements

### Linux
- **libpcap** installed (usually pre-installed on most distributions)
- **Appropriate permissions** to capture packets

### Windows
- **Npcap** or **WinPcap** installed
- **Administrator privileges** or appropriate permissions

### macOS
- **libpcap** (built-in)
- **Administrator privileges** (sudo)

## Installation

### Linux (Debian/Ubuntu)

```bash
# Install libpcap development files
sudo apt-get update
sudo apt-get install libpcap-dev

# Verify installation
ldconfig -p | grep pcap
```

### Linux (RedHat/CentOS/Fedora)

```bash
# Install libpcap
sudo yum install libpcap libpcap-devel

# Or on newer Fedora
sudo dnf install libpcap libpcap-devel
```

### Windows

1. Download and install Npcap from: https://npcap.com/
2. During installation, check "Install Npcap in WinPcap API-compatible mode"
3. Restart your computer after installation

### macOS

libpcap is pre-installed on macOS. No additional installation required.

## Required Permissions

### Linux - Option 1: Run as Root (Not Recommended for Production)

```bash
# Run application as root
sudo java -jar dofus-retro-tracker.jar

# Or with Maven
sudo mvn spring-boot:run
```

**Warning:** Running as root is a security risk. Use one of the other options for production deployments.

### Linux - Option 2: Set Capabilities (Recommended)

This approach grants specific capabilities to the Java binary without requiring full root access:

```bash
# Find your Java executable
which java

# Set capabilities (replace /path/to/java with actual path)
sudo setcap cap_net_raw,cap_net_admin=eip /path/to/java

# For OpenJDK, the path is typically:
sudo setcap cap_net_raw,cap_net_admin=eip /usr/lib/jvm/java-21-openjdk-amd64/bin/java

# Verify capabilities
getcap /path/to/java
# Should output: /path/to/java = cap_net_admin,cap_net_raw+eip
```

**Note:** This needs to be reapplied after Java updates.

### Linux - Option 3: Use AppArmor/SELinux Policies

Create a security policy that allows packet capture for your specific application:

```bash
# For AppArmor (Ubuntu/Debian)
# Create a profile in /etc/apparmor.d/

# For SELinux (RedHat/CentOS)
# Create a custom policy module
```

### Linux - Option 4: Add User to pcap Group

```bash
# Create pcap group if it doesn't exist
sudo groupadd pcap

# Add your user to the pcap group
sudo usermod -a -G pcap $USER

# Set permissions on libpcap
sudo chgrp pcap /usr/bin/tcpdump
sudo chmod 750 /usr/bin/tcpdump

# For dumpcap (used by Wireshark)
sudo chgrp pcap /usr/bin/dumpcap
sudo chmod 750 /usr/bin/dumpcap
sudo setcap cap_net_raw,cap_net_admin=eip /usr/bin/dumpcap

# Log out and log back in for group changes to take effect
```

### Windows

**Option 1: Run as Administrator**

Right-click the application and select "Run as Administrator"

**Option 2: Set Permanent Administrator Privileges**

1. Right-click the Java executable or application
2. Select Properties > Compatibility
3. Check "Run this program as an administrator"
4. Click OK

### macOS

```bash
# Run with sudo
sudo java -jar dofus-retro-tracker.jar

# Or with Maven
sudo mvn spring-boot:run
```

## Testing Your Setup

### Test 1: Verify Pcap4j Can Find Interfaces

Run the Proof of Concept class:

```bash
cd dofus-retro-tracker

# Linux/macOS (with sudo)
sudo mvn exec:java -Dexec.mainClass="com.dofusretro.pricetracker.network.Pcap4jPoC"

# Windows (as Administrator in cmd)
mvn exec:java -Dexec.mainClass="com.dofusretro.pricetracker.network.Pcap4jPoC"
```

Expected output should list your network interfaces:

```
=== Pcap4j Proof of Concept ===

Available network interfaces:
  [0] eth0: Ethernet interface
      Address: 192.168.1.100
  [1] wlan0: Wireless interface
      Address: 192.168.1.101
  [2] lo: Loopback interface
      Address: 127.0.0.1

=== Selected Interface ===
Name: eth0
Description: Ethernet interface
...
```

### Test 2: Verify BPF Filter Works

The PoC will attempt to capture TCP traffic on port 5555 (Dofus Retro default port). If you see packets being captured, your setup is working correctly.

## Configuration

The packet capture service can be configured in `application.yml`:

```yaml
packet:
  capture:
    enabled: true                 # Enable/disable packet capture
    dofus-port: 5555             # Dofus Retro server port
    network-interface: null       # Auto-detect or specify (e.g., "eth0")
    snap-len: 65536              # Maximum bytes per packet
    timeout: 1000                # Read timeout (ms)
    queue-capacity: 1000         # Packet queue size
    queue-timeout: 100           # Queue offer timeout (ms)
```

## Troubleshooting

### Error: "No network interfaces found"

**Cause:** Insufficient permissions or libpcap not installed.

**Solutions:**
- Linux: Run with sudo or set capabilities (see above)
- Windows: Install Npcap and run as Administrator
- macOS: Run with sudo

### Error: "Permission denied"

**Cause:** User doesn't have permission to capture packets.

**Solutions:**
- Check permissions (see "Required Permissions" section above)
- Verify you're in the pcap group: `groups $USER`
- Try running with sudo to confirm it's a permission issue

### Error: "org.pcap4j.core.PcapNativeException: The operation is not permitted"

**Cause:** Java doesn't have the necessary capabilities.

**Solutions:**
- Set capabilities on Java binary (Option 2 above)
- Run as root/administrator
- Check SELinux/AppArmor policies

### Error: "No suitable device found"

**Cause:** All interfaces are loopback or no interfaces have IP addresses.

**Solutions:**
- Check network interfaces: `ip addr` (Linux) or `ipconfig` (Windows)
- Connect to a network
- Specify interface manually in `application.yml`

### Warning: "Packet queue full, dropping packets"

**Cause:** Packets are arriving faster than they can be processed.

**Solutions:**
- Increase `queue-capacity` in configuration
- Optimize packet processing code
- Check if protocol parser is keeping up

### No Packets Being Captured

**Possible causes:**
1. Dofus client not running
2. Client not connected to server
3. Wrong port configured (check `dofus-port` setting)
4. Wrong network interface selected
5. BPF filter too restrictive

**Debug steps:**
1. Verify Dofus is running and connected
2. Check network traffic with: `sudo tcpdump -i any tcp port 5555`
3. Try capturing on all ports: Set BPF filter to empty in config
4. Check logs for error messages

### Java Updates Break Capabilities

**Cause:** Capabilities are set on the Java binary, which changes when Java is updated.

**Solution:**
- Re-run the setcap command after Java updates
- Consider creating a wrapper script that checks and resets capabilities
- Use Option 4 (pcap group) which is more persistent

## Security Considerations

1. **Minimize Privileges:** Use capabilities instead of running as root
2. **Restrict Access:** Only grant packet capture permissions to necessary users
3. **Network Isolation:** Run capture on isolated network interface if possible
4. **Audit Logs:** Monitor who has packet capture privileges
5. **Data Protection:** Captured packets may contain sensitive information

## Performance Tuning

### Optimize Snapshot Length

Set `snap-len` to the minimum required:
- Dofus packets are typically < 2000 bytes
- Default 65536 captures entire packet
- Consider setting to 2048 for better performance

### Adjust Queue Capacity

- Larger queue = more memory usage but less packet drops
- Monitor queue size via actuator endpoint
- Adjust based on traffic volume and processing speed

### BPF Filters

- More specific filters = better performance
- Filter at kernel level (BPF) vs application level
- Example: `tcp port 5555 and (tcp[tcpflags] & tcp-push != 0)`

## References

- Pcap4j Documentation: https://www.pcap4j.org/
- libpcap Documentation: https://www.tcpdump.org/
- Npcap Project: https://npcap.com/
- BPF Filter Syntax: https://www.tcpdump.org/manpages/pcap-filter.7.html

## Next Steps

After setting up Pcap4j:
1. Run the PoC to verify your setup
2. Start the application with packet capture enabled
3. Monitor the health endpoint: `http://localhost:8080/actuator/health`
4. Check logs for any warnings or errors
5. Verify packets are being captured and queued
