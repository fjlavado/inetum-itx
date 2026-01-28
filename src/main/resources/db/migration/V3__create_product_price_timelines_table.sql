-- V3__create_product_price_timelines_table.sql
-- Description: Creates the CQRS aggregate table with JSONB storage for pricing rules
-- 
-- This migration implements the new CQRS pattern where all pricing rules for a
-- product+brand combination are stored as a single JSONB document instead of
-- multiple rows. This enables O(1) database lookups and in-memory filtering.

-- Main table: One row per product+brand with all pricing rules as JSONB
CREATE TABLE product_price_timelines (
    product_id BIGINT NOT NULL,
    brand_id BIGINT NOT NULL,
    price_rules JSONB NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Composite primary key for O(1) lookups
    PRIMARY KEY (product_id, brand_id),
    
    -- Ensure at least one pricing rule exists
    CONSTRAINT check_price_rules_not_empty 
        CHECK (jsonb_array_length(price_rules) > 0)
);

-- GIN index on JSONB column for advanced JSON queries
-- (future-proofing for potential queries like "find all products with priority > 1")
CREATE INDEX idx_product_price_timelines_rules_gin 
    ON product_price_timelines USING GIN (price_rules);

-- B-tree index on brand_id for queries filtering by brand
CREATE INDEX idx_product_price_timelines_brand 
    ON product_price_timelines (brand_id);

-- Partial index for recently updated timelines (performance optimization)
CREATE INDEX idx_product_price_timelines_recent 
    ON product_price_timelines (updated_at DESC)
    WHERE updated_at > CURRENT_TIMESTAMP - INTERVAL '7 days';

-- Documentation comments
COMMENT ON TABLE product_price_timelines IS 
    'CQRS aggregate: All pricing rules for a product stored as JSONB for O(1) lookup. Replaces row-per-price pattern.';

COMMENT ON COLUMN product_price_timelines.product_id IS 
    'Product identifier (part of composite PK)';

COMMENT ON COLUMN product_price_timelines.brand_id IS 
    'Brand identifier (part of composite PK)';

COMMENT ON COLUMN product_price_timelines.price_rules IS 
    'JSONB array of pricing rules with structure: [{priceListId, startDate, endDate, priority, amount}]';

COMMENT ON COLUMN product_price_timelines.version IS 
    'Optimistic locking version to prevent concurrent update conflicts';

COMMENT ON COLUMN product_price_timelines.created_at IS 
    'Timestamp when this timeline was first created';

COMMENT ON COLUMN product_price_timelines.updated_at IS 
    'Timestamp of last modification (used for cache invalidation)';
