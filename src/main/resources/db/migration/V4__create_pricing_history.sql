CREATE TABLE pricing_history (
    id BIGSERIAL PRIMARY KEY,
    target_type VARCHAR(30) NOT NULL,
    target_id BIGINT NOT NULL,
    field_name VARCHAR(50) NOT NULL,
    old_value VARCHAR(255),
    new_value VARCHAR(255),
    changed_by VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pricing_history_target ON pricing_history(target_type, target_id);
CREATE INDEX idx_pricing_history_created_at ON pricing_history(created_at);
