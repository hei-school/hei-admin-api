create table if not exists "retake_exam" (
    id varchar constraint retake_exam_pk primary key default uuid_generate_v4(),
    course_id varchar not null constraint course_retake_exam_fk references "course"(id),
    retake_exam_session_id varchar not null constraint retake_exam_session_fk references "retake_exam_session"(id),
    student_id varchar not null constraint student_fk references "user"(id)
);