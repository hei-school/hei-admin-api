create table if not exists "student_course"
(
    course_id                varchar           not null
    constraint course_id_fk references "course"(id),
    student_id               varchar
    constraint student_id_fk references "user"(id),
    status                   varchar           not null
);