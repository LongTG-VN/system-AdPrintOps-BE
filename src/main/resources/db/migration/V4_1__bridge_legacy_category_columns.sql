-- V5 was released with temporary category_code/category_name identifiers.
-- Preserve the released V5 checksum by exposing those names only while V5 runs.
ALTER TABLE pricing_categories RENAME COLUMN code TO category_code;
ALTER TABLE pricing_categories RENAME COLUMN name TO category_name;
