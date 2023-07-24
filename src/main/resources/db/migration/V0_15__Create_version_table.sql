create table "version"(
    id varchar primary key,
    ref int not null,
    created_by varchar references  "user"(id),
    transcript_id varchar references "transcript"(id),
    creation_datetime timestamp with time zone not null default now()
);
