-- =====================================================================================
-- Thêm cột xoá mềm cho bảng products.
--
-- ddl-auto=update của Hibernate TỰ thêm được 2 cột này, nên script chỉ cần khi:
--   - bạn muốn kiểm soát thời điểm thay đổi schema, hoặc
--   - ddl-auto đang để 'validate'/'none' trên môi trường thật.
--
-- An toàn chạy nhiều lần (IF NOT EXISTS).
--
--   docker exec -i kinderland-postgres psql -U postgres -d kinderland_product_db \
--     -v ON_ERROR_STOP=1 < scripts/add-product-soft-delete.sql
-- =====================================================================================

\set ON_ERROR_STOP on

BEGIN;

-- DEFAULT false BẮT BUỘC: thêm cột NOT NULL vào bảng đã có dữ liệu mà thiếu DEFAULT
-- thì PostgreSQL từ chối ALTER TABLE.
ALTER TABLE products ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE products ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

-- Danh sách sản phẩm luôn lọc deleted = false -> index giúp truy vấn không quét toàn bảng.
CREATE INDEX IF NOT EXISTS idx_products_deleted ON products (deleted);

-- Kiểm tra
SELECT COUNT(*) FILTER (WHERE deleted = false) AS dang_hien,
       COUNT(*) FILTER (WHERE deleted = true)  AS trong_thung_rac,
       COUNT(*)                                AS tong
FROM products;

COMMIT;
