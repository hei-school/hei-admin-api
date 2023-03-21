do
$$
begin
        if not exists(select from pg_type where typname = 'course_status') then
create type course_status as enum ('LINKED', 'UNLINKED');
end if;
end
$$;

create table if not exists "course_followed"(
    id_course_followed varchar constraint course_followed_pk primary key default uuid_generate_v4(),
    student_id           varchar                  not null
    constraint course_followed_user_id_fk references "user"(id),
    course_id         varchar                  not null
    constraint course_followed_course_id_fk references "course"(id_course),
    status course_status
);