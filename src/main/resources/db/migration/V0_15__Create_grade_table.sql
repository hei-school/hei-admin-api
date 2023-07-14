create table if not exists "grade"
(
    id varchar
    constraint grade_pk primary key default uuid_generate_v4
(
),
    student_course_id varchar
    constraint student_course_fk references "student_course"
(
    id
),
    exam_id varchar
    constraint exam_fk references "exam"
(
    id
),
    constraint unique_student_grade unique
(
    student_course_id,
    exam_id
),
    score int not null,
    creation_datetime timestamp

    );
