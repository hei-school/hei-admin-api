create table if not exists "course"
(
    id                varchar
        constraint user_pk primary key default uuid_generate_v4(),
    name              varchar                  not null,
    code              varchar                  not null,
    name              varchar                  not null,
    credits           number                  not null,
    total_hours       number                  not null,
    user_id           varchar                  not null
            constraint course_user_id_fk references "user"(id),
);
