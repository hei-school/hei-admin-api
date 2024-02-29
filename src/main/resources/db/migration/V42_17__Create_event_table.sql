do
$$
begin
    if not exists(select from pg_type where typname = 'event_type') then
        create type "event_type" as enum ('COURSE', 'INTEGRATION', 'SEMINAR', 'SUPPORT_SESSION', 'OTHER');
    end if;
end
$$;

create table if not exists "event"(
    id varchar constraint event_pk primary key default uuid_generate_v4(),
    type event_type not null,
    description varchar,
    "begin" timestamp with time zone not null,
    "end" timestamp with time zone not null,
    planner_id varchar constraint planner_id_fk references "user"(id),
    course_id varchar constraint event_course_id_fk references "course"(id)
);

