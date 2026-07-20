INSERT INTO pricing_categories (code, name, description)
VALUES ('DECAL', 'In Decal', 'Cấu hình giá cho sản phẩm in decal')
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_active = TRUE;

INSERT INTO pricing_materials (category_code, material_code, material_name, multiplier, base_price)
VALUES
    ('DECAL', 'thuong', 'Decal thường', 1.00, 0.00),
    ('DECAL', 'trong', 'Decal trong / đẹp', 1.50, 0.00)
ON CONFLICT (category_code, material_code) DO UPDATE
SET material_name = EXCLUDED.material_name,
    multiplier = EXCLUDED.multiplier,
    base_price = EXCLUDED.base_price,
    is_active = TRUE;

INSERT INTO pricing_rules (
    category_code, rule_name, min_area_sqm, max_area_sqm,
    price_per_sqm, lamination_fee_per_sqm, note
)
VALUES
    ('DECAL', 'Nhỏ lẻ', 0.000, 0.100, 200000.00, 50000.00, 'Diện tích dưới 0.1m²'),
    ('DECAL', 'Khổ nhỏ', 0.100, 1.000, 140000.00, 50000.00, 'Diện tích từ 0.1m² đến dưới 1m²'),
    ('DECAL', 'Khổ chuẩn', 1.000, NULL, 120000.00, 50000.00, 'Diện tích từ 1m² trở lên')
ON CONFLICT (category_code, min_area_sqm) DO UPDATE
SET rule_name = EXCLUDED.rule_name,
    max_area_sqm = EXCLUDED.max_area_sqm,
    price_per_sqm = EXCLUDED.price_per_sqm,
    lamination_fee_per_sqm = EXCLUDED.lamination_fee_per_sqm,
    note = EXCLUDED.note,
    is_active = TRUE;
