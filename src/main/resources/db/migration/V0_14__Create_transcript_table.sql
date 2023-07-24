create table "transcript"(
    id varchar primary key,
    student_id varchar references "user"(id),
    semester varchar not null,
    academic_year varchar,
    is_definitive boolean,
    creation_datetime timestamp with time zone not null default now()
);
