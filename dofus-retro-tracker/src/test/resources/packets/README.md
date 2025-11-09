# Test Packet Fixtures

This directory contains test packet data for protocol parser testing.

## File Format

- `.bin` files: Raw binary packet data
- `.txt` files: Hex dump with annotations
- `.json` files: Expected parsed output

## Packets

### hdv-price-wheat.bin

**Description:** HDV price message for Wheat (item GID 289)

**Contents:**
- Item: Wheat (GID 289)
- Category: Cereals (48)
- Prices:
  - Quantity 1: 15,000 kamas
  - Quantity 10: 140,000 kamas
  - Quantity 100: 1,300,000 kamas

**Message Type:** ExchangeTypesItemsExchangerDescriptionForUserMessage

### hdv-price-multiple.bin

**Description:** HDV price message with multiple items (Wheat, Barley, Oats)

**Contents:**
- 3 items from Cereals category
- Each with prices for quantities 1, 10, 100

### hdv-category.bin

**Description:** HDV category message for Cereals

**Contents:**
- Category ID: 48 (Cereals)

## Usage in Tests

```java
@Test
void testWithRealPacket() throws IOException {
    byte[] packet = Files.readAllBytes(
        Paths.get("src/test/resources/packets/hdv-price-wheat.bin")
    );

    ParsedMessage result = parser.parse(packet);
    assertThat(result).isNotNull();
}
```

## Generating Test Packets

Test packets can be generated using:
1. Actual Wireshark captures (when available)
2. `PacketTestDataGenerator.java` utility class
3. Manual hex editing based on protocol specification

## Hex Dump Format

Example annotation in `.txt` files:

```
17 04          # Header: message ID 5892 (0x1704 >> 2), length=1 byte
0A             # Payload length: 10 bytes
01             # Item count: 1
A1 02          # Item GID: 289 (Wheat, VarInt)
30             # Category: 48 (Cereals, VarInt)
03             # Price count: 3
B8 EA 01       # Price qty=1: 15000 (VarLong)
C0 A2 11       # Price qty=10: 140000 (VarLong)
C0 D2 9E 01    # Price qty=100: 1300000 (VarLong)
```

## Notes

- Message IDs are placeholders until confirmed with real Dofus Retro captures
- VarInt/VarLong encoding follows Dofus protocol specification
- All multi-byte values use big-endian (network byte order)
