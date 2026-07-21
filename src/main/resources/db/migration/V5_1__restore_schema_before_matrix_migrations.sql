-- Restore the canonical V1/JPA names after the legacy V5 seed.
ALTER TABLE pricing_categories RENAME COLUMN category_code TO code;
ALTER TABLE pricing_categories RENAME COLUMN category_name TO name;

-- V6/V7 reference this audit column even though the original schema omitted it.
ALTER TABLE pricing_rules ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

-- V6 temporarily creates an exact CARD interval (min=max). V9 removes that
-- legacy row and restores the strict area constraint after V8 moves CARD tiers
-- to pricing_configurations.
ALTER TABLE pricing_rules DROP CONSTRAINT IF EXISTS ck_pricing_rules_area_range;
