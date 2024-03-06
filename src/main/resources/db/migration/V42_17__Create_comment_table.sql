create table if not exists "comment"(
    id varchar constraint comment_pk primary key default uuid_generate_v4(),
    subject_id varchar constraint subject_id_fk references "user"(id),
    observer_id varchar constraint observer_id_fk references "user"(id),
    content text not null,
    creation_datetime timestamp with time zone not null default now()
);