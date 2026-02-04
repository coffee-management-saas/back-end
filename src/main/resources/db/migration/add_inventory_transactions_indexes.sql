-- Migration: Add indexes to inventory_transactions table for better query performance
-- Created: 2026-01-31
-- Purpose: Optimize queries for reports, order tracking, and batch history

-- Index 1: Shop + Ingredient + Date (for inventory reports by ingredient over time)
CREATE INDEX IF NOT EXISTS idx_inv_trans_shop_ingredient_date 
ON inventory_transactions(shop_id, ingredient_id, created_at);

-- Index 2: Shop + Batch (for batch history and FIFO tracking)
CREATE INDEX IF NOT EXISTS idx_inv_trans_shop_batch 
ON inventory_transactions(shop_id, batch_id);

-- Index 3: Shop + Order (for viewing what ingredients were deducted for an order)
CREATE INDEX IF NOT EXISTS idx_inv_trans_shop_order 
ON inventory_transactions(shop_id, order_id);

-- Index 4: Shop + Transaction Type + Date (for reports by transaction type)
CREATE INDEX IF NOT EXISTS idx_inv_trans_shop_type_date 
ON inventory_transactions(shop_id, transaction_type, created_at);

-- Index 5: Created At (for time-based queries and archiving old data)
CREATE INDEX IF NOT EXISTS idx_inv_trans_created_at 
ON inventory_transactions(created_at);

-- Verify indexes were created
SELECT 
    indexname, 
    indexdef 
FROM 
    pg_indexes 
WHERE 
    tablename = 'inventory_transactions'
ORDER BY 
    indexname;
