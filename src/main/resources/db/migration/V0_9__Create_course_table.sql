create table if not exists "course"(
    id_course varchar constraint course_pk primary key default uuid_generate_v4(),
    code varchar unique,
    "name" varchar,
    credits int,
    total_hours int,
    id_teacher           varchar                  not null
    constraint course_teacher_id_fk references "user"(id)
);