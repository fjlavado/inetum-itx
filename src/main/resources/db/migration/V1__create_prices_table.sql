-- V1__create_prices_table.sql
-- Description: Creates the prices table with proper indexing for query optimization

CREATE TABLE prices (
    id BIGSERIAL PRIMARY KEY,
    brand_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    price_list INTEGER NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    priority INTEGER NOT NULL,
    price NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'EUR',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT check_price_positive CHECK (price >= 0),
    CONSTRAINT check_priority_non_negative CHECK (priority >= 0),
    CONSTRAINT check_date_range CHECK (start_date < end_date)
);

-- Index for the primary query pattern: lookup by brand_id, product_id, and date range
-- This composite index dramatically improves query performance for the main use case
CREATE INDEX idx_prices_lookup 
    ON prices (brand_id, product_id, start_date, end_date);

-- Index for priority sorting when multiple prices match
CREATE INDEX idx_prices_priority 
    ON prices (priority DESC);

-- Composite index for complete query optimization including priority
CREATE INDEX idx_prices_complete_lookup 
    ON prices (brand_id, product_id, start_date, end_date, priority DESC);

-- Comments for documentation
COMMENT ON TABLE prices IS 'Stores pricing information for products with temporal validity and priority-based conflict resolution';
COMMENT ON COLUMN prices.brand_id IS 'Identifier for the brand (e.g., 1 = ZARA)';
COMMENT ON COLUMN prices.product_id IS 'Identifier for the product';
COMMENT ON COLUMN prices.price_list IS 'Identifier for the applicable price list';
COMMENT ON COLUMN prices.start_date IS 'Start date and time when this price becomes effective';
COMMENT ON COLUMN prices.end_date IS 'End date and time when this price expires';
COMMENT ON COLUMN prices.priority IS 'Priority for conflict resolution - higher values win when multiple prices overlap';
COMMENT ON COLUMN prices.price IS 'The final price amount';
COMMENT ON COLUMN prices.currency IS 'Currency code (ISO 4217)';
