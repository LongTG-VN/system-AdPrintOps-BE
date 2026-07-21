-- This migration serves both paths:
-- 1) fresh databases that executed V1..V8, and
-- 2) existing Hibernate-managed production databases baselined at V8.

ALTER TABLE pricing_rules ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

-- Remove legacy zero-width intervals before restoring the strict invariant.
DELETE FROM pricing_rules
WHERE max_area_sqm IS NOT NULL
  AND max_area_sqm <= min_area_sqm;

ALTER TABLE pricing_rules DROP CONSTRAINT IF EXISTS ck_pricing_rules_area_range;
ALTER TABLE pricing_rules
    ADD CONSTRAINT ck_pricing_rules_area_range
    CHECK (max_area_sqm IS NULL OR max_area_sqm > min_area_sqm);

-- Reconcile DECAL to the approved formula contract. Old runtime-seeded tiers
-- remain available for audit but are disabled.
INSERT INTO pricing_categories (code, name, description, is_active)
VALUES ('DECAL', 'In Decal', 'Cấu hình giá cho sản phẩm in decal', TRUE)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_active = TRUE;

UPDATE pricing_rules
SET is_active = FALSE,
    updated_by = 'SYSTEM_P1_REMEDIATION'
WHERE category_code = 'DECAL';

INSERT INTO pricing_rules (
    category_code, rule_name, min_area_sqm, max_area_sqm,
    price_per_sqm, lamination_fee_per_sqm, is_active, note, updated_by
)
VALUES
    ('DECAL', 'Nhỏ lẻ', 0.000, 0.100, 200000.00, 50000.00, TRUE, 'Diện tích dưới 0.1m²', 'SYSTEM_P1_REMEDIATION'),
    ('DECAL', 'Khổ nhỏ', 0.100, 1.000, 140000.00, 50000.00, TRUE, 'Diện tích từ 0.1m² đến dưới 1m²', 'SYSTEM_P1_REMEDIATION'),
    ('DECAL', 'Khổ chuẩn', 1.000, NULL, 120000.00, 50000.00, TRUE, 'Diện tích từ 1m² trở lên', 'SYSTEM_P1_REMEDIATION')
ON CONFLICT (category_code, min_area_sqm) DO UPDATE
SET rule_name = EXCLUDED.rule_name,
    max_area_sqm = EXCLUDED.max_area_sqm,
    price_per_sqm = EXCLUDED.price_per_sqm,
    lamination_fee_per_sqm = EXCLUDED.lamination_fee_per_sqm,
    is_active = TRUE,
    note = EXCLUDED.note,
    updated_by = EXCLUDED.updated_by;

INSERT INTO pricing_materials (
    category_code, material_code, material_name, multiplier, base_price, is_active
)
VALUES
    ('DECAL', 'thuong', 'Decal thường', 1.00, 0.00, TRUE),
    ('DECAL', 'trong', 'Decal trong / đẹp', 1.50, 0.00, TRUE)
ON CONFLICT (category_code, material_code) DO NOTHING;

-- Existing Hibernate-managed databases may not have received V8's unique
-- index. Deduplicate before enforcing the configuration key invariant.
DELETE FROM pricing_configurations duplicate
USING pricing_configurations keeper
WHERE duplicate.category_code = keeper.category_code
  AND duplicate.config_key = keeper.config_key
  AND duplicate.id > keeper.id;

CREATE UNIQUE INDEX IF NOT EXISTS uk_pricing_configurations_category_key
    ON pricing_configurations(category_code, config_key);

INSERT INTO pricing_configurations (
    category_code, config_key, config_name, base_price, multiplier, active, note
)
VALUES
    ('CARD', 'BOX_1_4', 'Card Visit 1-4 hộp', 70000, 1, TRUE, 'Đơn giá mỗi hộp'),
    ('CARD', 'BOX_5', 'Card Visit đúng 5 hộp', 40000, 1, TRUE, 'Đơn giá mỗi hộp'),
    ('CARD', 'BOX_6_9', 'Card Visit 6-9 hộp', 70000, 1, TRUE, 'Đơn giá mỗi hộp'),
    ('CARD', 'BOX_10_PLUS', 'Card Visit từ 10 hộp', 24000, 1, TRUE, 'Đơn giá mỗi hộp')
ON CONFLICT (category_code, config_key) DO NOTHING;
