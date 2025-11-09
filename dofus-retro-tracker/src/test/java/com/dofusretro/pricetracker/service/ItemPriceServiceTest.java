package com.dofusretro.pricetracker.service;

import com.dofusretro.pricetracker.exception.BusinessException;
import com.dofusretro.pricetracker.exception.ParsingException;
import com.dofusretro.pricetracker.model.Item;
import com.dofusretro.pricetracker.model.PriceEntry;
import com.dofusretro.pricetracker.protocol.MessageDefinitions.ExchangeTypesItemsMessage;
import com.dofusretro.pricetracker.protocol.MessageDefinitions.ItemTypeDescription;
import com.dofusretro.pricetracker.protocol.MessageDefinitions.PriceData;
import com.dofusretro.pricetracker.protocol.ParsedMessage;
import com.dofusretro.pricetracker.repository.ItemRepository;
import com.dofusretro.pricetracker.repository.PriceEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ItemPriceService.
 *
 * @author AGENT-BUSINESS
 * @version 1.0
 * @since Wave 2
 */
@ExtendWith(MockitoExtension.class)
class ItemPriceServiceTest {

    @Mock
    private DofusRetroProtocolParser parser;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private PriceEntryRepository priceEntryRepository;

    @InjectMocks
    private ItemPriceService service;

    private byte[] testPacket;
    private Item testItem;
    private ParsedMessage testMessage;
    private List<PriceData> testPriceData;

    @BeforeEach
    void setUp() {
        testPacket = new byte[]{0x01, 0x02, 0x03};

        testItem = Item.builder()
                .id(1L)
                .itemGid(289)
                .itemName("Wheat")
                .build();

        // Create test price data
        testPriceData = List.of(
                new PriceData(289, 48, 1, 15000L, Instant.now()),
                new PriceData(289, 48, 10, 140000L, Instant.now()),
                new PriceData(289, 48, 100, 1300000L, Instant.now())
        );

        // Create test message
        ItemTypeDescription itemDesc = new ItemTypeDescription(
                289, 48, new long[]{15000L, 140000L, 1300000L}
        );
        ExchangeTypesItemsMessage payload = new ExchangeTypesItemsMessage(
                5904, List.of(itemDesc), Instant.now()
        );
        testMessage = ParsedMessage.builder()
                .messageId(5904)
                .payload(payload)
                .rawData(testPacket)
                .build();
    }

    @Test
    void testProcessPacket_Success() {
        // Arrange
        when(parser.parse(testPacket)).thenReturn(testMessage);
        when(parser.extractPriceData(testMessage)).thenReturn(testPriceData);
        when(itemRepository.findByItemGid(289)).thenReturn(Optional.empty());
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);
        when(priceEntryRepository.saveAll(anyList())).thenReturn(List.of(
                PriceEntry.builder().build(),
                PriceEntry.builder().build(),
                PriceEntry.builder().build()
        ));

        // Act
        int persisted = service.processPacket(testPacket);

        // Assert
        assertThat(persisted).isEqualTo(3);
        verify(parser).parse(testPacket);
        verify(parser).extractPriceData(testMessage);
        verify(itemRepository).findByItemGid(289);
        verify(priceEntryRepository).saveAll(anyList());
    }

    @Test
    void testProcessPacket_NullPacket() {
        // Act
        int persisted = service.processPacket(null);

        // Assert
        assertThat(persisted).isZero();
        verifyNoInteractions(parser);
    }

    @Test
    void testProcessPacket_EmptyPacket() {
        // Act
        int persisted = service.processPacket(new byte[0]);

        // Assert
        assertThat(persisted).isZero();
        verifyNoInteractions(parser);
    }

    @Test
    void testProcessPacket_ParserReturnsNull() {
        // Arrange
        when(parser.parse(testPacket)).thenReturn(null);

        // Act
        int persisted = service.processPacket(testPacket);

        // Assert
        assertThat(persisted).isZero();
        verify(parser).parse(testPacket);
        verifyNoMoreInteractions(parser);
    }

    @Test
    void testProcessPacket_ParsingError() {
        // Arrange
        ParsedMessage errorMessage = ParsedMessage.builder()
                .messageId(-1)
                .rawData(testPacket)
                .errorMessage("Parse error")
                .build();
        when(parser.parse(testPacket)).thenReturn(errorMessage);

        // Act & Assert
        assertThatThrownBy(() -> service.processPacket(testPacket))
                .isInstanceOf(ParsingException.class)
                .hasMessageContaining("Parse error");
    }

    @Test
    void testProcessPacket_NoPriceData() {
        // Arrange
        ParsedMessage nonPriceMessage = ParsedMessage.builder()
                .messageId(100)
                .rawData(testPacket)
                .build();
        when(parser.parse(testPacket)).thenReturn(nonPriceMessage);

        // Act
        int persisted = service.processPacket(testPacket);

        // Assert
        assertThat(persisted).isZero();
        verify(parser).parse(testPacket);
        verify(parser, never()).extractPriceData(any());
    }

    @Test
    void testProcessPacketBatch_Success() {
        // Arrange
        List<byte[]> packets = List.of(testPacket, testPacket);
        when(parser.parse(testPacket)).thenReturn(testMessage);
        when(parser.extractPriceData(testMessage)).thenReturn(testPriceData);
        when(itemRepository.findByItemGid(289)).thenReturn(Optional.of(testItem));
        when(priceEntryRepository.saveAll(anyList())).thenReturn(List.of(
                PriceEntry.builder().build(),
                PriceEntry.builder().build(),
                PriceEntry.builder().build()
        ));

        // Act
        int persisted = service.processPacketBatch(packets);

        // Assert
        assertThat(persisted).isEqualTo(6);  // 3 prices * 2 packets
        verify(parser, times(2)).parse(testPacket);
    }

    @Test
    void testProcessPacketBatch_EmptyList() {
        // Act
        int persisted = service.processPacketBatch(List.of());

        // Assert
        assertThat(persisted).isZero();
        verifyNoInteractions(parser);
    }

    @Test
    void testProcessPacketBatch_PartialFailure() {
        // Arrange
        byte[] goodPacket = testPacket;
        byte[] badPacket = new byte[]{0x99};

        when(parser.parse(goodPacket)).thenReturn(testMessage);
        when(parser.parse(badPacket)).thenThrow(new RuntimeException("Parse error"));
        when(parser.extractPriceData(testMessage)).thenReturn(testPriceData);
        when(itemRepository.findByItemGid(289)).thenReturn(Optional.of(testItem));
        when(priceEntryRepository.saveAll(anyList())).thenReturn(List.of(
                PriceEntry.builder().build(),
                PriceEntry.builder().build(),
                PriceEntry.builder().build()
        ));

        List<byte[]> packets = List.of(goodPacket, badPacket);

        // Act
        int persisted = service.processPacketBatch(packets);

        // Assert
        assertThat(persisted).isEqualTo(3);  // Only the good packet
        verify(parser).parse(goodPacket);
        verify(parser).parse(badPacket);
    }

    @Test
    void testProcessPacketBatch_AllFailures() {
        // Arrange
        when(parser.parse(any())).thenThrow(new RuntimeException("Parse error"));

        List<byte[]> packets = List.of(testPacket, testPacket);

        // Act & Assert
        assertThatThrownBy(() -> service.processPacketBatch(packets))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Batch processing completed with failures");
    }

    @Test
    void testGetOrCreateItem_ExistingItem() {
        // Arrange
        when(itemRepository.findByItemGid(289)).thenReturn(Optional.of(testItem));

        // Act
        Item result = service.getOrCreateItem(289);

        // Assert
        assertThat(result).isEqualTo(testItem);
        verify(itemRepository).findByItemGid(289);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void testGetOrCreateItem_NewItem() {
        // Arrange
        when(itemRepository.findByItemGid(290)).thenReturn(Optional.empty());
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

        // Act
        Item result = service.getOrCreateItem(290);

        // Assert
        assertThat(result).isNotNull();
        verify(itemRepository).findByItemGid(290);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void testPersistPriceData_Success() {
        // Arrange
        when(itemRepository.findByItemGid(289)).thenReturn(Optional.of(testItem));
        when(priceEntryRepository.saveAll(anyList())).thenReturn(List.of(
                PriceEntry.builder().build(),
                PriceEntry.builder().build(),
                PriceEntry.builder().build()
        ));

        // Act
        int persisted = service.persistPriceData(testPriceData);

        // Assert
        assertThat(persisted).isEqualTo(3);
        verify(priceEntryRepository).saveAll(argThat(list -> list.size() == 3));
    }

    @Test
    void testPersistPriceData_EmptyList() {
        // Act
        int persisted = service.persistPriceData(List.of());

        // Assert
        assertThat(persisted).isZero();
        verifyNoInteractions(priceEntryRepository);
    }

    @Test
    void testPersistPriceData_InvalidPrice_Negative() {
        // Arrange
        List<PriceData> invalidData = List.of(
                new PriceData(289, 48, 1, -1000L, Instant.now())
        );

        when(itemRepository.findByItemGid(289)).thenReturn(Optional.of(testItem));
        when(priceEntryRepository.saveAll(anyList())).thenReturn(List.of());

        // Act
        int persisted = service.persistPriceData(invalidData);

        // Assert - Invalid data should be skipped
        assertThat(persisted).isZero();
    }

    @Test
    void testPersistPriceData_InvalidPrice_Zero() {
        // Arrange
        List<PriceData> zeroPrice = List.of(
                new PriceData(289, 48, 1, 0L, Instant.now())
        );

        when(itemRepository.findByItemGid(289)).thenReturn(Optional.of(testItem));
        when(priceEntryRepository.saveAll(anyList())).thenReturn(List.of());

        // Act
        int persisted = service.persistPriceData(zeroPrice);

        // Assert - Zero prices should be skipped
        assertThat(persisted).isZero();
    }

    @Test
    void testPersistPriceData_InvalidQuantity() {
        // Arrange
        List<PriceData> invalidQuantity = List.of(
                new PriceData(289, 48, 50, 1000L, Instant.now())  // Invalid quantity
        );

        when(itemRepository.findByItemGid(289)).thenReturn(Optional.of(testItem));
        when(priceEntryRepository.saveAll(anyList())).thenReturn(List.of());

        // Act
        int persisted = service.persistPriceData(invalidQuantity);

        // Assert - Invalid quantities should be skipped
        assertThat(persisted).isZero();
    }

    @Test
    void testGetItemByGid_Found() {
        // Arrange
        when(itemRepository.findByItemGid(289)).thenReturn(Optional.of(testItem));

        // Act
        Optional<Item> result = service.getItemByGid(289);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testItem);
    }

    @Test
    void testGetItemByGid_NotFound() {
        // Arrange
        when(itemRepository.findByItemGid(999)).thenReturn(Optional.empty());

        // Act
        Optional<Item> result = service.getItemByGid(999);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testGetLatestPrice() {
        // Arrange
        PriceEntry priceEntry = PriceEntry.builder()
                .item(testItem)
                .price(15000L)
                .quantity(1)
                .build();

        when(itemRepository.findByItemGid(289)).thenReturn(Optional.of(testItem));
        when(priceEntryRepository.findTopByItemAndQuantityOrderByCreatedAtDesc(testItem, 1))
                .thenReturn(Optional.of(priceEntry));

        // Act
        Optional<PriceEntry> result = service.getLatestPrice(289, 1);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getPrice()).isEqualTo(15000L);
    }

    @Test
    void testGetPriceHistory() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        List<PriceEntry> history = List.of(
                PriceEntry.builder().price(15000L).build(),
                PriceEntry.builder().price(16000L).build()
        );

        when(itemRepository.findByItemGid(289)).thenReturn(Optional.of(testItem));
        when(priceEntryRepository.findByItemAndQuantityAndCreatedAtBetween(
                testItem, 1, start, end)).thenReturn(history);

        // Act
        List<PriceEntry> result = service.getPriceHistory(289, 1, start, end);

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void testGetStatistics() {
        // Arrange
        when(itemRepository.count()).thenReturn(100L);
        when(priceEntryRepository.count()).thenReturn(5000L);

        // Act
        String stats = service.getStatistics();

        // Assert
        assertThat(stats).contains("100");
        assertThat(stats).contains("5000");
    }
}
