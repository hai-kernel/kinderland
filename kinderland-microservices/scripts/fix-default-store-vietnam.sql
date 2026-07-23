-- =====================================================================================
-- Sửa cửa hàng placeholder "Kinderland Default Store / 123 Kinderland" thành cửa hàng
-- Việt Nam thật, và bổ sung các chi nhánh còn thiếu.
--
-- Vì sao cần script này: StoreDataSeeder chỉ THÊM cửa hàng mới, không sửa bản ghi
-- đã nằm trong database. Cửa hàng placeholder do ProductDataSeeder cũ tạo ra không có
-- latitude/longitude/phone/giờ mở cửa -> trang /stores hiện "null - null", cửa hàng
-- rơi vào nhóm "Khác" và bản đồ trôi sang Lào. KHÔNG xoá bản ghi này: toàn bộ tồn kho
-- (bảng inventory.store_id) đang trỏ vào nó.
--
-- Chạy (chế độ XEM TRƯỚC, không thay đổi gì):
--   docker exec -i kinderland-postgres psql -U postgres -d kinderland_product_db \
--     -v ON_ERROR_STOP=1 -f - < scripts/fix-default-store-vietnam.sql
--
-- Script KẾT THÚC BẰNG ROLLBACK. Đọc bảng "sau khi sửa" mà nó in ra, nếu đúng ý thì
-- đổi ROLLBACK ở dòng cuối thành COMMIT rồi chạy lại.
--
-- Địa chỉ BẮT BUỘC chứa "TP.HCM" / "Hà Nội" / "Đà Nẵng" / "Cần Thơ" / "Hải Phòng":
-- frontend nhóm cửa hàng theo thành phố bằng cách dò chuỗi trong address
-- (extractCityFromAddress trong src/services/storeApi.ts).
-- =====================================================================================

\set ON_ERROR_STOP on

BEGIN;

-- ---------------------------------------------------------------------------
-- 1) Biến cửa hàng placeholder thành chi nhánh Quận 1, TP.HCM.
--    Giữ nguyên id -> mọi bản ghi inventory/transfer_orders đang trỏ vào vẫn đúng.
-- ---------------------------------------------------------------------------
UPDATE stores
SET name          = 'Kinderland Nguyễn Huệ',
    code          = 'KL-HCM-01',
    address       = '72 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM',
    phone         = '(+84) 28-7300-8801',
    manager_name  = COALESCE(NULLIF(manager_name, ''), 'Nguyễn Thị Lan'),
    latitude      = 10.774020,
    longitude     = 106.703300,
    opening_time  = TIME '08:00',
    closing_time  = TIME '22:00',
    active        = TRUE
WHERE address = '123 Kinderland'
   OR name    = 'Kinderland Default Store';

-- ---------------------------------------------------------------------------
-- 2) Thêm các chi nhánh còn lại. ON CONFLICT không dùng được vì cột code không có
--    ràng buộc UNIQUE -> lọc bằng NOT EXISTS để chạy lại nhiều lần vẫn an toàn.
-- ---------------------------------------------------------------------------
INSERT INTO stores (name, code, address, phone, manager_name,
                    latitude, longitude, opening_time, closing_time, active, created_at)
SELECT v.name, v.code, v.address, v.phone, v.manager_name,
       v.latitude, v.longitude, v.opening_time, v.closing_time, TRUE, NOW()
FROM (VALUES
    ('Kinderland Crescent Mall', 'KL-HCM-02',
     '101 Tôn Dật Tiên, Phường Tân Phú, Quận 7, TP.HCM',
     '(+84) 28-7300-8802', 'Trần Minh Khoa', 10.728690, 106.718700, TIME '09:00', TIME '22:00'),
    ('Kinderland Gò Vấp', 'KL-HCM-03',
     '242 Quang Trung, Phường 10, Quận Gò Vấp, TP.HCM',
     '(+84) 28-7300-8803', 'Lê Thu Hà', 10.836480, 106.665100, TIME '08:30', TIME '21:30'),
    ('Kinderland Hoàn Kiếm', 'KL-HN-01',
     '25 Lý Thường Kiệt, Phường Phan Chu Trinh, Quận Hoàn Kiếm, Hà Nội',
     '(+84) 24-7300-8804', 'Phạm Quốc Anh', 21.024500, 105.853300, TIME '08:00', TIME '22:00'),
    ('Kinderland Times City', 'KL-HN-02',
     '458 Minh Khai, Phường Vĩnh Tuy, Quận Hai Bà Trưng, Hà Nội',
     '(+84) 24-7300-8805', 'Đỗ Hương Giang', 20.995300, 105.867600, TIME '09:00', TIME '22:00'),
    ('Kinderland Đà Nẵng', 'KL-DN-01',
     '255 Hùng Vương, Phường Vĩnh Trung, Quận Thanh Khê, Đà Nẵng',
     '(+84) 236-7300-8806', 'Ngô Văn Bình', 16.067800, 108.214000, TIME '08:00', TIME '21:30'),
    ('Kinderland Cần Thơ', 'KL-CT-01',
     '1 Đại lộ Hòa Bình, Phường Tân An, Quận Ninh Kiều, Cần Thơ',
     '(+84) 292-7300-8807', 'Huỳnh Mỹ Duyên', 10.033100, 105.782800, TIME '08:00', TIME '21:00'),
    ('Kinderland Hải Phòng', 'KL-HP-01',
     '15 Lạch Tray, Phường Đằng Giang, Quận Ngô Quyền, Hải Phòng',
     '(+84) 225-7300-8808', 'Vũ Đức Thắng', 20.845100, 106.688000, TIME '08:00', TIME '21:00')
) AS v(name, code, address, phone, manager_name,
       latitude, longitude, opening_time, closing_time)
WHERE NOT EXISTS (
    SELECT 1 FROM stores s WHERE lower(s.code) = lower(v.code)
);

-- ---------------------------------------------------------------------------
-- 3) Xem trước kết quả.
-- ---------------------------------------------------------------------------
SELECT id, code, name, address, phone, latitude, longitude,
       opening_time, closing_time, active
FROM stores
ORDER BY id;

-- Cảnh báo: cửa hàng còn thiếu toạ độ sẽ hiển thị sai trên bản đồ.
SELECT count(*) AS stores_thieu_toa_do
FROM stores
WHERE latitude IS NULL OR longitude IS NULL;

-- Đổi thành COMMIT khi đã xem và đồng ý với kết quả ở trên.
ROLLBACK;
