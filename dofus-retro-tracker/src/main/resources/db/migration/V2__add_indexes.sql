-- =====================================================
-- Dofus Retro Price Tracker - Performance Indexes
-- Version: V2
-- Description: Adds performance indexes for query optimization
-- Author: AGENT-DATA
-- Wave: 1, Track 1A
-- =====================================================

-- Index on items.item_gid for fast lookups by game ID
-- Used frequently when processing incoming price data
CREATE INDEX idx_item_gid ON items(item_gid);

-- Index on price_entries.created_at for time-based queries
-- Used for price history queries and data cleanup
CREATE INDEX idx_created_at ON price_entries(created_at);

-- Composite index on item_id and quantity for price lookups
-- Used when querying prices for a specific item and quantity
CREATE INDEX idx_item_quantity ON price_entries(item_id, quantity);

-- Index on items.sub_category_id for category-based queries
-- Used when filtering items by category
CREATE INDEX idx_item_category ON items(sub_category_id);

-- Composite index for price history queries
-- Optimizes queries that fetch recent prices for an item
CREATE INDEX idx_price_item_created ON price_entries(item_id, created_at DESC);

-- Update table statistics for query planner optimization
-- This helps PostgreSQL choose optimal query plans
ANALYZE sub_categories;
ANALYZE items;
ANALYZE price_entries;
