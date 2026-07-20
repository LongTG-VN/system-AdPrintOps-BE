-- Forward-only CARD quantity tiers. Do not reuse pricing_rules area intervals.
CREATE UNIQUE INDEX IF NOT EXISTS uk_pricing_configurations_category_key
    ON pricing_configurations(category_code, config_key);

INSERT INTO pricing_configurations (category_code, config_key, config_name, base_price, multiplier, active, note) VALUES
('CARD','BOX_1_4','Card Visit 1-4 hộp',70000,1,true,'Đơn giá mỗi hộp'),
('CARD','BOX_5','Card Visit đúng 5 hộp',40000,1,true,'Đơn giá mỗi hộp'),
('CARD','BOX_6_9','Card Visit 6-9 hộp',70000,1,true,'Đơn giá mỗi hộp'),
('CARD','BOX_10_PLUS','Card Visit từ 10 hộp',24000,1,true,'Đơn giá mỗi hộp')
ON CONFLICT (category_code, config_key) DO NOTHING;
