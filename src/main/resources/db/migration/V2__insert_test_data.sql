-- V2__insert_test_data.sql
-- Description: Inserts test data for the 5 mandatory test scenarios
-- Brand: ZARA (brand_id = 1)
-- Product: 35455
-- Test scenarios cover dates from June 14-16, 2020

-- Price List 1: Base price for the entire period
-- Priority: 0 (lowest)
-- Valid from 2020-06-14 00:00:00 to 2020-12-31 23:59:59
-- Price: 35.50 EUR
INSERT INTO prices (brand_id, product_id, price_list, start_date, end_date, priority, price, currency)
VALUES (1, 35455, 1, '2020-06-14 00:00:00', '2020-12-31 23:59:59', 0, 35.50, 'EUR');

-- Price List 2: Special promotion price for afternoon hours on June 14
-- Priority: 1 (higher than base)
-- Valid from 2020-06-14 15:00:00 to 2020-06-14 18:30:00
-- Price: 25.45 EUR
-- Test scenario 2: Day 14 at 16:00 should return this price
INSERT INTO prices (brand_id, product_id, price_list, start_date, end_date, priority, price, currency)
VALUES (1, 35455, 2, '2020-06-14 15:00:00', '2020-06-14 18:30:00', 1, 25.45, 'EUR');

-- Price List 3: Another promotional price for June 15
-- Priority: 1 (higher than base)
-- Valid from 2020-06-15 00:00:00 to 2020-06-15 11:00:00
-- Price: 30.50 EUR
-- Test scenario 4: Day 15 at 10:00 should return this price
INSERT INTO prices (brand_id, product_id, price_list, start_date, end_date, priority, price, currency)
VALUES (1, 35455, 3, '2020-06-15 00:00:00', '2020-06-15 11:00:00', 1, 30.50, 'EUR');

-- Price List 4: Premium price for extended period
-- Priority: 1 (higher than base)
-- Valid from 2020-06-15 16:00:00 to 2020-12-31 23:59:59
-- Price: 38.95 EUR
-- Test scenario 5: Day 16 at 21:00 should return this price
INSERT INTO prices (brand_id, product_id, price_list, start_date, end_date, priority, price, currency)
VALUES (1, 35455, 4, '2020-06-15 16:00:00', '2020-12-31 23:59:59', 1, 38.95, 'EUR');

-- Summary of expected results for the 5 test scenarios:
-- Test 1: 2020-06-14 10:00 -> Price List 1, 35.50 EUR (only base price applies)
-- Test 2: 2020-06-14 16:00 -> Price List 2, 25.45 EUR (promotion price wins with priority 1)
-- Test 3: 2020-06-14 21:00 -> Price List 1, 35.50 EUR (only base price applies, promotion ended)
-- Test 4: 2020-06-15 10:00 -> Price List 3, 30.50 EUR (morning promotion wins with priority 1)
-- Test 5: 2020-06-16 21:00 -> Price List 4, 38.95 EUR (premium price wins with priority 1)
