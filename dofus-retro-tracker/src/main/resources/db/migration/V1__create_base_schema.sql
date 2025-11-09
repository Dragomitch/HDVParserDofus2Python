-- =====================================================
-- Dofus Retro Price Tracker - Base Schema
-- Version: V1
-- Description: Creates the core database tables for tracking
--              item prices from the Dofus Retro auction house
-- Author: AGENT-DATA
-- Wave: 1, Track 1A
-- =====================================================

-- Create sub_categories table
-- Stores Dofus item sub-categories (e.g., Cereals, Fish, Minerals)
CREATE TABLE sub_categories (
    id BIGSERIAL PRIMARY KEY,
    dofus_id INTEGER UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create items table
-- Stores Dofus items that can be traded in the auction house
CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    item_gid INTEGER UNIQUE NOT NULL,
    item_name VARCHAR(255),
    sub_category_id BIGINT REFERENCES sub_categories(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create price_entries table
-- Stores historical price data for items at different quantities
CREATE TABLE price_entries (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    price BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    server_timestamp BIGINT
);

-- Add table documentation comments
COMMENT ON TABLE sub_categories IS 'Dofus item sub-categories (e.g., Cereals, Fish)';
COMMENT ON TABLE items IS 'Dofus items tracked in the auction house';
COMMENT ON TABLE price_entries IS 'Historical price data for items at different quantities';

-- Add column documentation comments for key fields
COMMENT ON COLUMN sub_categories.dofus_id IS 'Unique identifier from Dofus game category system';
COMMENT ON COLUMN items.item_gid IS 'Game Item ID - unique identifier from Dofus game protocol';
COMMENT ON COLUMN price_entries.quantity IS 'Quantity tier: 1, 10, or 100 units';
COMMENT ON COLUMN price_entries.server_timestamp IS 'Server timestamp from Dofus game server (milliseconds since epoch)';
