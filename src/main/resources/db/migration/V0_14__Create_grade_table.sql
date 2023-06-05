create table if not exists "grade"
(
    id  varchar
    constraint grade_pk primary key default uuid_generate_v4(),
    user_id varchar
    constraint user_fk references "user",
    exam_id varchar
    constraint exam_fk references "exam",
    constraint unique_student_grade unique
(user_id, exam_id),
    score   int     not null,
    creation_datetime datetime

    );
