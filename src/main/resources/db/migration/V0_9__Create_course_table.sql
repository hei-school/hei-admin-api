create table if not exists "course"
(
    id                varchar
    constraint course_pk primary key           default uuid_generate_v4(),
    code              varchar                  not null
    constraint course_ref_unique unique,
    name              varchar                  not null,
    credits           integer,
    total_hours       integer,
    main_teacher      varchar                  not null
    constraint teacher_id_fk references "user"(id)
);