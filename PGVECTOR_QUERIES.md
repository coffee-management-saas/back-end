# 📊 Hướng dẫn xem dữ liệu pgvector

## 🔌 Kết nối vào database

```bash
docker exec -it pgvector-db psql -U postgres -d future_saas
```

## 📋 Các lệnh psql hữu ích

### Lệnh meta (bắt đầu với \)

```sql
-- Xem tất cả các bảng
\dt

-- Xem tất cả extensions
\dx

-- Xem cấu trúc của một bảng cụ thể
\d table_name
\d vector_store

-- Xem tất cả schemas
\dn

-- Xem tất cả databases
\l

-- Xem kích thước database
\l+

-- Thoát psql
\q

-- Xem lịch sử lệnh
\s

-- Xem thời gian thực thi
\timing
```

### Lệnh SQL để kiểm tra

```sql
-- Kiểm tra extension vector
SELECT * FROM pg_extension WHERE extname = 'vector';

-- Xem kích thước database
SELECT pg_size_pretty(pg_database_size('future_saas')) as database_size;

-- Xem tất cả các bảng (SQL thuần)
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public';

-- Đếm số lượng records trong bảng
SELECT COUNT(*) FROM vector_store;

-- Xem cấu trúc bảng vector_store
SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'vector_store';
```

## 🔍 Xem dữ liệu Vector Store (sau khi Spring Boot chạy)

### Xem tất cả vectors

```sql
-- Xem 10 vectors đầu tiên
SELECT id, content, metadata 
FROM vector_store 
LIMIT 10;

-- Xem preview nội dung (50 ký tự đầu)
SELECT 
    id, 
    LEFT(content, 50) as content_preview, 
    metadata,
    created_at
FROM vector_store 
ORDER BY created_at DESC 
LIMIT 10;

-- Đếm tổng số vectors
SELECT COUNT(*) as total_vectors FROM vector_store;

-- Xem kích thước bảng
SELECT pg_size_pretty(pg_total_relation_size('vector_store')) as table_size;
```

### Tìm kiếm theo metadata

```sql
-- Tìm vectors theo metadata (ví dụ: category)
SELECT id, content, metadata 
FROM vector_store 
WHERE metadata->>'category' = 'product';

-- Tìm vectors theo nhiều điều kiện
SELECT id, content, metadata 
FROM vector_store 
WHERE metadata->>'type' = 'faq' 
  AND metadata->>'language' = 'vi';
```

### Xem thông tin embedding

```sql
-- Xem dimensions của vector
SELECT id, array_length(embedding, 1) as dimensions
FROM vector_store
LIMIT 1;

-- Xem một phần của vector embedding (10 phần tử đầu)
SELECT id, embedding[1:10] as embedding_preview
FROM vector_store
LIMIT 5;
```

### Similarity Search (tìm kiếm tương tự)

```sql
-- Tìm 5 vectors gần nhất với một vector cho trước
-- (Cần có vector để so sánh)
SELECT id, content, 
       embedding <=> '[0.1, 0.2, ...]'::vector as distance
FROM vector_store
ORDER BY distance
LIMIT 5;

-- Tìm vectors tương tự với một document cụ thể
SELECT 
    v1.id as source_id,
    v2.id as similar_id,
    v2.content,
    v1.embedding <=> v2.embedding as distance
FROM vector_store v1
CROSS JOIN vector_store v2
WHERE v1.id = 'your-document-id'
  AND v2.id != v1.id
ORDER BY distance
LIMIT 5;
```

## 📊 Thống kê và phân tích

```sql
-- Thống kê theo metadata
SELECT 
    metadata->>'category' as category,
    COUNT(*) as count
FROM vector_store
GROUP BY metadata->>'category'
ORDER BY count DESC;

-- Xem vectors được tạo gần đây
SELECT id, content, created_at
FROM vector_store
ORDER BY created_at DESC
LIMIT 10;

-- Tìm vectors có nội dung dài nhất
SELECT id, LENGTH(content) as content_length, LEFT(content, 100) as preview
FROM vector_store
ORDER BY content_length DESC
LIMIT 10;
```

## 🛠️ Quản lý dữ liệu

### Xóa dữ liệu

```sql
-- Xóa một vector cụ thể
DELETE FROM vector_store WHERE id = 'your-id';

-- Xóa vectors theo metadata
DELETE FROM vector_store WHERE metadata->>'category' = 'old_data';

-- Xóa tất cả vectors (CẢNH BÁO!)
TRUNCATE TABLE vector_store;
```

### Cập nhật metadata

```sql
-- Cập nhật metadata của một vector
UPDATE vector_store 
SET metadata = metadata || '{"updated": true}'::jsonb
WHERE id = 'your-id';

-- Thêm field mới vào metadata
UPDATE vector_store 
SET metadata = jsonb_set(metadata, '{new_field}', '"new_value"')
WHERE id = 'your-id';
```

## 🔧 Troubleshooting

### Kiểm tra indexes

```sql
-- Xem tất cả indexes
\di

-- Xem indexes của bảng vector_store
SELECT indexname, indexdef 
FROM pg_indexes 
WHERE tablename = 'vector_store';

-- Xem kích thước indexes
SELECT 
    indexname,
    pg_size_pretty(pg_relation_size(indexname::regclass)) as index_size
FROM pg_indexes
WHERE tablename = 'vector_store';
```

### Kiểm tra performance

```sql
-- Bật timing để xem thời gian thực thi
\timing

-- Xem query plan
EXPLAIN ANALYZE
SELECT * FROM vector_store 
WHERE metadata->>'category' = 'product'
LIMIT 10;

-- Xem thống kê bảng
SELECT * FROM pg_stat_user_tables WHERE relname = 'vector_store';
```

## 💡 Tips

1. **Sử dụng LIMIT:** Luôn dùng LIMIT khi query để tránh load quá nhiều dữ liệu
2. **Format output:** Dùng `\x` để toggle expanded display (dễ đọc hơn cho records lớn)
3. **Export data:** Dùng `\copy` để export dữ liệu ra file CSV
4. **Timing:** Dùng `\timing` để đo thời gian thực thi queries

## 📝 Ví dụ workflow

```sql
-- 1. Kết nối
docker exec -it pgvector-db psql -U postgres -d future_saas

-- 2. Bật timing
\timing

-- 3. Xem tổng quan
\dt
\dx

-- 4. Kiểm tra dữ liệu
SELECT COUNT(*) FROM vector_store;

-- 5. Xem một vài records
SELECT id, LEFT(content, 100), metadata 
FROM vector_store 
LIMIT 5;

-- 6. Thoát
\q
```

## 🚀 Quick Commands

```bash
# Xem số lượng vectors
docker exec pgvector-db psql -U postgres -d future_saas -c "SELECT COUNT(*) FROM vector_store;"

# Xem 5 vectors gần nhất
docker exec pgvector-db psql -U postgres -d future_saas -c "SELECT id, LEFT(content, 50) FROM vector_store ORDER BY created_at DESC LIMIT 5;"

# Xem kích thước database
docker exec pgvector-db psql -U postgres -d future_saas -c "SELECT pg_size_pretty(pg_database_size('future_saas'));"

# Kiểm tra extension
docker exec pgvector-db psql -U postgres -d future_saas -c "SELECT * FROM pg_extension WHERE extname = 'vector';"
```

---

**Lưu ý:** Bảng `vector_store` sẽ chỉ được tạo sau khi Spring Boot application chạy lần đầu tiên!
