create table if not exists "user_activity" (
    id varchar constraint user_activity_pk primary key default uuid_generate_v4(),
    user_id     VARCHAR,
    user_email  VARCHAR,
    endpoint    VARCHAR NOT NULL,
    http_method VARCHAR,
    request_body JSONB,
    created_at  timestamp with time zone not null default now()
);