-- =====================================================
-- Dofus Retro Price Tracker - Data Integrity Constraints
-- Version: V3
-- Description: Adds CHECK constraints and unique indexes for data validation
-- Author: AGENT-DATA
-- Wave: 1, Track 1A
-- =====================================================

-- =====================================================
-- SUB_CATEGORIES Constraints
-- =====================================================

-- Ensure dofus_id is positive
ALTER TABLE sub_categories
    ADD CONSTRAINT chk_dofus_id_positive CHECK (dofus_id > 0);

-- Ensure name is not empty (only whitespace)
ALTER TABLE sub_categories
    ADD CONSTRAINT chk_name_not_empty CHECK (length(trim(name)) > 0);

-- =====================================================
-- ITEMS Constraints
-- =====================================================

-- Ensure item_gid is positive
ALTER TABLE items
    ADD CONSTRAINT chk_item_gid_positive CHECK (item_gid > 0);

-- Ensure item_name, if present, is not empty
ALTER TABLE items
    ADD CONSTRAINT chk_item_name_length CHECK (
        item_name IS NULL OR length(trim(item_name)) > 0
    );

-- =====================================================
-- PRICE_ENTRIES Constraints
-- =====================================================

-- Ensure price is positive
ALTER TABLE price_entries
    ADD CONSTRAINT chk_price_positive CHECK (price > 0);

-- Ensure quantity is one of the valid values (1, 10, or 100)
-- These are the quantity tiers shown in the Dofus auction house
ALTER TABLE price_entries
    ADD CONSTRAINT chk_quantity_valid CHECK (quantity IN (1, 10, 100));

-- Ensure server_timestamp is positive if present
ALTER TABLE price_entries
    ADD CONSTRAINT chk_server_timestamp_positive CHECK (
        server_timestamp IS NULL OR server_timestamp > 0
    );

-- =====================================================
-- Deduplication Constraints
-- =====================================================

-- Prevent duplicate price entries within a short time window
-- This unique index prevents saving the same price multiple times
-- within 10 minutes, which helps avoid data duplication from
-- repeated captures of the same auction house state
CREATE UNIQUE INDEX idx_unique_price_entry ON price_entries (
    item_id,
    quantity,
    price,
    date_trunc('minute', created_at)
) WHERE created_at >= NOW() - INTERVAL '10 minutes';

COMMENT ON INDEX idx_unique_price_entry IS 'Prevents duplicate price entries within 10-minute windows';
