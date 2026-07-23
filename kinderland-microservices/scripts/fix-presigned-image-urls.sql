-- =====================================================================================
-- Sửa các bản ghi images bị ghi đè bằng PRESIGNED URL thay vì S3 key.
--
-- Nguyên nhân (đã vá ở code): form sửa sản phẩm gửi lại presigned URL do API trả về
-- làm imageUrl. Backend lưu nguyên chuỗi đó, và vì nó bắt đầu bằng "http" nên
-- resolveImageUrl trả thẳng, không ký lại -> ảnh chết sau 60 phút.
--
-- Presigned URL có dạng:
--   https://<bucket>.s3.<region>.amazonaws.com/images/<uuid>_<ten>.jpg?X-Amz-Algorithm=...
-- S3 key thật là phần giữa:
--   images/<uuid>_<ten>.jpg
--
-- Script cắt bỏ phần host và query để lấy lại key. An toàn chạy nhiều lần.
--
--   docker exec -i kinderland-postgres psql -U postgres -d kinderland_product_db \
--     -v ON_ERROR_STOP=1 < scripts/fix-presigned-image-urls.sql
--
-- Kết thúc bằng ROLLBACK. Xem output, đúng thì đổi thành COMMIT và chạy lại.
-- =====================================================================================

\set ON_ERROR_STOP on

BEGIN;

-- ---------- XEM TRƯỚC: bản ghi nào đang hỏng ----------
SELECT id,
       entity_type,
       entity_id,
       LEFT(image_url, 60) AS hien_tai,
       -- key khôi phục được: bỏ scheme+host ở đầu, bỏ query string ở cuối
       regexp_replace(regexp_replace(image_url, '^https?://[^/]+/', ''), '\?.*$', '') AS key_khoi_phuc
FROM images
WHERE image_url ILIKE 'http%'
  AND (image_url ILIKE '%X-Amz-Signature%' OR image_url ILIKE '%X-Amz-Credential%')
ORDER BY entity_id;

-- ---------- SỬA ----------
UPDATE images
SET image_url = regexp_replace(regexp_replace(image_url, '^https?://[^/]+/', ''), '\?.*$', '')
WHERE image_url ILIKE 'http%'
  AND (image_url ILIKE '%X-Amz-Signature%' OR image_url ILIKE '%X-Amz-Credential%');

-- ---------- KIỂM TRA: phải ra 0 ----------
SELECT COUNT(*) AS con_presigned_url
FROM images
WHERE image_url ILIKE '%X-Amz-Signature%' OR image_url ILIKE '%X-Amz-Credential%';

-- Phân loại phần còn lại để soát bằng mắt.
SELECT CASE
           WHEN image_url ILIKE 'http%' THEN 'URL public (seed - OK)'
           ELSE 'S3 key (OK)'
       END AS loai,
       COUNT(*)
FROM images
GROUP BY 1;

-- Đổi thành COMMIT sau khi đã xem output và thấy đúng.
ROLLBACK;
