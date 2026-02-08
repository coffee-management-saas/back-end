# Initialize pgvector database with vector_store table

Write-Host "=== Initializing pgvector Database ===" -ForegroundColor Cyan
Write-Host ""

# Check if container is running
$containerStatus = docker ps --filter name=pgvector-db --format "{{.Status}}"
if (-not $containerStatus) {
    Write-Host "ERROR: Container pgvector-db is not running!" -ForegroundColor Red
    Write-Host "Start it with: docker start pgvector-db" -ForegroundColor Yellow
    exit 1
}

Write-Host "Container Status: $containerStatus" -ForegroundColor Green
Write-Host ""

# Create vector_store table
Write-Host "Creating vector_store table..." -ForegroundColor Yellow
docker exec pgvector-db psql -U postgres -d future_saas -f /tmp/init-vector-store.sql 2>$null

if ($LASTEXITCODE -eq 0) {
    Write-Host "SUCCESS: Table created!" -ForegroundColor Green
} else {
    # Try alternative method
    Write-Host "Using alternative method..." -ForegroundColor Yellow
    
    # Create table
    docker exec pgvector-db psql -U postgres -d future_saas -c "CREATE TABLE IF NOT EXISTS vector_store (id uuid DEFAULT gen_random_uuid() PRIMARY KEY, content text NOT NULL, metadata json, embedding vector(768) NOT NULL, created_at timestamp DEFAULT CURRENT_TIMESTAMP);"
    
    # Create HNSW index
    docker exec pgvector-db psql -U postgres -d future_saas -c "CREATE INDEX IF NOT EXISTS vector_store_embedding_idx ON vector_store USING hnsw (embedding vector_cosine_ops);"
    
    # Create metadata index
    docker exec pgvector-db psql -U postgres -d future_saas -c "CREATE INDEX IF NOT EXISTS vector_store_metadata_idx ON vector_store USING gin (metadata);"
    
    Write-Host "SUCCESS: Table and indexes created!" -ForegroundColor Green
}

Write-Host ""

# Verify table
Write-Host "Verifying table structure..." -ForegroundColor Yellow
docker exec pgvector-db psql -U postgres -d future_saas -c "\d vector_store"

Write-Host ""

# Show table info
Write-Host "Table Information:" -ForegroundColor Cyan
docker exec pgvector-db psql -U postgres -d future_saas -c "SELECT pg_size_pretty(pg_total_relation_size('vector_store')) as table_size;"

Write-Host ""
Write-Host "=== Setup Complete! ===" -ForegroundColor Green
Write-Host ""
Write-Host "You can now:" -ForegroundColor Yellow
Write-Host "1. Restart your Spring Boot application" -ForegroundColor Gray
Write-Host "2. Add documents to vector store" -ForegroundColor Gray
Write-Host "3. Perform similarity searches" -ForegroundColor Gray
Write-Host ""
