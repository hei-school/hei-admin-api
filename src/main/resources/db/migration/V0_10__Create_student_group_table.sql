
create table if not exists "student_group"
(
    id                varchar
        constraint group_student_pk primary key default uuid_generate_v4(),
    group_id              varchar                  not null
        constraint group_student_group_id_fk references "group"(id),
    student_id        varchar                  not null
        constraint event_student_student_id_fk references "user"(id)
);
