-- Create vector_store table for Spring AI
-- This table stores document embeddings for similarity search

-- Create table
CREATE TABLE IF NOT EXISTS vector_store (
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    content text NOT NULL,
    metadata json,
    embedding vector(768) NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP
);

-- Create HNSW index for fast similarity search
-- Using cosine distance (most common for embeddings)
CREATE INDEX IF NOT EXISTS vector_store_embedding_idx 
ON vector_store 
USING hnsw (embedding vector_cosine_ops);

-- Optional: Create index on metadata for filtering
CREATE INDEX IF NOT EXISTS vector_store_metadata_idx 
ON vector_store 
USING gin (metadata);

-- Grant permissions (if needed)
-- GRANT ALL PRIVILEGES ON TABLE vector_store TO your_user;

-- Verify table creation
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables 
WHERE tablename = 'vector_store';

-- Verify indexes
SELECT indexname, indexdef 
FROM pg_indexes 
WHERE tablename = 'vector_store';
