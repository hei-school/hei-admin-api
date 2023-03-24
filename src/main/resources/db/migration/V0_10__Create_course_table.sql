create table if not exists "course"
(
    id                varchar
        constraint course_pk primary key default uuid_generate_v4(),
    code              varchar                  not null
        constraint course_code_unique unique,
    name              varchar                  not null
        constraint course_name_unique unique,
    main_teacher_id   varchar                  not null
        constraint course_main_teacher_id_fk references "user" (id),
    credits           integer                  not null,
    total_hours           integer                  not null
);