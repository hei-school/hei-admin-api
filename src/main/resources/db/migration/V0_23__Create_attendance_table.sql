do
$$
begin
        if not exists(select from pg_type where typname = 'attendance_movement_type') then
create type "attendance_movement_type" as enum ('IN', 'OUT');
end if;
        if not exists(select from pg_type where typname = 'place') then
create type "place" as enum ('ANDRAHARO', 'IVANDRY');
end if;
end
$$;

create table if not exists "attendance" (
    id                                  varchar
    constraint  attendance_pk primary key default uuid_generate_v4(),
    created_at                          timestamp with time zone default null,
    is_late                             boolean default false,
    late_of                             integer,
    attendance_movement_type            attendance_movement_type default 'IN',
    place                               place,
    course_session_id                   varchar
    constraint  attendance_course_session_fk references "course_session"(id),
    student_id                          varchar     not null
    constraint  attendance_student_fk       references  "user"(id)
    );
create index attendance_created_at_index on "attendance" (created_at ASC NULLS LAST);