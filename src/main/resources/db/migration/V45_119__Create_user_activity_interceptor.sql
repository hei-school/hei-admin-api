create table if not exists "user_activity" (
    id          BIGSERIAL PRIMARY KEY,
    user_id     VARCHAR,
    user_email  VARCHAR,
    endpoint    VARCHAR NOT NULL,
    http_method VARCHAR,
    request_body JSONB,
    created_at  timestamp with time zone not null default now()
);