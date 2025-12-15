CREATE TABLE configurations
(
    id         UUID PRIMARY KEY,
    app_name   VARCHAR(100) NOT NULL,
    env        VARCHAR(50)  NOT NULL,
    version    INTEGER      NOT NULL,
    data       JSONB        NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL
);

CREATE INDEX idx_config_app_env
    ON configurations (app_name, env);

CREATE UNIQUE INDEX idx_config_app_env_version
    ON configurations (app_name, env, version);
