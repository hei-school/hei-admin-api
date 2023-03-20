create table if not exists "course"
(
    id                  varchar
        constraint course_pk primary key default uuid_generate_v4(),
    main_teacher_id          varchar             not null
        constraint main_teacher_id_fk references "user"(id),
    code                     varchar             not null unique,
    name                     varchar             not null,
    credits                  integer             not null,
    total_hours              integer             not null
);