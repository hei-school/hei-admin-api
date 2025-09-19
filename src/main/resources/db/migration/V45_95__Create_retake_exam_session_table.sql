create table if not exist "retake_exam_session"(
    id varchar constraint retake_exam_session_pk primary key default uuid_generate_v4(),
    date_from timestamp not null,
    date_to timestamp not null
);