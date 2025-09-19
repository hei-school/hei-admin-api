create table if not exists "retake_exam_session"(
    id varchar constraint session_pk primary key default uuid_generate_v4(),
    date_from timestamp not null,
    date_to timestamp not null
);