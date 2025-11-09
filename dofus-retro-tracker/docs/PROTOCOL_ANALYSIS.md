# Dofus Retro Protocol Analysis

**Date:** 2025-11-09
**Analysis Source:** LaBot Python implementation reference
**Protocol Version:** Dofus 2.x (Compatible with Dofus Retro)

## Executive Summary

This document provides a comprehensive analysis of the Dofus Retro network protocol, specifically focusing on the Auction House (HDV - Hôtel des Ventes) message structure. The analysis is based on the existing Python implementation (LaBot) and serves as the foundation for the Java protocol parser implementation.

---

## 1. Connection Details

### Network Configuration
- **Server Port:** 5555 (TCP)
- **Protocol:** Custom binary protocol over TCP
- **Byte Order:** Big-endian (network byte order)
- **Encryption:** None (plaintext TCP)
- **Compression:** Optional (NetworkDataContainerMessage with zlib)

### Communication Model
- Bidirectional TCP stream
- Client → Server: Messages with sequence counter
- Server → Client: Messages without sequence counter

---

## 2. Message Structure

### 2.1 Message Header Format

Every Dofus message follows this structure:

```
┌─────────────────────────────────────────────────────────────┐
│                        MESSAGE HEADER                        │
├──────────────┬──────────────┬──────────────┬────────────────┤
│   Header     │    Count     │   Length     │    Payload     │
│   (2 bytes)  │ (4 bytes)    │ (1-3 bytes)  │  (N bytes)     │
└──────────────┴──────────────┴──────────────┴────────────────┘
```

#### Header Byte (2 bytes / 16 bits)
```
Bits:  15 14 13 12 11 10 09 08 | 07 06 05 04 03 02 01 00
       ├─────────────────────┤   ├─────────────────┤ ├──┤
              Message ID              Message ID     Len
              (high bits)              (low bits)   Bits
```

- **Bits 2-15:** Message ID (14 bits, values 0-16383)
- **Bits 0-1:** Length encoding (2 bits)
  - `00` = 0 bytes (empty message)
  - `01` = 1 byte length field
  - `10` = 2 byte length field
  - `11` = 3 byte length field

**Calculation:**
```java
int header = readUnsignedShort();
int messageId = header >> 2;          // Extract bits 2-15
int lengthBytes = header & 0x03;      // Extract bits 0-1
```

#### Count Field (4 bytes, client messages only)
- **Present:** Only in client → server messages
- **Type:** Unsigned 32-bit integer
- **Purpose:** Sequence counter for message ordering
- **Note:** Server → client messages do NOT include this field

#### Length Field (1-3 bytes)
- **Type:** Unsigned integer (big-endian)
- **Bytes:** Determined by header bits 0-1
- **Value:** Exact byte count of payload

#### Payload (N bytes)
- **Length:** Specified by length field
- **Format:** Message-specific binary data
- **Encoding:** Variable-length integers (VarInt), strings, nested structures

### 2.2 Example Message Breakdown

**Hex dump:**
```
12 34 00 00 00 0F 05 A3 02 ...
```

**Parsing:**
```
Header:     0x1234 = 0001 0010 0011 0100 (binary)
  Message ID:   (0x1234 >> 2) = 0x48D = 1165
  Length bits:  (0x1234 & 0x03) = 0 (no length encoding)

Count:      0x0000000F = 15 (message sequence number)

Length:     0x05 = 5 bytes (payload length)

Payload:    0xA3 0x02 ... (5 bytes of message data)
```

---

## 3. Variable-Length Integer Encoding (VarInt)

Dofus uses **variable-length encoding** for integers to save bandwidth. This is similar to Protocol Buffers' VarInt encoding.

### 3.1 Encoding Rules

Each byte in a VarInt has:
- **Bit 7:** Continuation bit (1 = more bytes follow, 0 = last byte)
- **Bits 0-6:** Data bits (7 bits per byte)

### 3.2 Reading Algorithm

```java
public int readVarInt() {
    int result = 0;
    int shift = 0;
    byte b;

    do {
        b = readByte();
        result |= (b & 0x7F) << shift;  // Extract 7 data bits
        shift += 7;
    } while ((b & 0x80) != 0);  // Check continuation bit

    return result;
}
```

### 3.3 Examples

**Value: 1**
```
Encoded: 0x01
Binary:  0000 0001
         ^      ^^^
         |      └─ Data (1)
         └─ No continuation
```

**Value: 300**
```
Decimal: 300
Binary:  0000 0001 0010 1100 (9 bits needed)

Encoded: 0xAC 0x02
  Byte 1: 1010 1100 = 0x AC
          ^    ^^^^
          |    └─ Lower 7 bits: 010 1100 (44)
          └─ Continuation bit: 1 (more bytes)

  Byte 2: 0000 0010 = 0x02
          ^    ^^^^
          |    └─ Upper 2 bits: 000 0010 (2)
          └─ Continuation bit: 0 (last byte)

Reconstruction:
  44 + (2 << 7) = 44 + 256 = 300
```

**Value: 16384**
```
Encoded: 0x80 0x80 0x01

  Byte 1: 1000 0000 (bits 0-6: 0)
  Byte 2: 1000 0000 (bits 7-13: 0)
  Byte 3: 0000 0001 (bits 14-20: 1)

  Result: 0 + (0 << 7) + (1 << 14) = 16384
```

### 3.4 Variants

- **VarInt:** 32-bit signed/unsigned integers
- **VarShort:** 16-bit integers
- **VarLong:** 64-bit integers
- **VarUhInt/VarUhShort/VarUhLong:** Unsigned variants (same encoding)

---

## 4. HDV-Specific Messages

### 4.1 ExchangeTypesItemsExchangerDescriptionForUserMessage

**Purpose:** Sent by server when client views auction house items and prices.

**Message Type:** `ExchangeTypesItemsExchangerDescriptionForUserMessage`

**Structure:**
```json
{
  "__type__": "ExchangeTypesItemsExchangerDescriptionForUserMessage",
  "itemTypeDescriptions": [
    {
      "objectGID": 289,           // Item Global ID (e.g., Wheat)
      "objectType": 48,            // Category ID (e.g., Cereals)
      "prices": [15000, 140000, 1300000]  // Prices for qty [1, 10, 100]
    },
    {
      "objectGID": 290,
      "objectType": 48,
      "prices": [20000, 180000, 1600000]
    }
    // ... more items
  ]
}
```

**Binary Encoding (Payload):**
```
┌────────────────────────────────────────────┐
│          itemTypeDescriptions              │
├────────────────────────────────────────────┤
│  Count (VarInt)                            │
├────────────────────────────────────────────┤
│  ┌──────────────────────────────────────┐  │
│  │ Item 1                               │  │
│  │  - objectGID (VarInt)                │  │
│  │  - objectType (VarInt)               │  │
│  │  - prices count (VarInt) = 3         │  │
│  │  - price[0] (VarLong)                │  │
│  │  - price[1] (VarLong)                │  │
│  │  - price[2] (VarLong)                │  │
│  └──────────────────────────────────────┘  │
│  ┌──────────────────────────────────────┐  │
│  │ Item 2                               │  │
│  │  ...                                 │  │
│  └──────────────────────────────────────┘  │
└────────────────────────────────────────────┘
```

**Example Parsing:**
```java
int itemCount = reader.readVarInt();

for (int i = 0; i < itemCount; i++) {
    int objectGID = reader.readVarInt();
    int objectType = reader.readVarInt();

    int priceCount = reader.readVarInt();
    long[] prices = new long[priceCount];
    for (int j = 0; j < priceCount; j++) {
        prices[j] = reader.readVarLong();
    }

    // Create price data for qty 1, 10, 100
    for (int j = 0; j < prices.length; j++) {
        int quantity = (int)Math.pow(10, j);
        createPriceEntry(objectGID, quantity, prices[j]);
    }
}
```

### 4.2 Price Interpretation

**Quantity Mapping:**
- `prices[0]` → Price for **1 item**
- `prices[1]` → Price for **10 items**
- `prices[2]` → Price for **100 items**

**Price Value:**
- Unit: Kamas (in-game currency)
- 0 value means: Not available for that quantity

**Example:**
```json
"prices": [15000, 140000, 1300000]
```
Means:
- 1 item costs 15,000 kamas
- 10 items cost 140,000 kamas (14,000 each)
- 100 items cost 1,300,000 kamas (13,000 each)

### 4.3 Item Categories

Common category IDs (from Dofus data):
- `48` - Cereals (Wheat, Barley, Oats, etc.)
- `49` - Vegetables
- `50` - Fruits
- `51` - Fish
- `52` - Meats
- `53` - Woods
- `54` - Ores
- `55` - Flowers
- And many more...

---

## 5. String Encoding

### UTF-8 String Format

Strings in Dofus protocol use:
1. **Length prefix:** VarInt or UnsignedShort (varies by field)
2. **UTF-8 encoded bytes**

**Reading:**
```java
public String readUTF() {
    int length = readUnsignedShort();  // or readVarInt()
    byte[] bytes = readBytes(length);
    return new String(bytes, StandardCharsets.UTF_8);
}
```

**Example:**
```
Length: 0x0004 (4 bytes)
Bytes:  0x74 0x65 0x73 0x74 ("test")
```

---

## 6. Compression

### NetworkDataContainerMessage (ID: 2)

Some messages are compressed using **zlib** to reduce bandwidth.

**Detection:**
```java
if (messageId == 2) {
    // This is a compressed container
    byte[] compressedData = reader.readByteArray();
    byte[] uncompressed = decompress(compressedData);

    // Parse the inner message
    BinaryReader innerReader = new BinaryReader(uncompressed);
    return parseMessage(innerReader);
}
```

---

## 7. Implementation Notes

### 7.1 Java Translation

**Python (LaBot)** → **Java (Spring Boot)**

| Python | Java |
|--------|------|
| `Data` class | `BinaryReader` utility |
| `readVarInt()` | `readVarInt()` |
| `readUnsignedShort()` | `readShort() & 0xFFFF` |
| `read(n)` | `readBytes(n)` |
| `protocol.read()` | `DofusRetroProtocolParser.parse()` |

### 7.2 Testing Strategy

1. **Unit tests:** VarInt encoding/decoding
2. **Sample packets:** Binary fixtures from captures
3. **Mock messages:** Programmatically generated test data
4. **Integration tests:** End-to-end parsing

### 7.3 Error Handling

**Common issues:**
- **BufferUnderflowException:** Incomplete packet data
- **Unknown message ID:** Unsupported or new message type
- **Invalid VarInt:** Corrupted data or wrong protocol version

---

## 8. Reference Message IDs

Based on Dofus 2.x protocol (to be confirmed for Dofus Retro):

| ID | Message Name | Description |
|----|-------------|-------------|
| 2 | NetworkDataContainerMessage | Compressed message container |
| 110 | AuthenticationTicketMessage | Login authentication |
| 5904 | ExchangeTypesItemsExchangerDescriptionForUserMessage | HDV items and prices |
| 5905 | ExchangeTypesExchangerDescriptionForUserMessage | HDV categories |

**Note:** Message IDs may vary between Dofus 2 and Dofus Retro. The exact IDs should be determined from actual packet captures or protocol definition files.

---

## 9. Next Steps

### 9.1 Implementation Checklist

- [x] Document protocol structure
- [x] Analyze VarInt encoding
- [x] Identify HDV message format
- [ ] Implement BinaryReader utility
- [ ] Create message definition classes
- [ ] Implement protocol parser
- [ ] Write comprehensive tests
- [ ] Capture real packets (if possible)
- [ ] Validate against live data

### 9.2 Open Questions

1. **Exact Message IDs:** Need to confirm IDs for Dofus Retro (may differ from Dofus 2)
2. **Protocol Version:** Which version of Dofus Retro is targeted?
3. **Additional Messages:** Are there other HDV-related messages we need to support?
4. **Authentication:** How do we handle login/session messages?

### 9.3 Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Protocol changes | High | Version detection, graceful degradation |
| Unknown messages | Medium | Log unknown IDs, skip parsing |
| Incomplete packets | Medium | Buffer management, reassembly logic |
| Performance | Low | Efficient ByteBuffer usage, minimal allocations |

---

## 10. References

### Code References
- **Python LaBot:** `/home/user/HDVParserDofus2Python/labot/`
  - `protocol.py` - Message parsing logic
  - `data/binrw.py` - Binary reader/writer (Data class)
  - `data/msg.py` - Message wrapper and framing
  - `mitm/MessageParser.py` - HDV message extraction

### External Documentation
- Dofus Protocol Documentation: Community reverse-engineering efforts
- Protocol Buffers VarInt: Similar encoding scheme
- Java NIO ByteBuffer: Efficient binary data handling

---

## Appendix A: Sample Packet (Hypothetical)

```
Hex dump of ExchangeTypesItemsExchangerDescriptionForUserMessage:

17 04 1A                          // Header: msg_id=5892, len=3 bytes
00 00 02 00                       // Count: 512 (client sequence)
0A                                // Length: 10 bytes payload
02                                // itemTypeDescriptions count: 2 items
  A1 02                           // objectGID: 289 (VarInt)
  30                              // objectType: 48 (VarInt)
  03                              // prices count: 3
    B8 EA 01                      // price[0]: 15000 (VarLong)
    C0 A2 11                      // price[1]: 140000 (VarLong)
    C0 D2 9E 01                   // price[2]: 1300000 (VarLong)
  A2 02                           // objectGID: 290
  30                              // objectType: 48
  03                              // prices count: 3
    ...
```

---

**Document Version:** 1.0
**Last Updated:** 2025-11-09
**Author:** AGENT-PROTOCOL
