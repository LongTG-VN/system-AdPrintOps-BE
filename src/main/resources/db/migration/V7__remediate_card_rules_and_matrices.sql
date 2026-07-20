-- V7__remediate_card_rules_and_matrices.sql
-- Corrective Flyway Migration for CARD exact-5 tier & additional matrix presets

-- 1. Fix CARD rule exact 5 boxes tier from V5 (where min_area_sqm was 5.000 and max_area_sqm was 10.000)
UPDATE pricing_rules 
SET min_area_sqm = 5.000, max_area_sqm = 5.000, price_per_sqm = 40000, note = 'Card Visit tròn 5 hộp (40k/hộp)' 
WHERE category_code = 'CARD' AND min_area_sqm = 5.000 AND max_area_sqm = 10.000;

-- 2. Insert rule for 6-9 boxes (70k/box)
INSERT INTO pricing_rules (category_code, rule_name, min_area_sqm, max_area_sqm, price_per_sqm, lamination_fee_per_sqm, note, updated_by)
SELECT 'CARD', 'Card Visit 6-9 hộp', 6.000, 9.000, 70000, 0, 'Card Visit 6-9 hộp', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM pricing_rules WHERE category_code = 'CARD' AND rule_name = 'Card Visit 6-9 hộp'
);
