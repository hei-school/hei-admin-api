create table if not exists "transcript_version" (
    id varchar constraint transcript_version_pk
        primary key default uuid_generate_v4(),
    pdf_link varchar,
    creation_datetime timestamp with time zone not null default now(),
    ref int,
    transcript_id varchar not null
        constraint transcript_fk references "transcript" (id),
    editor_id varchar not null
        constraint editor_fk references "user" (id)
);

create index if not exists transcript_index on "transcript_version" (transcript_id);
create index if not exists editor_index on "transcript_version" (editor_id);

