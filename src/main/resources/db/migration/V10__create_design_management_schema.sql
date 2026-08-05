-- V10: Design Management Subsystem & Order Linking Schema
-- Designed to decouple design management from pricing rules completely.

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    order_code VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    total_amount NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED',
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    category_code VARCHAR(50) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    width_cm NUMERIC(10, 2),
    height_cm NUMERIC(10, 2),
    quantity INT NOT NULL DEFAULT 1,
    material_code VARCHAR(50),
    calculated_price NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    specifications_json TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS design_tasks (
    id BIGSERIAL PRIMARY KEY,
    task_code VARCHAR(50) NOT NULL UNIQUE,
    order_item_id BIGINT NOT NULL REFERENCES order_items(id) ON DELETE CASCADE,
    designer_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_ASSIGNMENT',
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    deadline TIMESTAMPTZ,
    designer_note TEXT,
    customer_feedback TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS design_files (
    id BIGSERIAL PRIMARY KEY,
    design_task_id BIGINT NOT NULL REFERENCES design_tasks(id) ON DELETE CASCADE,
    version_number INT NOT NULL DEFAULT 1,
    file_type VARCHAR(30) NOT NULL DEFAULT 'PREVIEW_IMAGE',
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size_bytes BIGINT NOT NULL DEFAULT 0,
    uploaded_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    is_approved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS design_activity_logs (
    id BIGSERIAL PRIMARY KEY,
    design_task_id BIGINT NOT NULL REFERENCES design_tasks(id) ON DELETE CASCADE,
    actor_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    action_type VARCHAR(50) NOT NULL,
    content TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index optimization for Design Management queries
CREATE INDEX IF NOT EXISTS idx_orders_customer ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_design_tasks_designer ON design_tasks(designer_id);
CREATE INDEX IF NOT EXISTS idx_design_tasks_status ON design_tasks(status);
CREATE INDEX IF NOT EXISTS idx_design_files_task ON design_files(design_task_id);
CREATE INDEX IF NOT EXISTS idx_design_logs_task ON design_activity_logs(design_task_id);
