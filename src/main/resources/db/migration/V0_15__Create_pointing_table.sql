create table if not exists "pointing" (
    id                      varchar
        constraint pointing_pk primary key default uuid_generate_v4(),
    pointing_datetime       timestamp with time zone,
    place                   varchar,
    datetime_course_enter   timestamp with time zone not null,
    datetime_course_exit    timestamp with time zone not null,
    is_late                 boolean             default true,
    is_missing              boolean             default true,
    course_id               varchar     not null
        constraint  pointing_course_fk      references  "course"(id),
    student_id              varchar     not null
        constraint  pointing_student_fk     references  "user"(id)
);