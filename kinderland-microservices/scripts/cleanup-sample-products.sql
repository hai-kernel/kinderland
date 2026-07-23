-- =====================================================================================
-- Dọn dữ liệu rác "Sản phẩm mẫu N" do vòng lặp i = 21..50 trong ProductDataSeeder cũ tạo.
-- Vòng lặp đó đã bị gỡ khỏi code; script này xoá các bản ghi ĐÃ nằm trong database.
--
-- Chạy (chế độ XEM TRƯỚC, không thay đổi gì):
--   docker exec -i kinderland-postgres psql -U postgres -d kinderland_product_db \
--     -v ON_ERROR_STOP=1 -f - < scripts/cleanup-sample-products.sql
--
-- Script KẾT THÚC BẰNG ROLLBACK. Đọc bảng "xem trước" mà nó in ra, nếu đúng ý thì
-- đổi ROLLBACK ở dòng cuối thành COMMIT rồi chạy lại. Xoá dữ liệu KHÔNG hoàn tác được.
--
-- Thứ tự xoá lấy từ quan hệ thật trong entity:
--   inventory.sku_id        -> sku.id       (FK, NOT NULL)
--   reviews.sku_id          -> sku.id       (FK, NOT NULL)  + reviews.product_id
--   transfer_orders.sku_id  -> sku.id       (FK, nullable)
--   sku.product_id          -> products.id  (FK, NOT NULL)
--   images(entity_type,entity_id)  -> KHÔNG có FK (đa hình) -> phải dọn tay
--   wishlist_items.product_id      -> KHÔNG có FK (cột thường) -> phải dọn tay
-- Bỏ qua 2 bảng cuối sẽ để lại bản ghi mồ côi mà database không hề báo lỗi.
-- =====================================================================================

\set ON_ERROR_STOP on

BEGIN;

-- Chốt danh sách id một lần, để mọi DELETE bên dưới nhìn cùng một tập
-- (nếu mỗi câu lệnh tự chạy LIKE thì dễ lệch nhau).
CREATE TEMP TABLE tmp_junk_products ON COMMIT DROP AS
SELECT id FROM products WHERE name LIKE 'Sản phẩm mẫu%';

CREATE TEMP TABLE tmp_junk_skus ON COMMIT DROP AS
SELECT id FROM sku WHERE product_id IN (SELECT id FROM tmp_junk_products);

-- ---------- XEM TRƯỚC ----------
SELECT 'products'        AS bang, COUNT(*) AS so_dong_se_xoa FROM tmp_junk_products
UNION ALL SELECT 'sku',             COUNT(*) FROM tmp_junk_skus
UNION ALL SELECT 'inventory',       COUNT(*) FROM inventory       WHERE sku_id     IN (SELECT id FROM tmp_junk_skus)
UNION ALL SELECT 'images',          COUNT(*) FROM images          WHERE entity_type = 'PRODUCT'
                                                                    AND entity_id  IN (SELECT id FROM tmp_junk_products)
UNION ALL SELECT 'wishlist_items',  COUNT(*) FROM wishlist_items  WHERE product_id IN (SELECT id FROM tmp_junk_products)
UNION ALL SELECT 'reviews',         COUNT(*) FROM reviews         WHERE sku_id     IN (SELECT id FROM tmp_junk_skus)
                                                                     OR product_id IN (SELECT id FROM tmp_junk_products)
UNION ALL SELECT 'transfer_orders', COUNT(*) FROM transfer_orders WHERE sku_id     IN (SELECT id FROM tmp_junk_skus);

-- DỪNG LẠI nếu 'reviews' hoặc 'transfer_orders' > 0: có nghiệp vụ thật gắn vào sản
-- phẩm mẫu (khách đã đánh giá, kho đã điều chuyển). Xử lý riêng, đừng xoá mù.

-- ---------- XOÁ THEO THỨ TỰ FK: con trước, cha sau ----------
DELETE FROM inventory       WHERE sku_id     IN (SELECT id FROM tmp_junk_skus);
DELETE FROM reviews         WHERE sku_id     IN (SELECT id FROM tmp_junk_skus)
                               OR product_id IN (SELECT id FROM tmp_junk_products);
DELETE FROM transfer_orders WHERE sku_id     IN (SELECT id FROM tmp_junk_skus);
DELETE FROM wishlist_items  WHERE product_id IN (SELECT id FROM tmp_junk_products);
DELETE FROM images          WHERE entity_type = 'PRODUCT'
                              AND entity_id  IN (SELECT id FROM tmp_junk_products);
DELETE FROM sku             WHERE id         IN (SELECT id FROM tmp_junk_skus);
DELETE FROM products        WHERE id         IN (SELECT id FROM tmp_junk_products);

-- ---------- KIỂM TRA: cả hai dòng PHẢI ra 0 ----------
SELECT 'con lai San pham mau' AS kiem_tra, COUNT(*) AS phai_bang_0
FROM products WHERE name LIKE 'Sản phẩm mẫu%'
UNION ALL
SELECT 'sku mo coi', COUNT(*) FROM sku s LEFT JOIN products p ON p.id = s.product_id WHERE p.id IS NULL;

SELECT COUNT(*) AS tong_san_pham_con_lai FROM products;

-- Đổi thành COMMIT sau khi đã xem output và xác nhận đúng.
ROLLBACK;
