create table if not exists "transcript_history"
(
    id                      varchar
    constraint transcript_history_pk primary key default uuid_generate_v4(),
    transcript_id           varchar not null
    constraint transcript_history_transcript_id_fk references "transcript"(id),
    created_by_user_id      varchar not null
    constraint transcript_history_user_id_fk references "user"(id),
    created_by_user_role    varchar,
    creation_datetime       timestamp with time zone not null default now()
);