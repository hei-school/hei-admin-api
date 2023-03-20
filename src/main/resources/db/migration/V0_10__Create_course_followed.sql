do
$$
begin
        if not exists(select from pg_type where typname = 'course_status') then
create type course_status as enum ('LINKED', 'UNLIKED');
end if;
end
$$;

create table if not exists "course_followed"(
    id_course_followed varchar constraint course_pk primary key default uuid_generate_v4(),
    user_id           varchar                  not null
    constraint course_followed_user_id_fk references "user"(id),
    course_id         varchar                  not null
    constraint course_followd_course_id_fk references "course"(id_course),
    status course_status
);