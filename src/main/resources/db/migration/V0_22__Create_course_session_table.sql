create table if not exists "course_session" (
    id                    varchar
    constraint course_session_pk primary key default uuid_generate_v4(),
    awarded_course_id             varchar                     not null
    constraint course_session_awareded_course_fk references "awarded_course"(id),
    "begin"                 timestamp   not null,
    "end"                   timestamp   not null
);