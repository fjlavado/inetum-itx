-- V4__migrate_prices_to_timelines.sql
-- Description: Migrates data from row-per-price pattern to CQRS aggregate pattern
--
-- This migration transforms the 'prices' table (one row per pricing rule) into
-- 'product_price_timelines' table (one row per product+brand with JSONB array).
--
-- Strategy:
-- 1. Group all price rows by (product_id, brand_id)
-- 2. Aggregate into JSONB array
-- 3. Insert into new table
-- 4. Validate migration success
-- 5. Keep old table for rollback safety

-- Migrate all existing prices to the new JSONB format
INSERT INTO product_price_timelines (product_id, brand_id, price_rules, created_at, updated_at)
SELECT 
    product_id,
    brand_id,
    jsonb_agg(
        jsonb_build_object(
            'priceListId', jsonb_build_object('value', price_list),
            'startDate', to_char(start_date, 'YYYY-MM-DD"T"HH24:MI:SS'),
            'endDate', to_char(end_date, 'YYYY-MM-DD"T"HH24:MI:SS'),
            'priority', jsonb_build_object('value', priority),
            'amount', jsonb_build_object('amount', price)
        )
        ORDER BY priority DESC, start_date ASC -- Keep highest priority first for potential optimizations
    ) AS price_rules,
    MIN(created_at) AS created_at,
    MAX(updated_at) AS updated_at
FROM prices
GROUP BY product_id, brand_id;

-- Validation: Ensure row counts match
-- This block will raise an exception if the migration failed
DO $$
DECLARE
    old_product_brand_count INTEGER;
    new_count INTEGER;
    old_total_rules INTEGER;
    new_total_rules INTEGER;
BEGIN
    -- Count distinct product+brand combinations in old table
    SELECT COUNT(DISTINCT (product_id, brand_id)) INTO old_product_brand_count 
    FROM prices;
    
    -- Count rows in new table
    SELECT COUNT(*) INTO new_count 
    FROM product_price_timelines;
    
    -- Validate product+brand combination count
    IF old_product_brand_count != new_count THEN
        RAISE EXCEPTION 'Migration validation failed: Expected % product+brand combinations, got %', 
            old_product_brand_count, new_count;
    END IF;
    
    -- Count total pricing rules in old table
    SELECT COUNT(*) INTO old_total_rules 
    FROM prices;
    
    -- Count total pricing rules in new table (sum of JSONB array lengths)
    SELECT SUM(jsonb_array_length(price_rules)) INTO new_total_rules 
    FROM product_price_timelines;
    
    -- Validate total rule count
    IF old_total_rules != new_total_rules THEN
        RAISE EXCEPTION 'Migration validation failed: Expected % total rules, got %', 
            old_total_rules, new_total_rules;
    END IF;
    
    RAISE NOTICE 'Migration successful:';
    RAISE NOTICE '  - % product+brand combinations migrated', new_count;
    RAISE NOTICE '  - % total pricing rules migrated', new_total_rules;
    RAISE NOTICE '  - Old "prices" table retained for rollback safety';
END $$;

-- Note: We intentionally do NOT drop the old 'prices' table yet
-- This allows for:
-- 1. Easy rollback if issues are discovered
-- 2. Performance comparison between old and new approaches
-- 3. Gradual migration with feature flags (if needed)
--
-- To drop the old table after validating the new implementation:
-- DROP TABLE prices CASCADE;

-- Create a view for backwards compatibility (optional)
-- This allows old queries to continue working during transition period
-- Uncomment if gradual migration is needed:
--
-- CREATE OR REPLACE VIEW prices_legacy AS
-- SELECT 
--     row_number() OVER () AS id,
--     ppt.product_id,
--     ppt.brand_id,
--     (rule->>'priceListId')::jsonb->>'value' AS price_list,
--     (rule->>'startDate')::timestamp AS start_date,
--     (rule->>'endDate')::timestamp AS end_date,
--     (rule->>'priority')::jsonb->>'value' AS priority,
--     (rule->>'amount')::jsonb->>'amount' AS price,
--     ppt.created_at,
--     ppt.updated_at
-- FROM product_price_timelines ppt,
--      jsonb_array_elements(ppt.price_rules) AS rule;
