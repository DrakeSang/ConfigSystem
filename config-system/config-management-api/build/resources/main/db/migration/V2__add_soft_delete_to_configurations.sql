ALTER TABLE configurations
ADD COLUMN deleted_at TIMESTAMP NULL;

CREATE INDEX idx_config_not_deleted
ON configurations (app_name, env)
WHERE deleted_at IS NULL;
