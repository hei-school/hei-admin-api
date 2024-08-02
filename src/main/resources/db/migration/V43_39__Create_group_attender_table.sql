create table if not exists group_attender (
    id varchar constraint group_attender_pk primary key default uuid_generate_v4(),
    group_id varchar not null constraint group_attender_group_id_fk references "group"(id),
    student_id varchar not null constraint group_attender_student_id_fk references "user"(id),
    migration_datetime timestamp not null,
    is_deleted boolean default false
);