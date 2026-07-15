-- Non-destructive report reliability upgrade for existing MySQL 8.0.40 deployments.
-- Run after 20260714_report_permissions_and_bootstrap_admin.sql during the v1.0.0 upgrade.

USE career_ability;

ALTER TABLE report_record
    ADD COLUMN IF NOT EXISTS filter_city VARCHAR(100) NULL AFTER time_range_end,
    ADD COLUMN IF NOT EXISTS filter_position VARCHAR(100) NULL AFTER filter_city,
    ADD COLUMN IF NOT EXISTS filter_industry VARCHAR(100) NULL AFTER filter_position,
    ADD COLUMN IF NOT EXISTS analysis_dimensions VARCHAR(500) NULL AFTER filter_industry,
    ADD COLUMN IF NOT EXISTS analysis_scope TEXT NULL AFTER analysis_dimensions,
    ADD COLUMN IF NOT EXISTS generation_started_at DATETIME NULL AFTER error_msg,
    ADD COLUMN IF NOT EXISTS generation_attempts INT NOT NULL DEFAULT 0 AFTER generation_started_at;

CREATE INDEX idx_report_status_update ON report_record (status, update_time);
