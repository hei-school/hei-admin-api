create table if not exists "grade"(
    id varchar
    constraint grade_pk primary key default uuid_generate_v4(),
    student_id varchar constraint user_fk references "user"(id),
    exam_id varchar constraint exam_fk references "exam"(id),
    constraint unique_student_grade unique (student_id, exam_id),
    score int not null,
    creation_datetime timestamp
);
