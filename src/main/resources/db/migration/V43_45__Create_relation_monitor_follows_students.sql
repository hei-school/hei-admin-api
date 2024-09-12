create table if not exists "monitor_following_student" (
    id          varchar
        constraint mfs_pk primary key default uuid_generate_v4(),
    student_id  varchar not null
        constraint mfs_student_id_fk references "user"(id),
    monitor_id  varchar not null
        constraint mfs_teacher_id_fk references "user"(id)
);