create table if not exists "work_file" (
    id varchar constraint work_file_pk primary key default uuid_generate_v4(),
    filename varchar,
    creation_datetime timestamp with time zone not null default now(),
    commitment_begin timestamp with time zone not null,
    commitment_end timestamp with time zone not null,
    file_path      varchar                     not null,
    student_id varchar constraint work_file_student_id_fk references "user"(id)
);