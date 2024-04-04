do
$$
begin
    if not exists(select from pg_type where typname = 'scope') then
create type "scope" as enum ('GLOBAL', 'STUDENT', 'TEACHER', 'MANAGER');
end if;
end
$$;

create table if not exists announcement(
    id varchar constraint announcement_pk primary key default uuid_generate_v4(),
    title varchar not null,
    content text not null,
    author_id varchar constraint author_id_fk references "user"(id),
    creation_datetime timestamp with time zone default now(),
    "scope" scope not null
);