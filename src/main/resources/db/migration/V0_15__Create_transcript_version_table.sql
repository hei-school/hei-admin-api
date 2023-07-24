create table if not exists "transcript_version"
(
    id                varchar
        constraint transcript_version_pk primary key default uuid_generate_v4(),
    ref               integer                  not null,
    creation_datetime timestamp with time zone not null default now(),
    pdf_link          varchar                  not null,
    transcript_id     varchar                  not null
        constraint transcript_id_fk references "transcript"(id),
    user_id           varchar                  not null
        constraint transcript_user_id_fk references "user"(id)
);