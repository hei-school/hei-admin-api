create table if not exists "course"
(
    id                varchar
    constraint course_pk primary key           default uuid_generate_v4(),
    code              varchar                  not null
    constraint course_ref_unique unique,
    name              varchar                  not null,
    credits           integer                  check ( credits > 0 ),
    total_hours       integer                  check ( total_hours > 0),
    main_teacher      varchar                  not null
    constraint teacher_id_fk references "user"(id)
);