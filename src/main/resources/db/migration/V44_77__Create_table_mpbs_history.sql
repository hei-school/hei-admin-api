create table if not exists "mpbs_status_history"
(
    id               varchar
        constraint mpbs_status_history_pk primary key default uuid_generate_v4(),
    "mpbs_id"        varchar references "mpbs" (id) not null,
    "status"         mpbs_status                    not null,
    creation_instant timestamp with time zone         default now(),
    update_instant   timestamp with time zone         default now()
);
