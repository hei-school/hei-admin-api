create table if not exists "transcript" (
    id varchar constraint transcript_pk
    primary key default uuid_generate_v4(),
    student_id varchar,
    semester varhcar,
    academic_year integer,
    is_defenitive boolean,
    creation_datetime timestamp with time zone not null default now()
)