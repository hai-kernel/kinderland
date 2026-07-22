# Kinderland Microservices

Chuyển đổi Kinderland từ **Modular Monolith** sang **Microservices**.
Reactor này độc lập với monolith cũ (`backend/pom.xml`) — monolith vẫn chạy được trong khi migrate.

## Kiến trúc mục tiêu

| Thành phần            | Port |
|-----------------------|------|
| Eureka Server         | 8761 |
| API Gateway           | 8080 |
| kinderland-common     | —    |
| Auth Service          | 8081 |
| Product Service       | 8082 |
| Order Service         | 8083 |
| Notification Service  | 8084 |

- **Async broker:** **Kafka** (Spring Cloud Stream). Hai luồng event:
  - `notification-events`: auth-service publish `UserCreatedEvent` (đăng ký) → notification-service gửi email.
  - `order-events`: order-service publish `OrderCreatedEvent` (tạo đơn) → product-service **trừ kho** (Saga).

- **Service Discovery:** Netflix Eureka.
- **Routing:** Spring Cloud Gateway (Spring Cloud 2025.0.0 / Boot 3.5.9).
- **Auth:** JWT được validate **tập trung tại Gateway**; service nhận danh tính qua header
  `X-Auth-Email` / `X-Auth-Role` (đã chống giả mạo).
- **Sync giữa service:** OpenFeign (vd: Order → Product lấy giá).
- **Async giữa service:** Spring Cloud Stream + Kafka/RabbitMQ (vd: đăng ký xong → gửi mail; tạo đơn → trừ kho).

## Cách chạy

> **Project độc lập** với monolith cũ: có sẵn Maven wrapper riêng (`./mvnw`),
> `.gitignore`, `.env.example`. Không cần gì từ thư mục `backend` bên ngoài.

Yêu cầu: JDK 21, Docker (cho Postgres + Kafka). Maven KHÔNG bắt buộc — dùng `./mvnw` kèm sẵn.

```bash
cd kinderland-microservices

# Cấu hình biến môi trường: copy mẫu rồi điền giá trị thật
cp .env .env

# 0. Hạ tầng: Postgres (tự tạo 3 DB) + Kafka
docker compose up -d

# 1. Build toàn bộ + cài kinderland-common vào local repo
mvn -DskipTests clean install

# 2. Eureka Server  (terminal 1)
mvn -pl eureka-server spring-boot:run            # http://localhost:8761

# 3. API Gateway    (terminal 2)   — cần JWT_SECRET (trùng auth-service)
$env:JWT_SECRET="<secret>"                        # bash: export JWT_SECRET=...
mvn -pl api-gateway spring-boot:run               # http://localhost:8080

# 4. Auth Service   (terminal 3)
$env:JWT_SECRET="<secret>"; $env:JWT_EXPIRATION="3600000"
$env:GOOGLE_CLIENT_ID="..."; $env:MAIL_USERNAME="..."; $env:MAIL_PASS="..."
mvn -pl auth-service spring-boot:run              # http://localhost:8081

# 5. Product / Order / Notification (mỗi cái 1 terminal)
mvn -pl product-service spring-boot:run           # http://localhost:8082
mvn -pl order-service spring-boot:run             # http://localhost:8083
$env:MAIL_USERNAME="..."; $env:MAIL_PASS="..."
mvn -pl notification-service spring-boot:run      # http://localhost:8084
```

### Sync (Feign) + Async (Saga) khi đặt hàng
- **Feign (sync):** Order gọi `lb://PRODUCT-SERVICE` `/internal/products/{id}` để **kiểm tra giá/tồn** lúc tạo đơn.
  `/internal/**` KHÔNG nằm trong route Gateway (không ra internet). Product phải chạy trước khi đặt đơn.
- **Kafka (async):** sau khi lưu đơn, Order publish `OrderCreatedEvent` → topic `order-events` →
  Product consume và **trừ kho** (eventual consistency). Tách trừ kho khỏi luồng đồng bộ.

### Thử luồng đặt hàng (qua Gateway, cần Bearer token)
```
POST http://localhost:8080/api/v1/products      (tạo product, có price + stockQuantity)
POST http://localhost:8080/api/v1/orders        body: {"items":[{"productId":1,"quantity":2}]}
   -> Order gọi Product (Feign) check giá/tồn, tạo đơn, bắn order-events; Product trừ kho async.
```

## Chạy toàn bộ bằng Docker (tuỳ chọn)
```bash
mvn -DskipTests clean package                       # build jar trước (bắt buộc)
docker compose -f docker-compose.yml up --build # hạ tầng + 6 service, 1 lệnh
```
Mỗi service có `Dockerfile` (JRE 21, copy jar). `docker-compose.full.yml` override DB/Eureka/Kafka
sang hostname container (`postgres`, `eureka-server`, `kafka:29092`). Có sẵn `JWT_SECRET` dev mặc định.

> Có thể đặt biến môi trường trong file `.env` (mỗi service tự `import optional:file:.env`).
> auth-service cần Kafka chạy để publish event; nếu Kafka tắt, app vẫn start, chỉ log lỗi khi gửi.

### Thử nhanh luồng đăng ký (qua Gateway)
```
POST http://localhost:8080/api/v1/auth/register
POST http://localhost:8080/api/v1/auth/login        -> nhận accessToken
GET  http://localhost:8080/api/v1/account/me         (Authorization: Bearer <accessToken>)
```
Khi register thành công, một `UserCreatedEvent` được đẩy lên Kafka topic `notification-events`.

### `JWT_SECRET` phải trùng
Token do **auth-service** ký bằng `JWT_SECRET`; Gateway verify bằng **cùng** secret đó.
Lệch secret ⇒ mọi request có token đều bị 401.

## Quy ước cho service downstream (Bước 2+)
- **KHÔNG** tự cấu hình CORS (Gateway lo). Tránh trùng header `Access-Control-Allow-Origin`.
- **KHÔNG** parse lại JWT. Đọc danh tính từ header `X-Auth-Email` / `X-Auth-Role`.
- Mỗi service có `application.yml` riêng, DB riêng, và đăng ký vào Eureka
  với `spring.application.name` viết HOA khớp tên trong route (AUTH-SERVICE, …).

## Routing map (Gateway → service)
- **auth-service:** `/api/v1/auth`, `/api/v1/account`, `/api/v1/address`, `/api/v1/admin`, `/api/v1/welcome`
- **product-service:** `/api/v1/products`, `/categories`, `/brands`, `/sku`, `/stores`, `/inventory`, `/images`, `/blogs`, `/blog-categories`, `/reviews`, `/promotions`, `/cart`, `/wishlist`, `/transfer`
- **order-service:** `/api/v1/orders`, `/payment`, `/financial`, `/loyalty`, `/return-requests`
