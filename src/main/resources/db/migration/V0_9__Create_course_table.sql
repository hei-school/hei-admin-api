create table if not exists "course"
(
    id                varchar
    constraint course_pk primary key default uuid_generate_v4(),
    code              varchar              unique not null,
    name              varchar,
    credits           integer,
    total_hours       integer,
    main_teacher_id   varchar
        constraint main_teacher_id_fk references "user"(id)
);