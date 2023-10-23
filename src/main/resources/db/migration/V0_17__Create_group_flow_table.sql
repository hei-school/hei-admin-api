do
$$
begin
        if not exists(select from pg_type where typname = 'group_flow_type') then
create type "group_flow_type" as enum ('JOIN', 'LEAVE');
end if;
end
$$;
create table if not exists "group_flow" (
    id varchar constraint group_flow_pk primary key default uuid_generate_v4(),
    group_flow_type group_flow_type not null,
    group_id varchar not null constraint group_flow_group_id_fk references "group"(id),
    student_id varchar not null constraint group_flow_user_id_fk references "user"(id),
    flow_datetime timestamp not null
);