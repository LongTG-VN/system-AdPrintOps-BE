-- Seed Pricing Categories
INSERT INTO pricing_categories (category_code, category_name, description, is_active)
VALUES 
    ('TEM', 'In Tem Bế', 'Tem nhãn bế hình / cắt định hình', true),
    ('CAT', 'Cắt Decal', 'Cắt decal viền / cắt cuộn theo tấc', true),
    ('CARD', 'Card Visit', 'In danh thiếp theo hộp', true),
    ('BANG', 'Bảng Hiệu Cứng', 'Bảng hiệu Formex, Alu, Mica, Tole', true),
    ('HIFLEX', 'Bảng Bạt Hiflex', 'Bạt Hiflex lót khung sắt vuông 16/20/25', true),
    ('GIAY', 'In Giấy / Tờ Rơi', 'In giấy Couche, brochure, catalog', true),
    ('TRANH', 'Tranh Điện / LED', 'Hộp đèn siêu mỏng LED, số nhà ăn mòn', true),
    ('KHAC', 'Phụ Phí & Khác', 'Gia công lắp đặt, phí phụ thu', true)
ON CONFLICT (category_code) DO NOTHING;

-- Seed Rules & Materials for TEM
INSERT INTO pricing_rules (category_code, rule_name, min_area_sqm, max_area_sqm, price_per_sqm, lamination_fee_per_sqm, is_active, note)
VALUES ('TEM', 'Đơn giá Tem bế hình', 0.000, NULL, 120000.00, 50000.00, true, 'Đơn giá bế hình tiêu chuẩn');

INSERT INTO pricing_materials (category_code, material_code, material_name, multiplier, base_price, is_active)
VALUES 
    ('TEM', 'bedie', 'Tem có bế hình (Chuẩn)', 1.00, 0.00, true),
    ('TEM', 'nobedie', 'Tem cắt thẳng (Không bế)', 0.92, 0.00, true);

-- Seed Rules & Materials for CAT
INSERT INTO pricing_rules (category_code, rule_name, min_area_sqm, max_area_sqm, price_per_sqm, lamination_fee_per_sqm, is_active, note)
VALUES ('CAT', 'Đơn giá Cắt Decal m²', 0.000, NULL, 130000.00, 0.00, true, 'Cắt Decal phẳng m²');

INSERT INTO pricing_materials (category_code, material_code, material_name, multiplier, base_price, is_active)
VALUES 
    ('CAT', 'phang', 'Cắt Decal Phẳng (m²)', 1.00, 0.00, true),
    ('CAT', 'cuon6tac', 'Cắt Decal Cuộn Khổ 6 tấc (đ/tấc)', 1.00, 10000.00, true),
    ('CAT', 'cuon10tac', 'Cắt Decal Cuộn Khổ 1m (đ/tấc)', 1.00, 15000.00, true);

-- Seed Rules & Materials for CARD
INSERT INTO pricing_rules (category_code, rule_name, min_area_sqm, max_area_sqm, price_per_sqm, lamination_fee_per_sqm, is_active, note)
VALUES 
    ('CARD', 'Card 1-4 hộp', 1.000, 5.000, 70000.00, 0.00, true, '70.000đ/hộp (1-4 hộp)'),
    ('CARD', 'Card 5-9 hộp', 5.000, 10.000, 40000.00, 0.00, true, '40.000đ/hộp (5-9 hộp)'),
    ('CARD', 'Card >=10 hộp', 10.000, NULL, 24000.00, 0.00, true, '24.000đ/hộp (>=10 hộp)');

INSERT INTO pricing_materials (category_code, material_code, material_name, multiplier, base_price, is_active)
VALUES ('CARD', 'couche300', 'Giấy Couche 300gsm cán màng mờ', 1.00, 0.00, true);

-- Seed Rules & Materials for BANG
INSERT INTO pricing_rules (category_code, rule_name, min_area_sqm, max_area_sqm, price_per_sqm, lamination_fee_per_sqm, is_active, note)
VALUES ('BANG', 'Bảng hiệu vật liệu cứng', 0.000, NULL, 150000.00, 0.00, true, 'Đơn giá nền phẳng');

INSERT INTO pricing_materials (category_code, material_code, material_name, multiplier, base_price, is_active)
VALUES 
    ('BANG', 'formex', 'Nền Formex 5mm', 1.00, 150000.00, true),
    ('BANG', 'alu', 'Nền Alu ngoài trời 3mm', 1.60, 240000.00, true),
    ('BANG', 'mica', 'Nền Mica Đài Loan 3mm', 2.20, 330000.00, true),
    ('BANG', 'tole', 'Nền Tole mạ kẽm', 1.40, 210000.00, true);

-- Seed Rules & Materials for HIFLEX
INSERT INTO pricing_rules (category_code, rule_name, min_area_sqm, max_area_sqm, price_per_sqm, lamination_fee_per_sqm, is_active, note)
VALUES ('HIFLEX', 'In bạt Hiflex xuyên đèn', 0.000, NULL, 65000.00, 0.00, true, 'Bạt Hiflex 65.000đ/m²');

INSERT INTO pricing_materials (category_code, material_code, material_name, multiplier, base_price, is_active)
VALUES 
    ('HIFLEX', 'khung16', 'Khung sắt vuông 16 (65.000đ/m dài)', 1.00, 65000.00, true),
    ('HIFLEX', 'khung20', 'Khung sắt vuông 20 (85.000đ/m dài)', 1.00, 85000.00, true),
    ('HIFLEX', 'khung25', 'Khung sắt vuông 25 (105.000đ/m dài)', 1.00, 105000.00, true);

-- Seed Rules & Materials for GIAY
INSERT INTO pricing_rules (category_code, rule_name, min_area_sqm, max_area_sqm, price_per_sqm, lamination_fee_per_sqm, is_active, note)
VALUES ('GIAY', 'Tờ rơi in màu Couche', 0.000, NULL, 120000.00, 30000.00, true, 'Tờ rơi in chuẩn');

INSERT INTO pricing_materials (category_code, material_code, material_name, multiplier, base_price, is_active)
VALUES 
    ('GIAY', 'c150', 'Giấy Couche 150gsm', 1.00, 0.00, true),
    ('GIAY', 'c250', 'Giấy Couche 250gsm', 1.25, 0.00, true),
    ('GIAY', 'c300', 'Giấy Couche 300gsm', 1.40, 0.00, true);

-- Seed Rules & Materials for TRANH
INSERT INTO pricing_rules (category_code, rule_name, min_area_sqm, max_area_sqm, price_per_sqm, lamination_fee_per_sqm, is_active, note)
VALUES ('TRANH', 'Tranh điện siêu mỏng', 0.000, NULL, 350000.00, 0.00, true, 'Hộp đèn LED 350k/m²');

INSERT INTO pricing_materials (category_code, material_code, material_name, multiplier, base_price, is_active)
VALUES 
    ('TRANH', 'tranh_led', 'Hộp đèn LED siêu mỏng', 1.00, 0.00, true),
    ('TRANH', 'so_nha', 'Số nhà inox ăn mòn 3D', 1.30, 0.00, true);

-- Seed Rules & Materials for KHAC
INSERT INTO pricing_rules (category_code, rule_name, min_area_sqm, max_area_sqm, price_per_sqm, lamination_fee_per_sqm, is_active, note)
VALUES ('KHAC', 'Phụ phí dịch vụ gia công', 0.000, NULL, 50000.00, 0.00, true, 'Gia công tùy chọn');

INSERT INTO pricing_materials (category_code, material_code, material_name, multiplier, base_price, is_active)
VALUES ('KHAC', 'giacong', 'Chi phí thi công / gia công', 1.00, 50000.00, true);
