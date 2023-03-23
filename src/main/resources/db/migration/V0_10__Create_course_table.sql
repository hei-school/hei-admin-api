create table if not exists "course"
(
    id              varchar
        constraint course_pk primary key default uuid_generate_V4(),
    code            varchar not null
        constraint course_code_unique unique,
    name            varchar not null
        constraint course_name_unique unique,
    credits         integer     not null,
    total_hours     integer     not null,
    main_teacher_id varchar not null
        constraint fk_course_main_teacher references "user" (id)
);
create index if not exists course_main_teacher_index on "course" (main_teacher_id);
