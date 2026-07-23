-- =====================================================================================
-- Nới cột order_items.image_url (varchar(255) -> TEXT) và product_name (-> varchar(500)).
--
-- LỖI ĐANG SỬA:
--   ERROR: value too long for type character varying(255)
--   khi INSERT order_items trong OrderService.createOrder -> POST /api/v1/orders/create trả 500.
--
-- NGUYÊN NHÂN:
--   product-service trả imageUrl là PRESIGNED S3 URL (S3Service.resolveImageUrl), dài
--   700–900 ký tự vì kèm X-Amz-Credential + X-Amz-Signature. OrderItem.imageUrl để kiểu
--   String không khai báo độ dài -> Hibernate tạo cột varchar(255). Đo thực tế trên
--   API production: URL ảnh SKU dài ~780 ký tự.
--
-- VÌ SAO PHẢI CHẠY TAY:
--   Dự án KHÔNG dùng Flyway/Liquibase (không có dependency nào trong pom, không có
--   thư mục db/migration). Schema do `spring.jpa.hibernate.ddl-auto: update` sinh ra,
--   mà chế độ update CHỈ thêm bảng/cột mới — nó KHÔNG bao giờ đổi kiểu cột đã tồn tại.
--   Vì vậy sửa entity thôi là chưa đủ với database đang chạy: bắt buộc ALTER tay bằng
--   script này. Với database tạo mới thì entity đã đủ.
--
-- Chạy (chế độ XEM TRƯỚC, không thay đổi gì):
--   docker exec -i kinderland-postgres psql -U postgres -d kinderland_order_db \
--     -v ON_ERROR_STOP=1 -f - < scripts/widen-order-items-image-url.sql
--
-- Script KẾT THÚC BẰNG ROLLBACK. Đọc bảng kiểu cột nó in ra, đúng ý thì đổi ROLLBACK
-- ở dòng cuối thành COMMIT rồi chạy lại.
--
-- AN TOÀN: varchar(255) -> TEXT và varchar(255) -> varchar(500) đều là NỚI RỘNG,
-- không cắt dữ liệu, không cần rewrite bảng trên PostgreSQL 9.2+ (chỉ đổi catalog).
-- Bảng không bị khoá lâu, nhưng ALTER TABLE vẫn cần ACCESS EXCLUSIVE LOCK trong
-- khoảnh khắc thực thi -> nên chạy lúc vắng đơn.
-- =====================================================================================

\set ON_ERROR_STOP on

BEGIN;

-- Trạng thái TRƯỚC khi sửa.
SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'order_items'
  AND column_name IN ('image_url', 'product_name')
ORDER BY column_name;

ALTER TABLE order_items
    ALTER COLUMN image_url TYPE TEXT;

ALTER TABLE order_items
    ALTER COLUMN product_name TYPE VARCHAR(500);

-- Trạng thái SAU khi sửa: image_url phải là 'text' (character_maximum_length = NULL),
-- product_name phải là 'character varying' với độ dài 500.
SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'order_items'
  AND column_name IN ('image_url', 'product_name')
ORDER BY column_name;

-- Đổi thành COMMIT khi đã xem và đồng ý với kết quả ở trên.
ROLLBACK;
