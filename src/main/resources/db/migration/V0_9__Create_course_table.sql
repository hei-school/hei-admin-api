create table if not exists "course"
(
    id                varchar
        constraint course_pk primary key default uuid_generate_v4(),
    code              varchar                   not null,
    "name"              varchar                   not null,
    credits              integer                   not null,
    total_hours              integer                   not null,
    user_id           varchar                  not null
        constraint course_user_id_fk references "user"(id)
);
create index if not exists course_user_id_index on "course" (user_id);