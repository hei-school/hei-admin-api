create table if not exists "course"(
    id_course varchar constraint course_pk primary key default uuid_generate_v4(),
    code varchar unique,
    "name" varchar,
    credits int,
    total_hours int,
    user_id           varchar                  not null
    constraint course_user_id_fk references "user"(id)
);