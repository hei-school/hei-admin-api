create table if not exists "transcript"
(
    id                  varchar
            constraint transcript_pk primary key default uuid_generate_v4(),
    student_id          varchar not null
            constraint transcript_student_id_fk references "user"(id),
    semester            varchar,
    academic_year       integer check( academic_year > 0),
    is_definitive boolean,
    creation_datetime   timestamp with time zone not null default now
);