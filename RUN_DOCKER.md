# Hướng dẫn chạy ứng dụng với Docker

## Cách 1: Sử dụng Docker Compose (KHUYẾN NGHỊ)

### Chạy toàn bộ stack (database + backend):
```bash
docker-compose up -d --build
```

### Xem logs:
```bash
docker-compose logs -f
```

### Dừng toàn bộ:
```bash
docker-compose down
```

### Truy cập ứng dụng:
- Backend API: http://localhost:8081
- Swagger UI: http://localhost:8081/swagger-ui.html

---

## Cách 2: Chạy riêng lẻ với docker run

### Bước 1: Đảm bảo database đang chạy
```bash
docker-compose up -d pgvector-db
```

### Bước 2: Chạy backend container
```bash
docker run --name saas-app -p 8082:8080 \
  --network back-end_default \
  -e DB_HOST=pgvector-db \
  -e DB_PORT=5432 \
  -e DB_NAME=future_saas \
  -e DB_USER=postgres \
  -e DB_PASSWORD=123456 \
  -e PORT=8080 \
  -e APPLICATION_NAME=saas \
  -e JWT_SECRET=9a4f2c8d3b7a1e5f2g8h4j9k0l1m2n3o4p5q6r7s8t9u0v1w2x3y4z5a6b7c8d9e \
  -e JWT_ACCESS_EXPIRATION=3600000 \
  -e JWT_REFRESH_EXPIRATION=604800000 \
  -e CLOUD_NAME=dmen8hyem \
  -e CLOUD_API_KEY=622878251667519 \
  -e CLOUD_API_SECRET=_o0mYsM0cWRNbO3VHu8NYk6yjbw \
  saas:0.0.1
```

### Truy cập ứng dụng:
- Backend API: http://localhost:8082

### Xem logs:
```bash
docker logs -f saas-app
```

### Dừng và xóa container:
```bash
docker stop saas-app
docker rm saas-app
```

---

## Lưu ý quan trọng

1. **Database type**: Ứng dụng sử dụng **PostgreSQL**, không phải MySQL
2. **Network**: Container backend phải ở cùng network với database (`back-end_default`)
3. **Environment variables**: Cần cung cấp đầy đủ các biến môi trường từ file `.env`
4. **Port mapping**: Format là `host_port:container_port` (ví dụ: `8082:8080`)

## Troubleshooting

### Lỗi "Connection refused":
- Kiểm tra database đang chạy: `docker ps | grep pgvector`
- Kiểm tra network: `docker network ls`
- Đảm bảo backend container ở cùng network với database

### Lỗi "Port already allocated":
- Thay đổi host port (số bên trái dấu `:`) thành port khác
- Ví dụ: `-p 8083:8080` thay vì `-p 8082:8080`

### Lỗi "Unknown type vector":
- Kết nối vào database và tạo extension:
```bash
docker exec -it pgvector-db psql -U postgres -d future_saas -c "CREATE EXTENSION IF NOT EXISTS vector;"
```
