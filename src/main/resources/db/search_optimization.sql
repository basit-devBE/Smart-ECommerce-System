-- ============================================
-- Search Optimization: Database Indexes
-- Epic 3: Searching, Sorting, and Optimization
-- ============================================

-- Create indexes for product search optimization
-- This significantly improves query performance for search operations

-- Index on product_name for case-insensitive search
-- Using pg_trgm extension for trigram matching (better for partial matches)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- GIN index for fast text search on product_name
CREATE INDEX IF NOT EXISTS idx_products_name_trgm 
ON products USING gin (product_name gin_trgm_ops);

-- GIN index for fast text search on description
CREATE INDEX IF NOT EXISTS idx_products_description_trgm 
ON products USING gin (description gin_trgm_ops);

-- B-tree index for category_id (for category filtering)
CREATE INDEX IF NOT EXISTS idx_products_category_id 
ON products (category_id);

-- Index on price for sorting/filtering
CREATE INDEX IF NOT EXISTS idx_products_price 
ON products (price);

-- Composite index for common search patterns (category + name)
CREATE INDEX IF NOT EXISTS idx_products_category_name 
ON products (category_id, product_name);

-- Index for categories search
CREATE INDEX IF NOT EXISTS idx_categories_name_trgm 
ON categories USING gin (category_name gin_trgm_ops);

-- Index for inventory warehouse location search
CREATE INDEX IF NOT EXISTS idx_inventory_warehouse 
ON inventory (warehouse_location);

-- Index for user email search (unique constraint already provides this)
CREATE INDEX IF NOT EXISTS idx_users_email 
ON users (email);

-- Index for user name search
CREATE INDEX IF NOT EXISTS idx_users_firstname 
ON users (firstname);

CREATE INDEX IF NOT EXISTS idx_users_lastname 
ON users (lastname);

-- ============================================
-- Performance Analysis Query
-- Run this to verify index usage
-- ============================================

-- Check if indexes are being used
-- EXPLAIN ANALYZE SELECT * FROM products WHERE product_name ILIKE '%search%';

-- Show all indexes
-- SELECT tablename, indexname, indexdef FROM pg_indexes WHERE schemaname = 'public' ORDER BY tablename, indexname;
