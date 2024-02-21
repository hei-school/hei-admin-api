do
$$
begin
        if not exists(select from pg_type where typname = 'file_type') then
create type "file_type" as enum ('TRANSCRIPT', 'DOCUMENT', 'OTHER');
end if;
end
$$;

create table if not exists "file"
(
    id                varchar
    constraint file_pk primary key default uuid_generate_v4(),
    user_id           varchar                  not null
    constraint file_user_id_fk references "user"(id),
    name              varchar,
    creation_datetime timestamp with time zone not null default now(),
    file_type         file_type                not null,
    file_key_url      varchar
);