create table if not exists "course"
(
    id                varchar
        constraint course_pk primary key default uuid_generate_v4(),
    teacher_id           varchar                  not null
        constraint course_teacher_id_fk references "user"(id),
    code varchar unique not null,
    name varchar,
    credits int not null,
    total_hours int not null
);
create index if not exists courses_teacher_id_index on "course" (teacher_id);