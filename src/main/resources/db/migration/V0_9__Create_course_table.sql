create extension if not exists "uuid-ossp";

create table if not exists "course"
(
    id                varchar
        constraint course_pk primary key                 default uuid_generate_v4(),
    name              varchar                  not null,
    code              varchar                  not null,
    credits           integer                  not null,
    total_hours       integer                  not null,
    main_teacher_id   varchar     not null
        constraint maint_teacher_id_fk references "user"(id)
);
