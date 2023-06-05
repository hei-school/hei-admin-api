create table if not exists "exam"
(
    id  varchar
    constraint exam_pk primary key default uuid_generate_v4(),
    coefficient     int     not null,
    title       varchar     not null,
    course_id   varchar     not null,
    constraint course_fk references "course",
    examination_date    datetime     not null
);
