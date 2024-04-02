create table if not exists "work_info" (
    id varchar constraint work_info_pk primary key default uuid_generate_v4(),
    student_id varchar constraint work_info_student_fk references "user"(id),
    "commitment_begin_date" timestamp with time zone default now(),
    "commitment_end_date" timestamp with time zone,
    business varchar,
    company varchar
);