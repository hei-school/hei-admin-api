create table if not exists "course_session" (
    id                    varchar
    constraint course_session_pk primary key default uuid_generate_v4(),
    course_id             varchar                     not null
    constraint course_session_course_fk references "course"(id),
    "begin"                 timestamp   with time zone  not null,
    "end"                   timestamp   with time zone  not null
                                            );