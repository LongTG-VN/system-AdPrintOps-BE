-- V6__create_pricing_matrix_configuration.sql
-- Matrix & Preset configuration table for all 9 product categories (100% synced with Cacl/index.html)

CREATE TABLE pricing_configurations (
    id BIGSERIAL PRIMARY KEY,
    category_code VARCHAR(50) NOT NULL,
    config_key VARCHAR(100) NOT NULL,
    config_name VARCHAR(255) NOT NULL,
    base_price NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    multiplier NUMERIC(5,2) NOT NULL DEFAULT 1.00,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    note TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pricing_configs_cat_key ON pricing_configurations(category_code, config_key);

-- Fix V5 Card rule description / tier in DB migration V6 (V5 max_area_sqm was 10.000)
UPDATE pricing_rules 
SET min_area_sqm = 5.000, max_area_sqm = 5.000, price_per_sqm = 40000, note = 'Card Visit tròn 5 hộp (40k/hộp)' 
WHERE category_code = 'CARD' AND min_area_sqm = 5.000 AND max_area_sqm = 10.000;

INSERT INTO pricing_rules (category_code, rule_name, min_area_sqm, max_area_sqm, price_per_sqm, lamination_fee_per_sqm, note, updated_by)
VALUES ('CARD', 'Card Visit 6-9 hộp', 6.000, 10.000, 70000, 0, 'Card Visit 6-9 hộp', 'SYSTEM');

-- Seed BANG 8 material/technique combinations x 3 area tiers (100% exact from bGia)
INSERT INTO pricing_configurations (category_code, config_key, config_name, base_price, multiplier, active, note) VALUES
('BANG', 'TOLE_IN_MINI', 'Tole In (<=0.06m2)', 40000, 1.00, true, 'Giá khoán <=0.06m2'),
('BANG', 'TOLE_IN_MID', 'Tole In (<0.5m2)', 550000, 1.00, true, 'Giá khoán <0.5m2'),
('BANG', 'TOLE_IN_LARGE', 'Tole In (>=0.5m2)', 450000, 1.00, true, 'Giá khoán >=0.5m2'),

('BANG', 'TOLE_CAT_MINI', 'Tole Cắt (<=0.06m2)', 50000, 1.00, true, 'Giá khoán <=0.06m2'),
('BANG', 'TOLE_CAT_MID', 'Tole Cắt (<0.5m2)', 700000, 1.00, true, 'Giá khoán <0.5m2'),
('BANG', 'TOLE_CAT_LARGE', 'Tole Cắt (>=0.5m2)', 550000, 1.00, true, 'Giá khoán >=0.5m2'),

('BANG', 'FORM_IN_MINI', 'Formex In (<=0.06m2)', 40000, 1.00, true, 'Giá khoán <=0.06m2'),
('BANG', 'FORM_IN_MID', 'Formex In (<0.5m2)', 400000, 1.00, true, 'Giá khoán <0.5m2'),
('BANG', 'FORM_IN_LARGE', 'Formex In (>=0.5m2)', 300000, 1.00, true, 'Giá khoán >=0.5m2'),

('BANG', 'FORM_CAT_MINI', 'Formex Cắt (<=0.06m2)', 50000, 1.00, true, 'Giá khoán <=0.06m2'),
('BANG', 'FORM_CAT_MID', 'Formex Cắt (<0.5m2)', 600000, 1.00, true, 'Giá khoán <0.5m2'),
('BANG', 'FORM_CAT_LARGE', 'Formex Cắt (>=0.5m2)', 450000, 1.00, true, 'Giá khoán >=0.5m2'),

('BANG', 'ALU_IN_MINI', 'Alu In (<=0.06m2)', 40000, 1.00, true, 'Giá khoán <=0.06m2'),
('BANG', 'ALU_IN_MID', 'Alu In (<0.5m2)', 650000, 1.00, true, 'Giá khoán <0.5m2'),
('BANG', 'ALU_IN_LARGE', 'Alu In (>=0.5m2)', 500000, 1.00, true, 'Giá khoán >=0.5m2'),

('BANG', 'ALU_CAT_MINI', 'Alu Cắt (<=0.06m2)', 70000, 1.00, true, 'Giá khoán <=0.06m2'),
('BANG', 'ALU_CAT_MID', 'Alu Cắt (<0.5m2)', 900000, 1.00, true, 'Giá khoán <0.5m2'),
('BANG', 'ALU_CAT_LARGE', 'Alu Cắt (>=0.5m2)', 700000, 1.00, true, 'Giá khoán >=0.5m2'),

('BANG', 'MICA_IN_MINI', 'Mica In (<=0.06m2)', 40000, 1.00, true, 'Giá khoán <=0.06m2'),
('BANG', 'MICA_IN_MID', 'Mica In (<0.5m2)', 1250000, 1.00, true, 'Giá khoán <0.5m2'),
('BANG', 'MICA_IN_LARGE', 'Mica In (>=0.5m2)', 900000, 1.00, true, 'Giá khoán >=0.5m2'),

('BANG', 'MICA_CAT_MINI', 'Mica Cắt (<=0.06m2)', 90000, 1.00, true, 'Giá khoán <=0.06m2'),
('BANG', 'MICA_CAT_MID', 'Mica Cắt (<0.5m2)', 1500000, 1.00, true, 'Giá khoán <0.5m2'),
('BANG', 'MICA_CAT_LARGE', 'Mica Cắt (>=0.5m2)', 1000000, 1.00, true, 'Giá khoán >=0.5m2');

-- Seed GIAY In Giấy Lẻ & Ép Nhựa & Tờ Rơi & Bảng Tên
INSERT INTO pricing_configurations (category_code, config_key, config_name, base_price, multiplier, active, note) VALUES
('GIAY', 'GIAY_A5_DAY1', 'In Giấy A5 Dày 1 Mặt', 5000, 1.00, true, 'In Giấy A5 Dày 1 Mặt'),
('GIAY', 'GIAY_A4_DAY1', 'In Giấy A4 Dày 1 Mặt', 7000, 1.00, true, 'In Giấy A4 Dày 1 Mặt'),
('GIAY', 'GIAY_A4_DAY2', 'In Giấy A4 Dày 2 Mặt', 13000, 1.00, true, 'In Giấy A4 Dày 2 Mặt'),
('GIAY', 'GIAY_A4_BONG1', 'In Giấy A4 Bóng 1 Mặt', 9000, 1.00, true, 'In Giấy A4 Bóng 1 Mặt'),
('GIAY', 'GIAY_A4_BONG2', 'In Giấy A4 Bóng 2 Mặt', 17000, 1.00, true, 'In Giấy A4 Bóng 2 Mặt'),
('GIAY', 'GIAY_A3_DAY1', 'In Giấy A3 Dày 1 Mặt', 13000, 1.00, true, 'In Giấy A3 Dày 1 Mặt'),
('GIAY', 'GIAY_A3_DAY2', 'In Giấy A3 Dày 2 Mặt', 24000, 1.00, true, 'In Giấy A3 Dày 2 Mặt'),
('GIAY', 'GIAY_A3_BONG1', 'In Giấy A3 Bóng 1 Mặt', 16000, 1.00, true, 'In Giấy A3 Bóng 1 Mặt'),
('GIAY', 'GIAY_A3_BONG2', 'In Giấy A3 Bóng 2 Mặt', 30000, 1.00, true, 'In Giấy A3 Bóng 2 Mặt'),

('GIAY', 'EP_A5_1', 'Ép nhựa A5 1 mặt', 10000, 1.00, true, 'Ép nhựa A5 1 mặt'),
('GIAY', 'EP_A5_2', 'Ép nhựa A5 2 mặt', 13000, 1.00, true, 'Ép nhựa A5 2 mặt'),
('GIAY', 'EP_A4_1', 'Ép nhựa A4 1 mặt', 20000, 1.00, true, 'Ép nhựa A4 1 mặt'),
('GIAY', 'EP_A4_2', 'Ép nhựa A4 2 mặt', 25000, 1.00, true, 'Ép nhựa A4 2 mặt'),
('GIAY', 'EP_A3_1', 'Ép nhựa A3 1 mặt', 35000, 1.00, true, 'Ép nhựa A3 1 mặt'),
('GIAY', 'EP_A3_2', 'Ép nhựa A3 2 mặt', 45000, 1.00, true, 'Ép nhựa A3 2 mặt'),

-- Tờ Rơi A4
('GIAY', 'ROI_A4_100_500', 'Tờ rơi A4 100g (500 tờ)', 700000, 1.00, true, 'Tờ rơi A4 100g 500 tờ'),
('GIAY', 'ROI_A4_100_1000', 'Tờ rơi A4 100g (1.000 tờ)', 800000, 1.00, true, 'Tờ rơi A4 100g 1.000 tờ'),
('GIAY', 'ROI_A4_100_2000', 'Tờ rơi A4 100g (2.000 tờ)', 1100000, 1.00, true, 'Tờ rơi A4 100g 2.000 tờ'),
('GIAY', 'ROI_A4_100_3000', 'Tờ rơi A4 100g (3.000 tờ)', 1460000, 1.00, true, 'Tờ rơi A4 100g 3.000 tờ'),
('GIAY', 'ROI_A4_100_5000', 'Tờ rơi A4 100g (5.000 tờ)', 2010000, 1.00, true, 'Tờ rơi A4 100g 5.000 tờ'),
('GIAY', 'ROI_A4_100_10000', 'Tờ rơi A4 100g (10.000 tờ)', 3690000, 1.00, true, 'Tờ rơi A4 100g 10.000 tờ'),

('GIAY', 'ROI_A4_150_500', 'Tờ rơi A4 150g (500 tờ)', 760000, 1.00, true, 'Tờ rơi A4 150g 500 tờ'),
('GIAY', 'ROI_A4_150_1000', 'Tờ rơi A4 150g (1.000 tờ)', 860000, 1.00, true, 'Tờ rơi A4 150g 1.000 tờ'),
('GIAY', 'ROI_A4_150_2000', 'Tờ rơi A4 150g (2.000 tờ)', 1190000, 1.00, true, 'Tờ rơi A4 150g 2.000 tờ'),
('GIAY', 'ROI_A4_150_3000', 'Tờ rơi A4 150g (3.000 tờ)', 1580000, 1.00, true, 'Tờ rơi A4 150g 3.000 tờ'),
('GIAY', 'ROI_A4_150_5000', 'Tờ rơi A4 150g (5.000 tờ)', 2280000, 1.00, true, 'Tờ rơi A4 150g 5.000 tờ'),
('GIAY', 'ROI_A4_150_10000', 'Tờ rơi A4 150g (10.000 tờ)', 4360000, 1.00, true, 'Tờ rơi A4 150g 10.000 tờ'),

('GIAY', 'ROI_A4_200_500', 'Tờ rơi A4 200g (500 tờ)', 960000, 1.00, true, 'Tờ rơi A4 200g 500 tờ'),
('GIAY', 'ROI_A4_200_1000', 'Tờ rơi A4 200g (1.000 tờ)', 1110000, 1.00, true, 'Tờ rơi A4 200g 1.000 tờ'),
('GIAY', 'ROI_A4_200_2000', 'Tờ rơi A4 200g (2.000 tờ)', 1500000, 1.00, true, 'Tờ rơi A4 200g 2.000 tờ'),
('GIAY', 'ROI_A4_200_3000', 'Tờ rơi A4 200g (3.000 tờ)', 1920000, 1.00, true, 'Tờ rơi A4 200g 3.000 tờ'),
('GIAY', 'ROI_A4_200_5000', 'Tờ rơi A4 200g (5.000 tờ)', 3060000, 1.00, true, 'Tờ rơi A4 200g 5.000 tờ'),
('GIAY', 'ROI_A4_200_10000', 'Tờ rơi A4 200g (10.000 tờ)', 5960000, 1.00, true, 'Tờ rơi A4 200g 10.000 tờ'),

-- Tờ Rơi A5
('GIAY', 'ROI_A5_100_1000', 'Tờ rơi A5 100g (1.000 tờ)', 740000, 1.00, true, 'Tờ rơi A5 100g 1.000 tờ'),
('GIAY', 'ROI_A5_100_2000', 'Tờ rơi A5 100g (2.000 tờ)', 880000, 1.00, true, 'Tờ rơi A5 100g 2.000 tờ'),
('GIAY', 'ROI_A5_100_4000', 'Tờ rơi A5 100g (4.000 tờ)', 1260000, 1.00, true, 'Tờ rơi A5 100g 4.000 tờ'),
('GIAY', 'ROI_A5_100_6000', 'Tờ rơi A5 100g (6.000 tờ)', 1560000, 1.00, true, 'Tờ rơi A5 100g 6.000 tờ'),
('GIAY', 'ROI_A5_100_10000', 'Tờ rơi A5 100g (10.000 tờ)', 2080000, 1.00, true, 'Tờ rơi A5 100g 10.000 tờ'),
('GIAY', 'ROI_A5_100_20000', 'Tờ rơi A5 100g (20.000 tờ)', 3430000, 1.00, true, 'Tờ rơi A5 100g 20.000 tờ'),

('GIAY', 'ROI_A5_150_1000', 'Tờ rơi A5 150g (1.000 tờ)', 2440000, 1.00, true, 'Tờ rơi A5 150g 1.000 tờ'),
('GIAY', 'ROI_A5_150_2000', 'Tờ rơi A5 150g (2.000 tờ)', 970000, 1.00, true, 'Tờ rơi A5 150g 2.000 tờ'),
('GIAY', 'ROI_A5_150_4000', 'Tờ rơi A5 150g (4.000 tờ)', 1330000, 1.00, true, 'Tờ rơi A5 150g 4.000 tờ'),
('GIAY', 'ROI_A5_150_6000', 'Tờ rơi A5 150g (6.000 tờ)', 1780000, 1.00, true, 'Tờ rơi A5 150g 6.000 tờ'),
('GIAY', 'ROI_A5_150_10000', 'Tờ rơi A5 150g (10.000 tờ)', 2430000, 1.00, true, 'Tờ rơi A5 150g 10.000 tờ'),
('GIAY', 'ROI_A5_150_20000', 'Tờ rơi A5 150g (20.000 tờ)', 4310000, 1.00, true, 'Tờ rơi A5 150g 20.000 tờ'),

('GIAY', 'ROI_A5_200_1000', 'Tờ rơi A5 200g (1.000 tờ)', 1040000, 1.00, true, 'Tờ rơi A5 200g 1.000 tờ'),
('GIAY', 'ROI_A5_200_2000', 'Tờ rơi A5 200g (2.000 tờ)', 1200000, 1.00, true, 'Tờ rơi A5 200g 2.000 tờ'),
('GIAY', 'ROI_A5_200_4000', 'Tờ rơi A5 200g (4.000 tờ)', 1630000, 1.00, true, 'Tờ rơi A5 200g 4.000 tờ'),
('GIAY', 'ROI_A5_200_6000', 'Tờ rơi A5 200g (6.000 tờ)', 2130000, 1.00, true, 'Tờ rơi A5 200g 6.000 tờ'),
('GIAY', 'ROI_A5_200_10000', 'Tờ rơi A5 200g (10.000 tờ)', 2960000, 1.00, true, 'Tờ rơi A5 200g 10.000 tờ'),
('GIAY', 'ROI_A5_200_20000', 'Tờ rơi A5 200g (20.000 tờ)', 5160000, 1.00, true, 'Tờ rơi A5 200g 20.000 tờ'),

-- Bảng tên
('GIAY', 'BANGTEN_35K_LT5', 'Bảng tên 35K (<5 màu)', 75000, 1.00, true, 'Bảng tên 35K dưới 5 màu'),
('GIAY', 'BANGTEN_35K_GT5', 'Bảng tên 35K (>5 màu)', 65000, 1.00, true, 'Bảng tên 35K trên 5 màu'),
('GIAY', 'BANGTEN_35K_GT10', 'Bảng tên 35K (>10 màu)', 55000, 1.00, true, 'Bảng tên 35K trên 10 màu'),

('GIAY', 'BANGTEN_50K_LT5', 'Bảng tên 50K (<5 màu)', 105000, 1.00, true, 'Bảng tên 50K dưới 5 màu'),
('GIAY', 'BANGTEN_50K_GT5', 'Bảng tên 50K (>5 màu)', 95000, 1.00, true, 'Bảng tên 50K trên 5 màu'),
('GIAY', 'BANGTEN_50K_GT10', 'Bảng tên 50K (>10 màu)', 85000, 1.00, true, 'Bảng tên 50K trên 10 màu');

-- Seed TRANH presets (100% exact from tranhPrices & snGia in Cacl/index.html)
INSERT INTO pricing_configurations (category_code, config_key, config_name, base_price, multiplier, active, note) VALUES
('TRANH', 'LED_A4_FULL', 'Tranh Điện LED A4 (20x30cm - Trọn bộ)', 790000, 1.00, true, 'Full bộ A4'),
('TRANH', 'LED_A4_IN', 'Tranh Điện LED A4 (20x30cm - Chỉ in)', 90000, 1.00, true, 'Chỉ in A4'),

('TRANH', 'LED_A3_FULL', 'Tranh Điện LED A3 (30x40cm - Trọn bộ)', 905000, 1.00, true, 'Full bộ A3'),
('TRANH', 'LED_A3_IN', 'Tranh Điện LED A3 (30x40cm - Chỉ in)', 100000, 1.00, true, 'Chỉ in A3'),

('TRANH', 'LED_A2_FULL', 'Tranh Điện LED A2 (40x60cm - Trọn bộ)', 1120000, 1.00, true, 'Full bộ A2'),
('TRANH', 'LED_A2_IN', 'Tranh Điện LED A2 (40x60cm - Chỉ in)', 115000, 1.00, true, 'Chỉ in A2'),

('TRANH', 'LED_A1_FULL', 'Tranh Điện LED A1 (60x80cm - Trọn bộ)', 1540000, 1.00, true, 'Full bộ A1'),
('TRANH', 'LED_A1_IN', 'Tranh Điện LED A1 (60x80cm - Chỉ in)', 160000, 1.00, true, 'Chỉ in A1'),

('TRANH', 'LED_50X70_FULL', 'Tranh Điện LED 50x70cm (Trọn bộ)', 1450000, 1.00, true, 'Full bộ 50x70'),
('TRANH', 'LED_50X70_IN', 'Tranh Điện LED 50x70cm (Chỉ in)', 140000, 1.00, true, 'Chỉ in 50x70'),

('TRANH', 'LED_60X90_FULL', 'Tranh Điện LED 60x90cm (Trọn bộ)', 1631000, 1.00, true, 'Full bộ 60x90'),
('TRANH', 'LED_60X90_IN', 'Tranh Điện LED 60x90cm (Chỉ in)', 170000, 1.00, true, 'Chỉ in 60x90'),

('TRANH', 'LED_30X120_FULL', 'Tranh Điện LED 30x120cm (Trọn bộ)', 1654000, 1.00, true, 'Full bộ 30x120'),
('TRANH', 'LED_30X120_IN', 'Tranh Điện LED 30x120cm (Chỉ in)', 139000, 1.00, true, 'Chỉ in 30x120'),

('TRANH', 'LED_60X120_FULL', 'Tranh Điện LED 60x120cm (Trọn bộ)', 1908000, 1.00, true, 'Full bộ 60x120'),
('TRANH', 'LED_60X120_IN', 'Tranh Điện LED 60x120cm (Chỉ in)', 193000, 1.00, true, 'Chỉ in 60x120'),

('TRANH', 'LED_80X120_FULL', 'Tranh Điện LED 80x120cm (Trọn bộ)', 2194000, 1.00, true, 'Full bộ 80x120'),
('TRANH', 'LED_80X120_IN', 'Tranh Điện LED 80x120cm (Chỉ in)', 229000, 1.00, true, 'Chỉ in 80x120'),

-- Biển Số Nhà
('TRANH', 'SN_25X15_CHUNOI', 'Số nhà 25x15 Chữ + số nổi', 650000, 1.00, true, 'Số nhà 25x15 chữ + số nổi'),
('TRANH', 'SN_30X20_ANMON', 'Số nhà 30x20 Ăn mòn', 520000, 1.00, true, 'Số nhà 30x20 ăn mòn'),
('TRANH', 'SN_30X20_NOI', 'Số nhà 30x20 Số nổi', 750000, 1.00, true, 'Số nhà 30x20 số nổi'),
('TRANH', 'SN_30X20_CHUNOI', 'Số nhà 30x20 Chữ + số nổi', 1000000, 1.00, true, 'Số nhà 30x20 chữ + số nổi'),
('TRANH', 'SN_35X25_NOI', 'Số nhà 35x25 Số nổi', 850000, 1.00, true, 'Số nhà 35x25 số nổi'),
('TRANH', 'SN_35X25_CHUNOI', 'Số nhà 35x25 Chữ + số nổi', 1040000, 1.00, true, 'Số nhà 35x25 chữ + số nổi'),
('TRANH', 'SN_40X30_ANMON', 'Số nhà 40x30 Ăn mòn', 850000, 1.00, true, 'Số nhà 40x30 ăn mòn'),
('TRANH', 'SN_60X40_ANMON', 'Số nhà 60x40 Ăn mòn', 1250000, 1.00, true, 'Số nhà 60x40 ăn mòn');

-- Seed KHAC services
INSERT INTO pricing_configurations (category_code, config_key, config_name, base_price, multiplier, active, note) VALUES
('KHAC', 'BANG_LUA_CHAN', 'Bảng lụa có chân khoán', 650000, 1.00, true, 'Giá khoán 650k'),
('KHAC', 'LAMINATION_SERVICE', 'Gia công cán màng m2', 50000, 1.00, true, 'Cán màng 50k/m2');
