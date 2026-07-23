-- =====================================================================================
-- Dọn ảnh trùng của product: mỗi sản phẩm chỉ giữ MỘT dòng ảnh bìa.
--
-- Bối cảnh: một phiên bản ProductImageDataSeeder cũ tạo 3 dòng ảnh placehold.co cho mỗi
-- sản phẩm. ProductService coi ảnh bìa là DUY NHẤT — nó ghi vào một dòng và đọc ra một
-- dòng. Có 3 dòng thì hai thao tác đó chọn nhầm nhau: lưu ảnh mới vào dòng này, hiển thị
-- dòng khác (vẫn là placehold.co). Đó là lý do "cập nhật thành công" mà ảnh không đổi.
--
-- Quy tắc giữ lại, theo thứ tự ưu tiên:
--   1. Ảnh THẬT (S3 key — không bắt đầu bằng http)
--   2. Nếu không có, giữ dòng id nhỏ nhất
-- Xoá phần còn lại.
--
--   docker exec -i kinderland-postgres psql -U postgres -d kinderland_product_db \
--     -v ON_ERROR_STOP=1 < scripts/cleanup-duplicate-product-images.sql
--
-- Kết thúc bằng ROLLBACK. Xem output, đúng thì đổi thành COMMIT rồi chạy lại.
-- =====================================================================================

\set ON_ERROR_STOP on

BEGIN;

-- ---------- XEM TRƯỚC: sản phẩm nào đang có nhiều hơn 1 ảnh ----------
SELECT entity_id, COUNT(*) AS so_anh,
       COUNT(*) FILTER (WHERE image_url NOT ILIKE 'http%') AS anh_s3_that,
       COUNT(*) FILTER (WHERE image_url ILIKE '%placehold.co%') AS anh_placeholder
FROM images
WHERE entity_type = 'PRODUCT'
GROUP BY entity_id
HAVING COUNT(*) > 1
ORDER BY entity_id;

-- ---------- Dòng sẽ GIỮ LẠI cho mỗi sản phẩm ----------
CREATE TEMP TABLE tmp_keep ON COMMIT DROP AS
SELECT DISTINCT ON (entity_id) id, entity_id, image_url
FROM images
WHERE entity_type = 'PRODUCT'
ORDER BY entity_id,
         -- ưu tiên ảnh thật (S3 key) trước placeholder/URL ngoài
         (image_url ILIKE 'http%') ASC,
         id ASC;

SELECT k.entity_id, k.id AS id_giu_lai, LEFT(k.image_url, 60) AS image_url_giu_lai
FROM tmp_keep k ORDER BY k.entity_id;

-- ---------- XOÁ các dòng thừa ----------
DELETE FROM images
WHERE entity_type = 'PRODUCT'
  AND id NOT IN (SELECT id FROM tmp_keep);

-- ---------- KIỂM TRA: không còn sản phẩm nào có >1 ảnh ----------
SELECT COUNT(*) AS san_pham_con_nhieu_anh
FROM (SELECT entity_id FROM images WHERE entity_type = 'PRODUCT'
      GROUP BY entity_id HAVING COUNT(*) > 1) x;

SELECT CASE WHEN image_url ILIKE '%placehold.co%' THEN 'placeholder'
            WHEN image_url ILIKE 'http%'          THEN 'URL ngoai (seed)'
            ELSE 'S3 key (anh that)' END AS loai,
       COUNT(*)
FROM images WHERE entity_type = 'PRODUCT'
GROUP BY 1;

-- Đổi thành COMMIT sau khi đã xem output và thấy đúng.
ROLLBACK;
