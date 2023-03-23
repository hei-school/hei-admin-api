create table if not exists "course"
(
    id                varchar
        constraint course_pk primary key default uuid_generate_v4(),
    code              varchar                  not null,
    name              varchar                  not null,
    credit            integer                      not null,
    total_hours       integer                   not null,
    main_teacher      varchar not null
        constraint main_teacher_id_fk references "user" (id)
);