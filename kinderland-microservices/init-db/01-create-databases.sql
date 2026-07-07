-- Tạo sẵn database riêng cho từng service (Database per Service).
-- Chạy tự động bởi Postgres container ở lần khởi tạo đầu tiên.
CREATE DATABASE kinderland_auth_db;
CREATE DATABASE kinderland_product_db;
CREATE DATABASE kinderland_order_db;
CREATE DATABASE kinderland_payment_db;
